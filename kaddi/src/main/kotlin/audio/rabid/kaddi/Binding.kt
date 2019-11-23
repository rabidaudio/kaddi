package audio.rabid.kaddi

data class Binding<T: Any>(
    val key: BindingKey<T>,
    val overrides: Boolean,
    val singleton: Boolean,
    val provider: Provider<T>
)
