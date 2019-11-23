# Kaddi

A caddy manages clubs (dependencies), providing the right one at the right time, so the golfer (developer)
can focus on golf (coding). Kaddi is another DI library for Kotlin. It could stand for
[K]otlin [A]n[d]roid [D]ependency [I]njection, but it's usage is not limited to Android.

**NOTE** This is a work in progress. The DSL form should be usable now, but the compiled form will need a little
more work. As you can see below some planned features are still missing and the documentation is incomplete.

---

BindingKey - type and optional identifier that is used to find a dependency 
Binding - link from a type to a dependency
Module - a collection of bindings. You can either define all your bindings in a single module, or
    group related bindings together into a package
Scope -


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

## TODO

- [x] overrides
- [x] dsl
- [x] scope closable
- [ ] set bindings
- [ ] thread safety performance
- [ ] async dependencies?
- [ ] eager dependencies?
- [ ] onReady? (when do these fire - on a new scope?)
- docs
  - [ ] motivation
  - [ ] comparison
  - [ ] limitations
  - [ ] concepts
  - [ ] examples
  - [ ] get started
- compiled implementation
  - [ ] constructor injection
  - [ ] cache binding lookups
  - [ ] scope annotations
  - [ ] scope-creation-time tree validation and scope validation?
