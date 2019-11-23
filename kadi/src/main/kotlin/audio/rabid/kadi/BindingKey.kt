package audio.rabid.kadi

import kotlin.reflect.KClass

data class BindingKey<T: Any>(val type: KClass<T>, val qualifier: Any = Unit) {

    override fun toString(): String {
        return StringBuilder().apply {
            append("Binding")
            append('<')
            append(type.java.canonicalName)
            append('>')
            if (qualifier != Unit) {
                append('(')
                append(qualifier)
                append(')')
            }
        }.toString()
    }
}
