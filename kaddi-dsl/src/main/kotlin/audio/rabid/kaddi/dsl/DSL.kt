package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.*

typealias OnAttachedToScope = DependencyProvider.() -> Unit

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

    /**
     * Declare that this module depends on [otherModule].
     */
    fun import(otherModule: Module)

    /**
     * Run code immediately after the module has been added to a scope
     */
    fun onAttachedToScope(callback: OnAttachedToScope)
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
    fun with(provider: Provider<T>)

    fun withSingleton(provider: Provider<T>)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.toInstance(value: T) {
    with(JustProvider(value))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.with(noinline block: DependencyProvider.() -> T) {
    with(LambdaProvider(block))
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Any> PartialBindingBlock<T>.withSingleton(noinline block: DependencyProvider.() -> T) {
    withSingleton(LambdaProvider(block))
}

fun module(name: String, block: BindingBlock.() -> Unit): Module {
    return DSLModule(name).also(block)
}

inline fun <reified T : Any> Module.includingInstance(value: T, qualifier: Any = Unit): Module {
    val binding = Binding.Basic(BindingKey(T::class, qualifier), overrides = false, singleton = false,
            intoSet = false, provider = JustProvider(value))
    return BindingBlock.prependingBinding(this, binding)
}
