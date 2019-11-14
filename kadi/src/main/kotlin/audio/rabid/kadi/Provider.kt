package audio.rabid.kadi

interface Provider<T: Any> {
    fun get(): T
}
