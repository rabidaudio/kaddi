package audio.rabid.kadi.compiled

import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Module(val importedModules: Array<KClass<*>> = [], val allowOverrides: Boolean = false)

// TODO this reflection will only work on JVM
// TODO cache this reflection for performance
fun <T: Any> findGeneratedModule(moduleClass: KClass<T>): audio.rabid.kadi.Module {
    val packageName = moduleClass.java.canonicalName.removeSuffix(moduleClass.java.simpleName)
    val targetClassName = packageName + "Generated${moduleClass.java.simpleName}"
    val targetClass = moduleClass.java.classLoader.loadClass(targetClassName)
    return targetClass.newInstance() as audio.rabid.kadi.Module
}

inline fun <reified T: Any> generatedModule(): audio.rabid.kadi.Module = findGeneratedModule(T::class)

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
//@Module(importedModules = [OtherModule::class])
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
//@Module
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
//class GeneratedOtherModule : Module("OtherModule", allowOverrides = false), OtherModule {
//
//
//    init {
//        addBinding(Binding(BindingKey(String::class, "foo"), false, LambdaProvider2(this::fooString)))
//    }
//}
//
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
//Kadi.getScope(application)
//        .createChildScope(this, generatedModule<MyModule>())