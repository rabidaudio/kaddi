package audio.rabid.kadi

object Kadi {
    internal val scopes = mutableMapOf<Any, ScopeImpl>()

    private var rootScope = ScopeImpl(Kadi, null)

    object RootScope : Scope {
        override fun <T : Any> get(key: BindingKey<T>): T = rootScope.get(key)

        override fun inject(receiver: Any) {
            rootScope.inject(receiver)
        }

        override fun createChildScope(identifier: Any, vararg modules: Module): Scope {
            return rootScope.createChildScope(identifier, *modules)
        }

        fun addModules(vararg modules: Module) {
            for (module in modules) {
                rootScope.addModule(module as KadiModule)
            }
        }

        override fun close() {
            synchronized(Kadi) {
                rootScope.close()
                rootScope = ScopeImpl(Kadi, null)
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
