plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":core:model"))
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.javax.inject)
    implementation(libs.dagger)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
