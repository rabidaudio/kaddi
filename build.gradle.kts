// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.set("kotlin_version", "1.3.50")
    extra.set("room_version", "2.2.1")

    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
    }

    dependencies {
        classpath(kotlin("gradle-plugin"))
        classpath("com.android.tools.build:gradle:3.5.2")
        classpath("com.airbnb.okreplay:gradle-plugin:1.5.0")
        classpath("com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.5")
        classpath("de.mannodermaus.gradle.plugins:android-junit5:1.5.1.0")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
    }
}

task("clean", Delete::class) {
    delete = setOf(rootProject.buildDir)
}
