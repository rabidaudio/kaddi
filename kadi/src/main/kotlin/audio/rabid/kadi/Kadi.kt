package audio.rabid.kadi

import androidx.annotation.CheckResult
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

object Kadi : Scope {

    private var rootScope = ScopeImpl(Kadi, null)
    internal val scopes = mutableMapOf<Any, ScopeImpl>()

    @CheckResult
    fun getScope(identifier: Any): ChildScope {
        return scopes[identifier]
            ?: throw IllegalStateException("Tried to get non-existent scope $identifier")
    }

    override fun createChildScope(identifier: Any, vararg modules: Module): ChildScope {
        return rootScope.createChildScope(identifier, *modules)
    }

    fun addRootModules(vararg modules: Module): Kadi {
        for (module in modules) {
            rootScope.addModule(module)
        }
        return this
    }

    override fun <T : Any> get(key: BindingKey<T>): T {
        return rootScope.get(key)
    }

    override fun inject(receiver: Any) {
        rootScope.inject(receiver)
    }

    // TODO this lookup could be cached
    internal fun getScopeForModule(module: Module): Scope {
        if (rootScope.containsModule(module)) return rootScope
        for ((_, scope) in scopes) {
            if (scope.containsModule(module)) return scope
        }
        throw IllegalStateException("$module not found in any scopes")
    }

    fun closeScope(identifier: Any) {
        synchronized(Kadi) {
            getScope(identifier).close()
        }
    }

    // TODO should this be a public feature if we want to expose the Root Scope?
    fun reset() {
        rootScope.close()
        rootScope = ScopeImpl(Kadi, null)
    }

//    @CheckResult
//    fun openScope(vararg scopeIdentifiers: Any): ScopeBuilder {
//        check(scopeIdentifiers.isNotEmpty()) { "Must specify at least one scope identifier" }
//        for (i in 0 until scopeIdentifiers.size - 1) {
//            val scopeIdentifier = scopeIdentifiers[i]
//            val scope = scopes[scopeIdentifier]
//                ?: throw IllegalStateException("audio.rabid.kadi.Scope not found for key $scopeIdentifier")
////            if (i == 0 && scope == RootScope) continue
////            check(scope != RootScope) { "RootScope must only be used at the root" }
//            check((i == 0 && scope.parentScope == null)
//                    || scope.parentScope == (scopes[scopeIdentifiers[i - 1]] as audio.rabid.kadi.ScopeImpl)) {
//                "Invalid parent scope hierarchy"
//            }
//        }
////        val parentScopeIdentifiers = listOf(RootScope) + scopeIdentifiers.dropLast(1)
//        val thisIdentifier = scopeIdentifiers.last()
//        check(!scopes.contains(thisIdentifier)) {
//            "audio.rabid.kadi.Scope $thisIdentifier already defined"
//        }
//        val parentIdentifier = if (scopeIdentifiers.size > 1) scopeIdentifiers[scopeIdentifiers.lastIndex - 1] else null
//        val parentScope = scopes[parentIdentifier]
//        val scope = audio.rabid.kadi.ScopeImpl(thisIdentifier, parentScope)
//        scopes[thisIdentifier] = scope
//
//        return ScopeBuilder(scope)
//    }

//    private fun scopeIdentifierTree(scopeIdentifier: Any): List<Any> {
//        val scope = scopes[scopeIdentifier] ?: return emptyList()
//        scope.parentScope?.let { listOf(scope) + scopeIdentifierTree(it) } ?: listOf(scopeIdentifier)
//    }
}
