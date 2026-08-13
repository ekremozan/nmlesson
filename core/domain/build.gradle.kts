plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    // api, not implementation: Story and PagingData appear in StoryRepository's own signatures, so
    // anything depending on :core:domain needs them on its compile classpath.
    api(project(":core:model"))
    // paging-common, not paging-runtime: the domain layer must stay free of Android dependencies,
    // and PagingData/PagingSource live in the pure-JVM artifact.
    api(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    // javax.inject + plain Dagger, never hilt-android: the domain layer knows about constructor
    // injection but stays free of Hilt and of Android. `dagger` is what the generated factories
    // below compile against.
    implementation(libs.javax.inject)
    implementation(libs.dagger)
    // Generates the use cases' Dagger factories here rather than in every consuming module.
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
}
