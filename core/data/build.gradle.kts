plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.nativeminds.data"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
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
}

dependencies {
    // api: this module's whole purpose is to satisfy the domain contracts, and :app needs them
    // visible to build the Hilt graph.
    api(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))

    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
