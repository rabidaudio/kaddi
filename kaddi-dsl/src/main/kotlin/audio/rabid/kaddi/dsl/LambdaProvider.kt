package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.Provider

internal class LambdaProvider<T : Any>(
        val providerBlock: ProviderBlock,
        private val lambda: ProviderBlock.() -> T
) : Provider<T> {
    override fun get(): T = lambda.invoke(providerBlock)

    internal fun withNewBlock(newProviderBlock: ProviderBlock): LambdaProvider<T>
            = LambdaProvider(newProviderBlock, lambda)
}
