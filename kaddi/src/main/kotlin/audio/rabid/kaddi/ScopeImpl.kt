package audio.rabid.kaddi

// This is the core logic. Needs to handle:
// singletons
// set bindings
// overrides
// searching up the module tree
//  all in a way that doesn't cause you to pull your hair out

// Basic binding:
//  if this or any parent defines the binding already, error
//  search this module, followed by each parent module until the binding is found, or throw if not found
//  call provider
// Singleton binding:
//  if this or any parent defines the binding already, error
//  search this module, followed by each parent module until the binding is found, or throw if not found
//  return cached value or call provider and cache
// Override:
//   overrides are only allowed if both the original binding and override are at the same scope
//   if this scope does not have the original binding, error
//   if the original binding is in a higher scope, error
//   if the binding is a set, error
//   match overridden binding, but keep original binding in case we support accessing the original in the future
//   same logic for basic/singletons
//   what happens when you override a set binding?
// Set:
//   set elements are only allowed to be added to the scope the set binding was defined (like overrides)
//   if the set container is not declared, error
//   if the set container is declared in a higher scope, error
internal class ScopeImpl(
        private val key: Any,
        private val parentScope: ScopeImpl?
) : Scope {
    // the modules that were added in this scope (they were not already added in a parent scope)
    private val modules = mutableListOf<KaddiModule>()

    // two versions of the same binding object can exist if they are both inSet

    // all bindings defined in the scope
    private val allBindings = mutableListOf<Binding<*>>()
    // a mapping of binding keys to the binding to use to resolve it
    private val bindingTable = mutableMapOf<BindingKey<*>, Binding<*>>()
    // a mapping of set binding definitions to the bindings supplying the elements
    private val setBindings = mutableMapOf<BindingKey<*>, MutableList<Binding.Basic<*>>>()
    // a mapping of a singleton binding to it's cached value
    private val singletons = mutableMapOf<Binding.Basic<*>, Any>()

    private fun addBinding(binding: Binding<*>) {
        when (binding) {
            is Binding.Basic -> {
                check(!(binding.intoSet && binding.overrides)) {
                    "Binding for ${binding.key} cannot override into a set"
                }
                when {
                    binding.intoSet -> {
                        // expect set binding defined here
                        check(!parentContainsSetBindingFor(binding.key)) {
                            "cannot add binding for ${binding.key} to set binding of parent scope"
                        }
                        check(localContainsSetBindingFor(binding.key)) {
                            "$this does not contain set binding for ${binding.key}"
                        }
                        // at set binding element binding
                        setBindings[binding.key]!!.add(binding)
                    }
                    binding.overrides -> {
                        // parent should not contain original binding
                        check(!parentContainsSetBindingFor(binding.key)) {
                            "Binding for ${binding.key} in scope $this cannot override a binding that was " +
                                    "defined in a higher scope"
                        }
                        // current scope should contain original binding
                        check(allBindings.any { it.key == binding.key && it is Binding.Basic && !it.overrides }) {
                            "Binding ${binding.key} marked as override but does not override an existing binding."
                        }
                        // replace table entry
                        bindingTable[binding.key] = binding
                    }
                    else -> {
                        check(!localContainsSetBindingFor(binding.key)) {
                            """Duplicate binding ${binding.key} found in scope $this. If you are trying to override an
                                    |existing binding, set override = true on the binding (and make sure the
                                    |module supports overrides)""".trimMargin()
                        }
                        check(!parentContainsSetBindingFor(binding.key)) {
                            """Duplicate binding ${binding.key} found in parent scope of $this. If you are trying to
                                    |override an  existing binding, set override = true on the binding (and make sure the
                                    |module supports overrides)""".trimMargin()
                        }
                        // add table entry
                        bindingTable[binding.key] = binding
                    }
                }
            }
            is Binding.Set -> {
                check(!localContainsSetBindingFor(binding.key)) {
                    """Duplicate binding ${binding.key} found in scope $this. If you are trying to override an
                            |existing binding, set override = true on the binding (and make sure the
                            |module supports overrides)""".trimMargin()
                }
                check(!parentContainsSetBindingFor(binding.key)) {
                    """Duplicate binding ${binding.key} found in parent scope of $this. If you are trying to
                            |override an  existing binding, set override = true on the binding (and make sure the
                            |module supports overrides)""".trimMargin()
                }
                bindingTable[binding.key] = binding
                setBindings[binding.key] = mutableListOf()
            }
        }
        allBindings.add(binding)
    }

    private fun moduleAlreadyAvailable(module: Module): Boolean {
        synchronized(Kaddi) {
            return modules.any { it.name == module.name }
                    || (parentScope?.moduleAlreadyAvailable(module) ?: false)
        }
    }

    internal fun addModule(module: KaddiModule) {
        synchronized(Kaddi) {
            if (moduleAlreadyAvailable(module)) return
            modules.add(module)
            for (command in module.getCommands()) {
                when (command) {
                    is Command.AddBinding -> addBinding(command.binding.assignDependencyProvider(this))
                    is Command.ImportModule -> addModule(command.module)
                }
            }
            // TODO should we wait to call onAttached callbacks on dependant modules until after all the child modules
            // have been added?
            module.onAttachedToScope(this)
        }
    }

    override fun createChildScope(qualifier: Any, vararg modules: Module): Scope {
        synchronized(Kaddi) {
            check(!Kaddi.scopes.contains(qualifier)) { "Scope for key $qualifier already exists" }
            return ScopeImpl(qualifier, this).apply {
                for (module in modules) {
                    addModule(module as KaddiModule)
                }
            }.also { Kaddi.scopes[qualifier] = it }
        }
    }

    internal fun getInstance(key: BindingKey<*>): Any {
        synchronized(Kaddi) {
            val localBinding = bindingTable[key]
                    ?: return parentScope?.getInstance(key)
                            ?: throw IllegalStateException("$key not found in $this or any parent scope")

            return when (localBinding) {
                is Binding.Set -> {
                    val set = mutableSetOf<Any>()
                    for (elementBinding in setBindings[localBinding.key]!!) {
                        set.add(elementBinding.getInstance())
                    }
                    return set
                }
                is Binding.Basic -> localBinding.getInstance()
            }
        }
    }

    private fun <T : Any> Binding.Basic<T>.getInstance(): T {
        if (!singleton) return provider.get()
        synchronized(Kaddi) {
            @Suppress("UNCHECKED_CAST")
            singletons[this]?.let { return it as T }
            return provider.get().also { singletons[this] = it }
        }
    }

    private fun localContainsSetBindingFor(bindingKey: BindingKey<*>): Boolean {
        return allBindings.any { it is Binding.Set && it.key == bindingKey }
    }

    private fun parentContainsSetBindingFor(bindingKey: BindingKey<*>): Boolean {
        if (parentScope == null) return false
        return parentScope.localContainsSetBindingFor(bindingKey) || parentScope.parentContainsSetBindingFor(bindingKey)
    }

    override fun contains(key: BindingKey<*>): Boolean {
        return bindingTable.containsKey(key) || parentScope?.contains(key) ?: false
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
            setBindings.clear()
            bindingTable.clear()
            allBindings.clear()
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
