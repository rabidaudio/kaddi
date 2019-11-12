package audio.rabid.kadi

open class Module internal constructor(
    val name: String,
    val allowOverrides: Boolean
) {

    private val importedModules = mutableSetOf<Module>()
    private val bindings = mutableListOf<Binding<*>>()

    internal fun getBindings(): List<Binding<*>> = bindings
    internal fun getImportedModules(): Set<Module> = importedModules

    internal fun require(module: Module) {
        importedModules.add(module)
    }

    internal fun addBinding(binding: Binding<*>) {
        bindings.add(binding)
    }

    override fun toString(): String {
        return "Module($name)"
    }
}

internal val Module.scope get() = Kadi.getScopeForModule(this)
