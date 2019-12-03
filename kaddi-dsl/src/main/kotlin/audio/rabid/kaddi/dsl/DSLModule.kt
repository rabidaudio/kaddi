package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.*

internal class DSLModule(
        override val name: String
) : KaddiModule, BindingBlock, ProviderBlock {

    private val commands = mutableListOf<Command>()
//    private val importedModules = mutableSetOf<KaddiModule>()
//    private val bindings = mutableListOf<Binding<*>>()
    private val requiredBindings = mutableSetOf<BindingKey<*>>()
    private val onReadyCallbacks = mutableListOf<OnReadyCallback>()

    internal lateinit var boundScope: Scope

//    override fun getBindings(): List<Binding<*>> = bindings
//    override fun getImportedModules(): Set<KaddiModule> = importedModules

    override fun getCommands(): List<Command> = commands

    override fun import(otherModule: Module) {
        commands.add(Command.ImportModule(otherModule as KaddiModule))
    }

    fun addBinding(binding: Binding<*>) {
        commands.add(Command.AddBinding(binding))
    }

    fun addRequiredBinding(bindingKey: BindingKey<*>) {
        requiredBindings.add(bindingKey)
    }

    override fun onReady(callback: OnReadyCallback) {
        onReadyCallbacks.add(callback)
    }

    override fun onAttachedToScope(scope: Scope) {
        // runtime verification of bindings
        for (requiredBinding in requiredBindings) {
            check(scope.contains(requiredBinding)) {
                "$this expected $requiredBinding to be defined in another module but was not found in $scope"
            }
        }
        boundScope = scope
        for (callback in onReadyCallbacks) {
            callback.invoke(this)
        }
    }

    fun withPrependedBinding(binding: Binding<*>): DSLModule {
        return copy().also { it.commands.add(0, Command.AddBinding(binding)) }
    }

    override fun copy(): DSLModule {
        return DSLModule(name).also { new ->
            for (command in commands) {
                when (command) {
                    is Command.AddBinding -> new.commands.add(Command.AddBinding(command.binding.copyForNewModule(new)))
                    else -> new.commands.add(command)
                }
            }
            new.requiredBindings.addAll(requiredBindings)
            new.onReadyCallbacks.addAll(onReadyCallbacks)
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
