package audio.rabid.kaddi

internal class ScopeImpl(
        private val key: Any,
        private val parentScope: ScopeImpl?
) : Scope {
    // the modules that were added in this scope (they were not already added in a parent scope)
    private val modules = mutableListOf<KaddiModule>()
    private val singletons = mutableMapOf<BindingKey<*>, Any>()

    // TODO this lookup could be cached
    private fun <T : Any> findBinding(bindingKey: BindingKey<T>): Binding<T>? {
        synchronized(Kaddi) {
            return findLocalBinding(bindingKey) ?: parentScope?.findBinding(bindingKey)
        }
    }

    private fun <T : Any> findLocalBinding(bindingKey: BindingKey<T>): Binding<T>? {
        synchronized(Kaddi) {
            for (module in modules) {
                for (binding in module.getBindings()) {
                    @Suppress("UNCHECKED_CAST")
                    if (binding.key == bindingKey) return binding as Binding<T>
                }
            }
            return null
        }
    }

    private fun verifyBinding(binding: Binding<*>, allowOverride: Boolean) {
        synchronized(Kaddi) {
            val existingBinding = findBinding(binding.key)
            if (existingBinding != null) {
                check(binding.overrides) {
                    """Duplicate binding ${binding.key} found. If you are trying to override an
                        |existing binding, set override = true on the binding (and make sure the
                        |module supports overrides)""".trimMargin()
                }
                check(allowOverride) {
                    """Binding ${binding.key} tried to override an existing binding but the module
                        |does not allow overrides.""".trimMargin()
                }
            } else {
                check(!binding.overrides) {
                    """Binding ${binding.key} marked as override but does not override an existing
                        |binding.""".trimMargin()
                }
            }
        }
    }

    private fun moduleAlreadyAvailable(module: Module): Boolean {
        synchronized(Kaddi) {
            return modules.any { it.name == module.name }
                    || (parentScope?.moduleAlreadyAvailable(module) ?: false)
        }
    }

    internal fun addModule(module: KaddiModule) {
        synchronized(Kaddi) {
            val moduleToAdd = module.copy()
            if (moduleAlreadyAvailable(moduleToAdd)) return
            for (binding in moduleToAdd.getBindings()) {
                verifyBinding(binding, moduleToAdd.allowOverrides)
            }
            for (importedModule in moduleToAdd.getImportedModules()) {
                addModule(importedModule)
            }
            modules.add(moduleToAdd).also {
                moduleToAdd.onAttachedToScope(this)
            }
        }
    }

    override fun createChildScope(identifier: Any, vararg modules: Module): Scope {
        synchronized(Kaddi) {
            check(!Kaddi.scopes.contains(identifier)) { "Scope for key $identifier already exists" }
            return ScopeImpl(identifier, this).apply {
                for (module in modules) {
                    addModule(module as KaddiModule)
                }
            }.also { Kaddi.scopes[identifier] = it }
        }
    }

    override fun <T : Any> get(key: BindingKey<T>): T {
        synchronized(Kaddi) {
            return getFromLocalBinding(key)
                    ?: parentScope?.get(key)
                    ?: throw IllegalStateException("$key not found in $this")
        }
    }

    // TODO need to deal with overrides. the code will get even more complicated so a redesign is probably in order
    private fun <T : Any> getFromLocalBinding(key: BindingKey<T>): T? {
        synchronized(Kaddi) {
            val binding = findLocalBinding(key) ?: return null
            if (!binding.singleton) return binding.provider.get()
            @Suppress("UNCHECKED_CAST")
            singletons[binding.key]?.let { return it as T }
            return binding.provider.get().also { singletons[binding.key] = it }
        }
    }

    override fun inject(receiver: Any) {
        InjectedProperty.inject(this, receiver)
    }

    override fun close() {
        synchronized(Kaddi) {
            for ((_, singleton) in singletons) {
                if (singleton is ScopeClosable) singleton.onScopeClose()
            }
            singletons.clear()
            modules.clear()
            Kaddi.scopes.remove(key)
        }
    }

    override fun toString(): String {
        return StringBuilder().apply {
            append("Scope")
            append('<')
            append(key)
            append('>')
        }.toString()
    }
}
