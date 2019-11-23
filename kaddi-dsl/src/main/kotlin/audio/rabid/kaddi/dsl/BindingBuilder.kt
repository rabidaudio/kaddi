package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.Binding
import audio.rabid.kaddi.BindingKey
import audio.rabid.kaddi.Provider

internal class BindingBuilder<T : Any>(
        private val bindingKey: BindingKey<T>,
        private val overrides: Boolean,
        val module: DSLModule
) : PartialBindingBlock<T> {

    override fun with(provider: Provider<T>) {
        module.addBinding(Binding(bindingKey, overrides, false, provider))
    }

    override fun withSingleton(provider: Provider<T>) {
        module.addBinding(Binding(bindingKey, overrides, true, provider))
    }
}
