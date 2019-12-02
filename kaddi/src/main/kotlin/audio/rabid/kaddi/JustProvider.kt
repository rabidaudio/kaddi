package audio.rabid.kaddi

class JustProvider<T : Any>(private val value: T) : Provider<T> {
    override fun get(): T = value
}
