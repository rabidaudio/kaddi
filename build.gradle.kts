import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.3.50"))
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.gradle.jacoco")

    extensions.configure<JacocoPluginExtension>("jacoco") {
        toolVersion = "0.8.2"
    }

    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))

        "testImplementation"("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
        "testImplementation"("org.spekframework.spek2:spek-runner-junit5:2.0.8")
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.4.1")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.4.1")

        "testImplementation"("com.winterbe:expekt:0.5.0")
        if (name != "testutils") {
            "testImplementation"(project(":testutils"))
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform {
            includeEngines("spek2")
            testLogging {
                events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
                debug {
                    events = TestLogEvent.values().toSet()
                    exceptionFormat = TestExceptionFormat.FULL
                }
            }
        }
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}
