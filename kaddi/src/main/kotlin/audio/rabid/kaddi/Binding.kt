package audio.rabid.kaddi

sealed class Binding<T : Any> {
    abstract val key: BindingKey<*>

    data class Basic<T : Any>(
            override val key: BindingKey<T>,
            val overrides: Boolean,
            val singleton: Boolean,
            val intoSet: Boolean,
            val provider: Provider<T>
    ) : Binding<T>() {

        /**
         * Does this binding override other
         */
//        fun overrides(other: Binding<T>): Boolean {
//            if (!overrides) return false
//            return matches(other)
//        }
    }

    data class Set<T : Any>(override val key: BindingKey<T>) : Binding<T>()

    /**
     * Is this binding the same as another, for the purposes of detecting duplicate bindings
     */
//    fun matches(other: Binding<*>): Boolean {
//        if (key != other.key) return false
//        when (this) {
//            is Basic -> {
//                if (other !is Basic) return false
//                // two bindings can have the same arguments if they are into a set
//                if (intoSet && other.intoSet) return false
//                return true
//            }
//            is Set -> return other is Set
//        }
//    }
}

