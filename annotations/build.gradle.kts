import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "11"

        freeCompilerArgs = listOfNotNull(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-Xexplicit-api=strict".takeUnless { "Test" in name }
        )
    }
}
