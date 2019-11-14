plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":kadi"))
    implementation("com.squareup:kotlinpoet:1.4.3")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.4")
}
