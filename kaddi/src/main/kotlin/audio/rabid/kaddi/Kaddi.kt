package audio.rabid.kaddi

object Kaddi {
    internal val scopes = mutableMapOf<Any, ScopeImpl>()

    private val rootScope = ScopeImpl(Kaddi, null)

    object RootScope : Scope by rootScope {

        internal val implementation: ScopeImpl get() = rootScope

        fun addModules(vararg modules: Module) {
            for (module in modules) {
                rootScope.addModule(module as KaddiModule)
            }
        }

        operator fun plusAssign(module: Module) {
            addModules(module)
        }
    }

    fun getScope(identifier: Any): Scope {
        return scopes[identifier]
                ?: throw IllegalStateException("Tried to get non-existent scope $identifier")
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun createScope(identifier: Any, vararg modules: Module): Scope {
        return RootScope.createChildScope(identifier, *modules)
    }

    fun closeScope(identifier: Any) {
        synchronized(Kaddi) {
            getScope(identifier).close()
        }
    }
}
