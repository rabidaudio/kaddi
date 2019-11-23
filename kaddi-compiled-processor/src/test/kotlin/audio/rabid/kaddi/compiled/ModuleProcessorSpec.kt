package audio.rabid.kaddi.compiled

import audio.rabid.kaddi.compiled.ModuleProcessor.Companion.GENERATED_SOURCES_DIR_KEY
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.*
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.winterbe.expekt.expect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.io.File

private val temporaryFolder = File("build/test/tmp")

class ModuleProcessorSpec : Spek({

    beforeEachTest {
        temporaryFolder.deleteRecursively()
        temporaryFolder.mkdirs()
    }

    afterEachTest {
        temporaryFolder.deleteRecursively()
    }

    describe("ModuleProcessor") {

        xit("should compile a module") {
            val result = compile(kotlin("source.kt", """
                package com.example.app
                
                import audio.rabid.kaddi.compiled.Module
                import audio.rabid.kaddi.compiled.Named
                
                class Foo(val name: String)
                
                @Module
                interface FooModule {
                    fun foo(@Named("myString") myString: String): Foo {
                        return Foo(myString) 
                    }
                    
                    @Named("myString")
                    fun myString(): String
                }
            """))
        }

        it("should only allow interfaces") {
            val result = compile(kotlin("source.kt", """
                import audio.rabid.kaddi.compiled.Module
                
                @Module
                class Foo
            """))
            expect(result.messages).contain("@Module annotation can only be used on interfaces")
            expect(result.exitCode).not.to.equal(ExitCode.OK)
        }
    }
})

private fun compile(vararg sourceFiles: SourceFile): Result {
    return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder
                annotationProcessors = listOf(ModuleProcessor())
                inheritClassPath = true
                sources = sourceFiles.toList()
                verbose = false
                kaptArgs[GENERATED_SOURCES_DIR_KEY] = temporaryFolder.absolutePath
            }.compile()
}
