package audio.rabid.kadi

interface Provider<T: Any> {
    fun get(): T
}

class JustProvider<T: Any>(private val value: T) : Provider<T> {
    override fun get(): T = value
}

class SingletonProvider<T: Any>(private val wrapped: Provider<T>): Provider<T> {
    private var value: T? = null

    fun release() {
        synchronized(this) {
            try {
                (value as? ScopeClosable)?.onScopeClose()
            } finally {
                value = null
            }
        }
    }

    override fun get(): T {
        synchronized(this) {
            return value ?: wrapped.get().also { value = it }
        }
    }

    fun copy(): SingletonProvider<T> = SingletonProvider(wrapped)
}
