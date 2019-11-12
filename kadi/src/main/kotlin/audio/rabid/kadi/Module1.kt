package audio.rabid.kadi

import kotlin.reflect.KClass

annotation class Module1(val importedModules: Array<KClass<*>> = [], val allowOverides: Boolean = false)

annotation class Named(val identifier: String)

annotation class InjectConstructor

annotation class Singleton

class LambdaProvider2<T : Any>(private val lambda: () -> T) : Provider<T> {
    override fun get(): T = lambda.invoke()
}

// example

//@InjectConstructor
//class Foo(@Named("foo") foo: String)
//
//
//interface IComplicatedThing
//
////@Singleton // TODO should this be allowed?
//class ComplicatedThing : IComplicatedThing {
//
//    private var foo: Foo? = null
//
//    companion object {
//        fun create(foo: Foo): ComplicatedThing {
//            return ComplicatedThing().also { it.foo = foo }
//        }
//    }
//}
//
//@Module1(importedModules = [OtherModule::class])
//interface MyModule {
//
//    // an interface method is a shortcut to defer to @InjectConstructor
//    fun foo(): Foo
//
//    @Singleton
//    fun complicatedThing(foo: Foo): IComplicatedThing {
//        return ComplicatedThing.create(foo)
//    }
//}
//
//@Module1
//interface OtherModule {
//
//    @Named("foo")
//    fun fooString(): String = "foo"
//}

/*
Tree:
    MyModule(
        [String("foo")] => Foo,
        [Foo] => IComplicatedThing
    )
    OtherModule(
        [] => String("foo")
    )
*/

//
//class OtherModuleImpl : Module("OtherModule", allowOverrides = false), OtherModule {
//
//
//    init {
//        addBinding(Binding(BindingKey(String::class, "foo"), false, LambdaProvider2(this::fooString)))
//    }
//}
//
//class MyModuleImpl : Module("MyModule", allowOverrides = false), MyModule {
//
//    init {
//        require(OtherModuleImpl())
//        addBinding(Binding(BindingKey(Foo::class), false, LambdaProvider2(this::foo)))
//        addBinding(Binding(BindingKey(IComplicatedThing::class), false, LambdaProvider2(this::complicatedThing)))
//    }
//
//    override fun foo(): Foo {
//        return Foo(foo = scope.get<String>("foo"))
//    }
//
//    fun complicatedThing(): IComplicatedThing {
//        return complicatedThing(foo = scope.get<Foo>())
//    }
//}
