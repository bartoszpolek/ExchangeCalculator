plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.exchange.feature.exchange"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
