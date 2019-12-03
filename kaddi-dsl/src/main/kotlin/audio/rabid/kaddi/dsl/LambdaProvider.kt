package audio.rabid.kaddi.dsl

import audio.rabid.kaddi.DependantInstanceProvider
import audio.rabid.kaddi.DependencyProvider

class LambdaProvider<T : Any>(
        private val lambda: DependencyProvider.() -> T
) : DependantInstanceProvider<T>() {
    override fun get(dependencyProvider: DependencyProvider): T = lambda.invoke(dependencyProvider)

    override fun copy(): DependantInstanceProvider<T> = LambdaProvider(lambda)
}
