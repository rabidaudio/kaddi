package audio.rabid.kaddi

import kotlin.reflect.KClass

interface Scope {
    fun <T: Any> getInstance(key: BindingKey<T>): T

    operator fun <T: Any> get(type: KClass<T>): T {
        return getInstance(BindingKey(type))
    }

    fun contains(key: BindingKey<*>): Boolean

    fun inject(receiver: Any)

    fun createChildScope(qualifier: Any, vararg modules: Module): Scope

    fun close()
}

inline fun <reified T: Any> Scope.getInstance(qualifier: Any = Unit): T {
    return getInstance(BindingKey(T::class, qualifier))
}

inline fun <reified T: Any> Scope.getSetInstance(qualifier: Any = Unit): Set<T> {
    return getInstance(BindingKey(T::class, qualifier, set = true)) as Set<T>
}
