package audio.rabid.kaddi

interface Scope : DependencyProvider {
    fun inject(receiver: Any)

    fun createChildScope(qualifier: Any, vararg modules: Module): Scope

    fun close()
}
