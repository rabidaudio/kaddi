package audio.rabid.kaddi

interface Provider<T : Any> {
    fun get(): T
}
