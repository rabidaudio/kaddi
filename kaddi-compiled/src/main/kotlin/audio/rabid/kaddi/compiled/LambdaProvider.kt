package audio.rabid.kaddi.compiled

import audio.rabid.kaddi.Provider

class LambdaProvider<T : Any>(private val lambda: () -> T) : Provider<T> {
    override fun get(): T = lambda.invoke()
}
