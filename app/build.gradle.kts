plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.nativeminds"
    // AndroidX (core 1.19, lifecycle 2.11) requires compiling against API 37.
    // targetSdk stays at 36 deliberately: compiling against newer APIs is separate from opting
    // in to the new runtime behavior, which needs its own testing pass.
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.nativeminds"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // Custom runner so instrumented tests boot HiltTestApplication instead of the real one.
        testInstrumentationRunner = "com.example.nativeminds.HiltTestRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
    implementation(project(":feature:home"))
    // :app is the composition root — it is the one module that pulls in the implementations so
    // their Hilt modules land in the generated component. Feature modules still only see :core:domain.
    implementation(project(":core:data"))
    // Only so the generated component can name StoryDao. Using api() on :core:data instead would
    // leak Room types up to every consumer, which is exactly what the layering avoids.
    implementation(project(":core:database"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.hilt.android.testing)
    // Room stays an implementation detail of :core:database everywhere except here, where the test
    // module has to build an in-memory database itself.
    androidTestImplementation(libs.androidx.room.runtime)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}