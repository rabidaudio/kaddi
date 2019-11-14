package audio.rabid.kadi.dsl

import audio.rabid.kadi.Provider

internal class LambdaProvider<T : Any>(
        private val providerBlock: ProviderBlock,
        private val lambda: ProviderBlock.() -> T
) : Provider<T> {
    override fun get(): T = lambda.invoke(providerBlock)

    internal fun withNewBlock(providerBlock: ProviderBlock): LambdaProvider<T> = LambdaProvider(providerBlock, lambda)
}
