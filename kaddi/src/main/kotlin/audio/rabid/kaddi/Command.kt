package audio.rabid.kaddi

sealed class Command {
    data class AddBinding(val binding: Binding<*>) : Command()
    data class ImportModule(val module: KaddiModule) : Command()
}
