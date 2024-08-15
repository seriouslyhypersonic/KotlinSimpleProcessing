import com.android.build.gradle.internal.utils.configureKotlinCompileTasks
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = "17"

        freeCompilerArgs = listOfNotNull(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xcontext-receivers",
            "-Xexplicit-api=strict".takeUnless { "Test" in name }
        )
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation(project(":annotations"))
    implementation(libs.ksp.api)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlin.compile.testing.ksp)

    testImplementation(libs.jupiter.api)
    testRuntimeOnly(libs.jupiter.engine)
}
