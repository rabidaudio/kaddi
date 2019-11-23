package audio.rabid.kaddi

interface Scope {
    fun <T: Any> get(key: BindingKey<T>): T

    fun inject(receiver: Any)

    fun createChildScope(identifier: Any, vararg modules: Module): Scope

    fun close()
}

inline fun <reified T: Any> Scope.get(identifier: Any = Unit): T {
    return get(BindingKey(T::class, identifier))
}
