plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    application
}

dependencies {
    implementation(project(":types"))
    implementation(project(":client"))

    implementation("io.ktor:ktor-client-core:2.3.8")
    implementation("io.ktor:ktor-client-cio:2.3.8")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

application {
    mainClass.set("org.near.jsonrpc.example.NearJsonRpcExampleKt")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
