package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.Binding
import audio.rabid.kaddi.BindingKey
import audio.rabid.kaddi.Provider

internal class BindingBuilder<T : Any>(
        private val bindingKey: BindingKey<T>,
        private val overrides: Boolean,
        private val intoSet: Boolean,
        val module: DSLModule
) : PartialBindingBlock<T> {

    override fun with(provider: Provider<T>) {
        addBinding(provider, singleton = false)
    }

    override fun withSingleton(provider: Provider<T>) {
        addBinding(provider, singleton = true)
    }

    private fun addBinding(provider: Provider<T>, singleton: Boolean) {
        module.addBinding(Binding.Basic(
                key = bindingKey,
                overrides = overrides,
                singleton = singleton,
                intoSet = intoSet,
                provider = provider
        ))
    }
}
