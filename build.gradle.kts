plugins {
    kotlin("jvm") version "1.9.23" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0" apply false
    `maven-publish`
}

allprojects {
    group = "com.near"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.0.1")
        debug.set(false)
        verbose.set(false)
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(true)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
            exclude("**/build.gradle.kts")
            include("**/src/**/*.kt")
        }
    }
}

subprojects {
    apply(plugin = "maven-publish")

    afterEvaluate {
        publishing {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])

                    pom {
                        name.set("NEAR JSON-RPC Kotlin SDK")
                        description.set("Kotlin SDK for NEAR Protocol JSON-RPC API")
                        url.set("https://github.com/near/near-jsonrpc-kotlin")

                        licenses {
                            license {
                                name.set("Apache License 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                            }
                        }

                        developers {
                            developer {
                                id.set("near")
                                name.set("NEAR Protocol")
                                organization.set("NEAR Protocol")
                                organizationUrl.set("https://near.org")
                            }
                        }

                        scm {
                            connection.set("scm:git:git://github.com/near/near-jsonrpc-kotlin.git")
                            developerConnection.set("scm:git:ssh://github.com/near/near-jsonrpc-kotlin.git")
                            url.set("https://github.com/near/near-jsonrpc-kotlin")
                        }
                    }
                }
            }
        }
    }
}
