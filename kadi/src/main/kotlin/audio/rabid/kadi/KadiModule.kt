package audio.rabid.kadi

interface KadiModule : Module {
    fun getBindings(): List<Binding<*>>
    fun getImportedModules(): Set<KadiModule>
    fun onAttachedToScope(scope: Scope)
    fun copy(): KadiModule
}
