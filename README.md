
# audio.rabid.kadi.Kadi

A caddy manages clubs (dependencies), giving the right one when it's needed, so the golfer (developer)
can focus on playing (coding).

audio.rabid.kadi.BindingKey - type and optional identifier that is used to find a dependency 
audio.rabid.kadi.Binding - link from a type to a dependency
audio.rabid.kadi.Module - a collection of bindings. You can either define all your bindings in a single module, or
    group related bindings together into a package
audio.rabid.kadi.Scope -


- deps are keyed by a class, and an optional object (e.g. a string, default unit)
- there can not be more than one binding with the same key within a scope tree (but two
    sibling scopes could have the same binding key)
- there are no nullable bindings. that's a bit of a smell, but if you must, wrap in Optional
- generic class keys are not supported, generics are erased. If you have a need for a generic type
    binding, make an interface to use, e.g. interface IntFoo : Foo<Int>
- singleton bindings will always be scoped to the highest scope that added the module. that avoids
    weirdness around what scope a binding ends up living in based on who calls it first
- scope identifiers must be unique. there could be two sibling versions of the "same scope"
    (the same parent tree and the same modules) as long as they have different identifiers
- bindings are copied from modules into the scope, so that singletons are unique to a scope, not a module

TODO

- [x] overrides
- [ ] set bindings
- [x] dsl
- [ ] constructor injection
- [ ] cache binding lookups
- [ ] scope annotations
- [x] scope closable
- [ ] thread safety performance
- [ ] scope-creation-time tree validation and scope validation?
- [ ] async dependencies?
- [ ] eager dependencies?
- [ ] onReady? (when do these fire - on a new scope?)
