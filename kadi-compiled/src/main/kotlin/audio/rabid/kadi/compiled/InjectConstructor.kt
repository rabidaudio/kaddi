package audio.rabid.kadi.compiled

// if on the class we will require a single public constructor
// otherwise it will have to be on a single public constructor
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.CONSTRUCTOR)
annotation class InjectConstructor
