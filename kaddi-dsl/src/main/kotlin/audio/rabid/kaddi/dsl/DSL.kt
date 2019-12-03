package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.*

typealias OnReadyCallback = ProviderBlock.() -> Unit

interface BindingBlock {
    companion object {
        // this companion object trick is a way to still supply a public method that does the work without cluttering
        // the methods of the interface, leaving only the inline versions of the methods suggested by the IDE
        fun <T : Any> bind(block: BindingBlock, bindingKey: BindingKey<T>, overrides: Boolean, intoSet: Boolean): PartialBindingBlock<T> =
                BindingBuilder(bindingKey, overrides, intoSet, block as DSLModule)

        fun <T : Any> createSetBinding(block: BindingBlock, key: BindingKey<T>) {
            (block as DSLModule).addBinding(Binding.Set(key))
        }

        fun <T : Any> require(block: BindingBlock, bindingKey: BindingKey<T>) {
            (block as DSLModule).addRequiredBinding(bindingKey)
        }

        fun <T : Any> prependingBinding(module: Module, binding: Binding<T>): Module {
            return (module as DSLModule).withPrependedBinding(binding)
        }
    }

    fun import(otherModule: Module)

    fun onReady(callback: OnReadyCallback)
}

inline fun <reified T : Any> BindingBlock.bind(
        qualifier: Any = Unit,
        overrides: Boolean = false
): PartialBindingBlock<T> = BindingBlock.bind(this, BindingKey(T::class, qualifier), overrides, intoSet = false)

inline fun <reified T : Any> BindingBlock.bindConstant(qualifier: Any, block: () -> T) {
    bind<T>(qualifier).toInstance(block.invoke())
}

inline fun <reified T : Any> BindingBlock.declareSetBinding(
        qualifier: Any = Unit
) {
    BindingBlock.createSetBinding(this, BindingKey(T::class, qualifier, set = true))
}

inline fun <reified T : Any> BindingBlock.bindIntoSet(
        qualifier: Any = Unit
) = BindingBlock.bind(this, BindingKey(T::class, qualifier, set = true), overrides = false, intoSet = true)

inline fun <reified T : Any> BindingBlock.require(qualifier: Any = Unit) {
    BindingBlock.require(this, BindingKey(T::class, qualifier))
}

interface PartialBindingBlock<T : Any> {
    companion object {
        fun <T : Any> createProvider(block: PartialBindingBlock<T>, lambda: ProviderBlock.() -> T): Provider<T> =
                LambdaProvider((block as BindingBuilder).module, lambda)
    }

    fun with(provider: Provider<T>)

    fun withSingleton(provider: Provider<T>)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.toInstance(value: T) {
    with(JustProvider(value))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.with(noinline block: ProviderBlock.() -> T) {
    with(PartialBindingBlock.createProvider(this, block))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.withSingleton(noinline block: ProviderBlock.() -> T) {
    withSingleton(PartialBindingBlock.createProvider(this, block))
}

interface ProviderBlock {

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun <T : Any> getInstance(block: ProviderBlock, bindingKey: BindingKey<T>): T {
            return (block as DSLModule).boundScope.getInstance(bindingKey) as T
        }

        fun <T : Any> getSetInstance(block: ProviderBlock, bindingKey: BindingKey<T>): Set<T> {
            return (block as DSLModule).boundScope.getInstance(bindingKey) as Set<T>
        }
    }
}

inline fun <reified T : Any> ProviderBlock.instance(qualifier: Any = Unit): T =
        ProviderBlock.getInstance(this, BindingKey(T::class, qualifier))

inline fun <reified T : Any> ProviderBlock.setInstance(qualifier: Any = Unit): Set<T> =
        ProviderBlock.getSetInstance(this, BindingKey(T::class, qualifier, set = true))

fun module(name: String, block: BindingBlock.() -> Unit): Module {
    return DSLModule(name).also(block)
}

inline fun <reified T : Any> Module.includingInstance(value: T, qualifier: Any = Unit): Module {
    val binding = Binding.Basic(BindingKey(T::class, qualifier), overrides = false, singleton = false,
            intoSet = false, provider = JustProvider(value))
    return BindingBlock.prependingBinding(this, binding)
}
