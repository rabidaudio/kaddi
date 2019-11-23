package audio.rabid.kaddi.compiled

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

class ModuleProcessor : AbstractProcessor() {

    companion object {
        const val GENERATED_SOURCES_DIR_KEY = "kapt.kotlin.generated"
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val generatedSourcesDir = processingEnv.options[GENERATED_SOURCES_DIR_KEY]
                ?: return error("No directory for generated Kotlin files")
        val moduleClasses = roundEnv.getElementsAnnotatedWith(Module::class.java)
        for (moduleClass in moduleClasses) {
            if (moduleClass.kind != ElementKind.INTERFACE) {
                return error("@Module annotation can only be used on interfaces")
            }
//            ElementFilter.methodsIn(moduleClass)
//            processingEnv.typeUtils.asElement()
            moduleClass as TypeElement
            val pkg = moduleClass.qualifiedName.removeSuffix(moduleClass.simpleName).removeSuffix(".").toString()
            val className = "Generated${moduleClass.simpleName}"
//            val generatedClass = ClassName(pkg, className)
            val generatedClass = FileSpec.builder(pkg, className)
                    .addType(
                            TypeSpec.classBuilder(className)
                                    .primaryConstructor(FunSpec.constructorBuilder().build())
//                                    .addFunction()
                                    .build()
                    )
                    .build()
            val file = File(generatedSourcesDir).apply { mkdirs() }
            generatedClass.writeTo(file)
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(Module::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    private fun error(message: String): Boolean {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
        return false
    }
}
