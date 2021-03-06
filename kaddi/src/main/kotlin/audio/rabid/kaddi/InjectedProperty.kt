package audio.rabid.kaddi

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class InjectedProperty<T : Any> private constructor(private val bindingKey: BindingKey<T>) {

    companion object {
        fun <T : Any> create(owner: Any, bindingKey: BindingKey<T>): InjectedProperty<T> {
            return InjectedProperty(bindingKey).also { addInjectedDelegate(owner, it) }
        }

        private val injectedDelegateListeners = mutableListOf<Pair<WeakReference<Any>, InjectedProperty<*>>>()

        private fun addInjectedDelegate(owner: Any, property: InjectedProperty<*>) {
            synchronized(Kaddi) {
                injectedDelegateListeners.add(WeakReference(owner) to property)
            }
        }

        internal fun inject(scope: Scope, receiver: Any) {
            synchronized(Kaddi) {
                val iterator = injectedDelegateListeners.iterator()
                while (iterator.hasNext()) {
                    val (ref, prop) = iterator.next()
                    if (ref.get() == receiver) {
                        prop.injectValue(scope)
                        iterator.remove()
                    }
                }
            }
        }
    }

    private lateinit var value: T

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    @Suppress("UNCHECKED_CAST")
    internal fun injectValue(scope: Scope) {
        value = bindingKey.getInstanceFromScope(scope) as T
    }
}

inline fun <reified T : Any> Any.inject(identifier: Any = Unit): InjectedProperty<T> {
    return InjectedProperty.create(this, BindingKey(T::class, identifier))
}
