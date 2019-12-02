package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.Binding
import audio.rabid.kaddi.BindingKey
import audio.rabid.kaddi.Provider
import audio.rabid.kaddi.dsl.BindingType.*

internal class BindingBuilder<T : Any>(
        private val bindingKey: BindingKey<T>,
        private val overrides: Boolean,
        private val type: BindingType,
        val module: DSLModule
) : PartialBindingBlock<T> {

    override fun with(provider: Provider<T>) {
        addBinding(provider, singleton = false)
    }

    override fun withSingleton(provider: Provider<T>) {
        addBinding(provider, singleton = true)
    }

    private fun addBinding(provider: Provider<T>, singleton: Boolean) {
        val binding = when (type) {
            BASIC,
            INTO_SET -> Binding.Basic(
                    key = bindingKey,
                    overrides = overrides,
                    singleton = singleton,
                    intoSet = type == INTO_SET,
                    provider = provider
            )
            SET -> Binding.Set(key = bindingKey)
        }
        module.addBinding(binding)
    }
}
