plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.exchange.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
