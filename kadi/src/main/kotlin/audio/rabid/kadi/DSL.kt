package audio.rabid.kadi

import androidx.annotation.CheckResult


class BindingBlock internal constructor(internal val module: Module) {

    fun require(otherModule: Module) {
        module.require(otherModule)
    }

    @CheckResult
    inline fun <reified T: Any> bind(
        identifier: Any = Unit,
        overrides: Boolean = false
    ): PartialBindingBlock<T> =
        PartialBindingBlock(this, BindingKey(T::class, identifier), overrides)
}

// TODO These could be better implemented with interfaces and a single class

class PartialBindingBlock<T: Any>(
    private val bindingBlock: BindingBlock,
    private val bindingKey: BindingKey<T>,
    private val overrides: Boolean
) {

    fun with(provider: Provider<T>) {
        bindingBlock.module.addBinding(Binding(bindingKey, overrides, provider))
    }

    fun providerBlock(): ProviderBlock = ProviderBlock(bindingBlock.module)

    inline fun toInstance(value: T) {
        with(JustProvider(value))
    }

    inline fun with(noinline block: ProviderBlock.() -> T) {
        with(LambdaProvider(providerBlock(), block))
    }

    inline fun withSingleton(provider: Provider<T>) {
        with(SingletonProvider(provider))
    }

    inline fun withSingleton(noinline block: ProviderBlock.() -> T) {
        with(SingletonProvider(LambdaProvider(providerBlock(), block)))
    }
}

class ProviderBlock(private val module: Module) {
    fun <T: Any> get(bindingKey: BindingKey<T>): T {
        return Kadi.getScopeForModule(module).get(bindingKey)
    }

    inline fun <reified T: Any> get(identifier: Any = Unit): T =
        get(BindingKey(T::class, identifier))
}

fun module(name: String, allowOverrides: Boolean = false, block: BindingBlock.() -> Unit): Module {
    val module = Module(name, allowOverrides)
    val bindingBlock = BindingBlock(module)
    block.invoke(bindingBlock)
    return module
}
