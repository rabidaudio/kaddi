package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.*

internal class DSLModule(
        override val name: String
) : KaddiModule, BindingBlock {

    private val commands = mutableListOf<Command>()
    private val requiredBindings = mutableSetOf<BindingKey<*>>()
    private val onBoundToScope = mutableListOf<OnAttachedToScope>()

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

    override fun onAttachedToScope(callback: OnAttachedToScope) {
        onBoundToScope.add(callback)
    }

    override fun onAttachedToScope(scope: Scope) {
        // runtime verification of bindings
        for (requiredBinding in requiredBindings) {
            check(scope.contains(requiredBinding)) {
                "$this expected $requiredBinding to be defined in another module but was not found in $scope"
            }
        }
        for (callback in onBoundToScope) {
            callback.invoke(scope)
        }
    }

    fun withPrependedBinding(binding: Binding<*>): DSLModule {
        return DSLModule(name).also { new ->
            new.commands.add(Command.AddBinding(binding))
            new.commands.addAll(commands)
            new.requiredBindings.addAll(requiredBindings)
            new.onBoundToScope.addAll(onBoundToScope)
        }
    }

    override fun toString(): String {
        return "Module($name)"
    }
}
