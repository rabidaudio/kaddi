package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.*

internal class DSLModule(
        override val name: String
) : KaddiModule, BindingBlock, ProviderBlock {
    private val importedModules = mutableSetOf<KaddiModule>()
    private val bindings = mutableListOf<Binding<*>>()
    private val requiredBindings = mutableSetOf<BindingKey<*>>()

    internal lateinit var boundScope: Scope

    override fun getBindings(): List<Binding<*>> = bindings
    override fun getImportedModules(): Set<KaddiModule> = importedModules

    override fun import(otherModule: Module) {
        importedModules.add(otherModule as KaddiModule)
    }

    fun addBinding(binding: Binding<*>) {
        bindings.add(binding)
    }

    fun addRequiredBinding(bindingKey: BindingKey<*>) {
        requiredBindings.add(bindingKey)
    }

    override fun onAttachedToScope(scope: Scope) {
        // runtime verification of bindings
        for (requiredBinding in requiredBindings) {
            check(scope.contains(requiredBinding)) {
                "$this expected $requiredBinding to be defined in another module but was not found in $scope"
            }
        }
        boundScope = scope
    }

    override fun copy(): KaddiModule {
        return DSLModule(name).also { new ->
            new.importedModules.addAll(importedModules)
            new.requiredBindings.addAll(requiredBindings)
            new.bindings.addAll(bindings.map { it.copyForNewModule(new) })
        }
    }

    private fun <T : Any> Binding<T>.copyForNewModule(module: DSLModule): Binding<T> {
        return when (this) {
            is Binding.Basic -> {
                val lambdaProvider = provider as? LambdaProvider<T> ?: return this
                return copy(provider = lambdaProvider.withNewBlock(module))
            }
            is Binding.Set -> this
        }
    }

    override fun toString(): String {
        return "Module($name)"
    }
}
