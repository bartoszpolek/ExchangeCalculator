plugins {
    id("exchange.android.library")
    id("exchange.kotlinx.serialization")
    id("exchange.android.hilt")
}

android {
    namespace = "com.example.exchange.core.network"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.assertk)
    testImplementation(libs.okhttp.mockwebserver)
}
