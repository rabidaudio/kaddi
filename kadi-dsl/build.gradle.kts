plugins {
    kotlin("jvm")
    jacoco
}

jacoco {
    toolVersion = "0.8.2"
}

dependencies {
    implementation(project(":kadi"))
    implementation(kotlin("stdlib-jdk8"))
    implementation("androidx.annotation:annotation:1.1.0")

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
    testImplementation("org.spekframework.spek2:spek-runner-junit5:2.0.8")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.1")

    testImplementation("com.winterbe:expekt:0.5.0")
}
