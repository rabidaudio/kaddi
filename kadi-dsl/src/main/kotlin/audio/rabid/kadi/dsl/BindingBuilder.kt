package audio.rabid.kadi.dsl

import audio.rabid.kadi.Binding
import audio.rabid.kadi.BindingKey
import audio.rabid.kadi.Provider

internal class BindingBuilder<T: Any>(
        private val bindingKey: BindingKey<T>,
        private val overrides: Boolean,
        val module: DSLModule
) : PartialBindingBlock<T> {

    override fun with(provider: Provider<T>) {
        module.addBinding(Binding(bindingKey, overrides, provider))
    }
}
