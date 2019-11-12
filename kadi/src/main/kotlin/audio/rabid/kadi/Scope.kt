package audio.rabid.kadi

import androidx.annotation.CheckResult

interface Scope {
    @CheckResult
    fun <T: Any> get(key: BindingKey<T>): T

    fun inject(receiver: Any)

    fun createChildScope(identifier: Any, vararg modules: Module): ChildScope
}

interface ChildScope : Scope {
    fun close()
}

@CheckResult
inline fun <reified T: Any> Scope.get(identifier: Any = Unit): T {
    return get(BindingKey(T::class, identifier))
}
