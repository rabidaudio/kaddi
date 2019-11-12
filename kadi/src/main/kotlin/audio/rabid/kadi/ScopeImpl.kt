package audio.rabid.kadi

internal class ScopeImpl internal constructor(
    private val key: Any,
    private val parentScope: ScopeImpl?
) : ChildScope {
    // the modules that were added in this scope (they were not already added in a parent scope)
    private val modules = mutableListOf<Module>()
    // the bindings from the modules of this scope
    private val bindings = mutableMapOf<BindingKey<*>, Binding<*>>()

    // TODO this lookup could be cached
    private fun <T: Any> findBinding(bindingKey: BindingKey<T>): Binding<T>? {
        synchronized(Kadi) {
            val binding = bindings[bindingKey] ?: return parentScope?.findBinding(bindingKey)
            @Suppress("UNCHECKED_CAST")
            return binding as Binding<T>
        }
    }

    private fun verifyBinding(binding: Binding<*>, allowOverride: Boolean) {
        synchronized(Kadi) {
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

    // TODO if you construct multiple copies of the same module we're going to have a problem...
    //  this could be solved by annotation-processed modules
    fun containsModule(module: Module): Boolean = modules.contains(module)

    private fun moduleAlreadyAvailable(module: Module): Boolean {
        synchronized(Kadi) {
            return modules.any { it.name == module.name }
                    || (parentScope?.moduleAlreadyAvailable(module) ?: false)
        }
    }

    internal fun addModule(module: Module) {
        synchronized(Kadi) {
            if (moduleAlreadyAvailable(module)) return
            for (binding in module.getBindings()) {
                verifyBinding(binding, module.allowOverrides)
                bindings[binding.key] = binding.copy()
            }
            for (importedModule in module.getImportedModules()) {
                addModule(importedModule)
            }
            modules.add(module)
        }
    }

    override fun createChildScope(identifier: Any, vararg modules: Module): ChildScope {
        synchronized(Kadi) {
            check(!Kadi.scopes.contains(identifier)) { "Scope for key $identifier already exists" }
            return ScopeImpl(identifier, this).apply {
                for (module in modules) {
                    addModule(module)
                }
            }.also { Kadi.scopes[identifier] = it }
        }
    }

    override fun <T : Any> get(key: BindingKey<T>): T {
        synchronized(Kadi) {
            val binding = findBinding(key)
                ?: throw IllegalStateException("$key not found in $this")
            return binding.provider.get()
        }
    }

    override fun inject(receiver: Any) {
        InjectedProperty.inject(this, receiver)
    }

    override fun close() {
        synchronized(Kadi) {
            for ((_,binding) in bindings) {
                if (binding.provider is SingletonProvider) binding.provider.release()
            }
            Kadi.scopes.remove(key)
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
