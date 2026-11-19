import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    kotlin("jvm") version "2.4.21"
}

group = "vadiole"

val lint = "32.0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
        apiVersion.set(KotlinVersion.KOTLIN_2_2)
    }
}

dependencies {
    compileOnly("com.android.tools.lint:lint-api:$lint")
    compileOnly("com.android.tools.lint:lint-checks:$lint")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    testImplementation("com.android.tools.lint:lint:$lint")
    testImplementation("com.android.tools.lint:lint-tests:$lint")
    testImplementation("junit:junit:4.13.2")
}

tasks.jar {
    manifest {
        attributes("Lint-Registry-v2" to "vadiole.lint.Registry")
    }
}
