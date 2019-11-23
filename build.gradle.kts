import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.60"
    jacoco
    id("com.github.johnrengelman.shadow") version "5.2.0" apply false
    `maven-publish`
}

allprojects {
    repositories {
        google()
        jcenter()
    }
    group = "audio.rabid.kaddi"
    version = "0.0.1"
}
jacoco {
    toolVersion = "0.8.2"
}

subprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("maven-publish")
        plugin("jacoco")
        plugin("com.github.johnrengelman.shadow")
    }

    sourceSets.main.get().java.srcDirs("src/main/kotlin")

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform {
            includeEngines("spek2")
            systemProperty("SPEK_TIMEOUT", 0) // disable test timeout
            testLogging {
                showExceptions = true
                showCauses = true
                showStackTraces = true
                events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
                exceptionFormat = TestExceptionFormat.FULL
            }
        }
    }

    val sourcesJar by tasks.creating(Jar::class) {
        dependsOn(tasks.classes)
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by tasks.creating(Jar::class) {
        from(tasks.javadoc)
        archiveClassifier.set("javadoc")
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
    }

    publishing {
        publications.create<MavenPublication>("kaddiPublication") {
            from(components["java"])
            artifact(sourcesJar)
            artifact(javadocJar)
            artifact(tasks.getByName("shadowJar"))
            groupId = this@subprojects.group as? String
            artifactId = this@subprojects.name
            version = this@subprojects.version as? String
            pom {
                description.set("Simple scoping dependency injection for Kotlin")
                name.set("kaddi")
                url.set("https://github.com/rabidudio/kaddi")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("Charles Julian Knight")
                        name.set("Charles Julian Knight")
                        email.set("charles@rabidaudio.com")
                        url.set("https://twitter.com/charlesjuliank")
                    }
                }
                scm {
                    url.set("https://github.com/rabidudio/kaddi")
                }
            }
        }
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        compileOnly("org.jetbrains:annotations:18.0.0")
        testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
        testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.8")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.1")
        testImplementation("com.winterbe:expekt:0.5.0")
    }
}
