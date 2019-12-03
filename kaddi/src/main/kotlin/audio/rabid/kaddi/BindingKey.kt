package audio.rabid.kaddi

import kotlin.reflect.KClass

data class BindingKey<T : Any>(val type: KClass<T>, val qualifier: Any = Unit, val set: Boolean = false) {

    // by inverting the arguments from DependencyProvider.get(BindingKey) to BindingKey.get(DependencyProvider), we
    // discourage use of the lower-level api
    fun getInstanceFromScope(dependencyProvider: DependencyProvider): Any {
        val scope = (dependencyProvider as? Kaddi.RootScope)?.implementation ?: dependencyProvider as ScopeImpl
        return scope.getInstance(this)
    }

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
