package audio.rabid.kadi.dsl

import audio.rabid.kadi.Binding
import audio.rabid.kadi.KadiModule
import audio.rabid.kadi.Module
import audio.rabid.kadi.Scope

internal class DSLModule(
        override val name: String,
        override val allowOverrides: Boolean
) : KadiModule, BindingBlock, ProviderBlock {
    private val importedModules = mutableSetOf<KadiModule>()
    private val bindings = mutableListOf<Binding<*>>()

    internal lateinit var boundScope: Scope

    override fun getBindings(): List<Binding<*>> = bindings
    override fun getImportedModules(): Set<KadiModule> = importedModules

    override fun require(otherModule: Module) {
        importedModules.add(otherModule as KadiModule)
    }

    fun addBinding(binding: Binding<*>) {
        bindings.add(binding)
    }

    override fun onAttachedToScope(scope: Scope) {
        boundScope = scope
    }

    override fun copy(): KadiModule {
        return DSLModule(name, allowOverrides).also { new ->
            new.importedModules.addAll(importedModules)
            new.bindings.addAll(bindings.map { it.copyForNewModule(new) })
        }
    }

    private fun <T : Any> Binding<T>.copyForNewModule(module: DSLModule): Binding<T> {
        val lambdaProvider = provider as? LambdaProvider<T> ?: return this
        return copy(provider = lambdaProvider.withNewBlock(module))
    }

    override fun toString(): String {
        return "Module($name)"
    }
}
