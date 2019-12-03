package audio.rabid.kaddi

sealed class Binding<T : Any> {
    abstract val key: BindingKey<*>

    data class Basic<T : Any>(
            override val key: BindingKey<T>,
            val overrides: Boolean,
            val singleton: Boolean,
            val intoSet: Boolean,
            val provider: Provider<T>
    ) : Binding<T>()

    data class Set<T : Any>(override val key: BindingKey<T>) : Binding<T>()

    internal fun assignDependencyProvider(dependencyProvider: DependencyProvider): Binding<T> {
        return when (this) {
            is Basic -> {
                if (provider !is DependantInstanceProvider) return this
                copy(provider = provider.assignDependencyProvider(dependencyProvider))
            }
            is Set -> this
        }
    }
}
