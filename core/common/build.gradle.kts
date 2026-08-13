plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.example.nativeminds.common"
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
    // api: the qualifier annotations appear in the constructor signatures of consumers, and those
    // consumers inject CoroutineDispatcher/CoroutineScope types from here.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
