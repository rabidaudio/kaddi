package audio.rabid.kadi.compiled

import audio.rabid.kadi.Provider

class LambdaProvider<T : Any>(private val lambda: () -> T) : Provider<T> {
    override fun get(): T = lambda.invoke()
}
