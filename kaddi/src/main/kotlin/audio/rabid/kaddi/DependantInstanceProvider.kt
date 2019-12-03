package audio.rabid.kaddi

abstract class DependantInstanceProvider<T : Any> : Provider<T> {

    private lateinit var dependencyProvider: DependencyProvider

    abstract fun get(dependencyProvider: DependencyProvider): T

    final override fun get(): T = get(dependencyProvider)

    abstract fun copy(): DependantInstanceProvider<T>

    internal fun assignDependencyProvider(dependencyProvider: DependencyProvider): Provider<T> {
        return copy().also { it.dependencyProvider = dependencyProvider }
    }
}
