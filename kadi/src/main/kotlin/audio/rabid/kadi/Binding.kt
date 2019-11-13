package audio.rabid.kadi

class Binding<T: Any>(
    val key: BindingKey<T>,
    val overrides: Boolean,
    val provider: Provider<T>
) {

    fun copy(): Binding<T> {
        // TODO  might be a cleaner way to handle this than using SingletonProvider
        val copiedProvider = (provider as? SingletonProvider<T>)?.copy() ?: provider
        return Binding(key, overrides, copiedProvider)
    }
}
