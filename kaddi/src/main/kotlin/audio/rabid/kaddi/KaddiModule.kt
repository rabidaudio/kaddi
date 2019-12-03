package audio.rabid.kaddi

interface KaddiModule : Module {
    fun getCommands(): List<Command>
    fun onAttachedToScope(scope: Scope)
}
