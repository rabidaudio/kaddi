package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.Binding
import audio.rabid.kaddi.KaddiModule
import audio.rabid.kaddi.Module
import audio.rabid.kaddi.Scope

internal class DSLModule(
        override val name: String,
        override val allowOverrides: Boolean
) : KaddiModule, BindingBlock, ProviderBlock {
    private val importedModules = mutableSetOf<KaddiModule>()
    private val bindings = mutableListOf<Binding<*>>()

    internal lateinit var boundScope: Scope

    override fun getBindings(): List<Binding<*>> = bindings
    override fun getImportedModules(): Set<KaddiModule> = importedModules

    override fun require(otherModule: Module) {
        importedModules.add(otherModule as KaddiModule)
    }

    fun addBinding(binding: Binding<*>) {
        bindings.add(binding)
    }

    override fun onAttachedToScope(scope: Scope) {
        boundScope = scope
    }

    override fun copy(): KaddiModule {
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
