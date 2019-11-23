package audio.rabid.kaddi

interface KaddiModule : Module {
    fun getBindings(): List<Binding<*>>
    fun getImportedModules(): Set<KaddiModule>
    fun onAttachedToScope(scope: Scope)
    fun copy(): KaddiModule
}
