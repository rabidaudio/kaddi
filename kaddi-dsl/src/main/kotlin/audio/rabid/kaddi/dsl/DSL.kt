package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.BindingKey
import audio.rabid.kaddi.JustProvider
import audio.rabid.kaddi.Module
import audio.rabid.kaddi.Provider

interface BindingBlock {
    companion object {
        // this companion object trick is a way to still supply a public method that does the work without cluttering
        // the methods of the interface, leaving only the inline versions of the methods suggested by the IDE
        fun <T : Any> bind(block: BindingBlock, bindingKey: BindingKey<T>, overrides: Boolean): PartialBindingBlock<T> =
                BindingBuilder(bindingKey, overrides, block as DSLModule)
    }

    fun require(otherModule: Module)
}

inline fun <reified T : Any> BindingBlock.bind(
        identifier: Any = Unit,
        overrides: Boolean = false
): PartialBindingBlock<T> = BindingBlock.bind(this, BindingKey(T::class, identifier), overrides)

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
    companion object {
        fun <T : Any> get(block: ProviderBlock, bindingKey: BindingKey<T>): T =
                (block as DSLModule).boundScope.get(bindingKey)
    }
}

inline fun <reified T : Any> ProviderBlock.instance(identifier: Any = Unit): T =
        ProviderBlock.get(this, BindingKey(T::class, identifier))

fun module(name: String, allowOverrides: Boolean = false, block: BindingBlock.() -> Unit): Module {
    return DSLModule(name, allowOverrides).also(block)
}
