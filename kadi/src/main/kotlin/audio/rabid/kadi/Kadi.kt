package audio.rabid.kadi

object Kadi {
    internal val scopes = mutableMapOf<Any, ScopeImpl>()

    private val rootScope = ScopeImpl(Kadi, null)

    object RootScope : Scope by rootScope {
        fun addModules(vararg modules: Module) {
            for (module in modules) {
                rootScope.addModule(module as KadiModule)
            }
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
        synchronized(Kadi) {
            getScope(identifier).close()
        }
    }
}
