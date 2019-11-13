package audio.rabid.kadi.compiled

import audio.rabid.kadi.Provider
import kotlin.reflect.KClass

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Module(val importedModules: Array<KClass<*>> = [], val allowOverides: Boolean = false)

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class Named(val identifier: String)

// if on the class we will require a single public constructor
// otherwise it will have to be on a single public constructor
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class InjectConstructor

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS) // TODO class or  binding or both?
annotation class Singleton

class LambdaProvider<T : Any>(private val lambda: () -> T) : Provider<T> {
    override fun get(): T = lambda.invoke()
}

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
interface OtherModule {

    @Named("foo")
    fun fooString(): String = "foo"
}

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
