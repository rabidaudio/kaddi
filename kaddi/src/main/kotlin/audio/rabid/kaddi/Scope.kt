package audio.rabid.kaddi

import kotlin.reflect.KClass

interface Scope {
    fun getInstance(key: BindingKey<*>): Any

    operator fun <T : Any> get(type: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return getInstance(BindingKey(type)) as T
    }

    fun contains(key: BindingKey<*>): Boolean

    fun inject(receiver: Any)

    fun createChildScope(qualifier: Any, vararg modules: Module): Scope

    fun close()
}

inline fun <reified T : Any> Scope.getInstance(qualifier: Any = Unit): T {
    return getInstance(BindingKey(T::class, qualifier)) as T
}

inline fun <reified T : Any> Scope.getSetInstance(qualifier: Any = Unit): Set<T> {
    @Suppress("UNCHECKED_CAST")
    return getInstance(BindingKey(T::class, qualifier, set = true)) as Set<T>
}
