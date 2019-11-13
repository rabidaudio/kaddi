plugins {
    kotlin("kapt")
}

dependencies {
    implementation(project(":kadi"))
    implementation("androidx.annotation:annotation:1.1.0")
    implementation("com.squareup:kotlinpoet:1.4.3")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.2.4")
}
