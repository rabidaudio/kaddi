package audio.rabid.kaddi

import kotlin.reflect.KClass

interface DependencyProvider {
    operator fun <T : Any> get(type: KClass<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BindingKey(type).getInstanceFromScope(this) as T
    }

    fun contains(key: BindingKey<*>): Boolean
}

inline fun <reified T : Any> DependencyProvider.instance(qualifier: Any = Unit): T {
    return BindingKey(T::class, qualifier).getInstanceFromScope(this) as T
}

inline fun <reified T : Any> DependencyProvider.setInstance(qualifier: Any = Unit): Set<T> {
    @Suppress("UNCHECKED_CAST")
    return BindingKey(T::class, qualifier, set = true).getInstanceFromScope(this) as Set<T>
}
