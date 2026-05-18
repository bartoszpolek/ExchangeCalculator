plugins {
    id("exchange.android.library")
}

android {
    namespace = "com.example.exchange.core.common"
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(libs.assertk)
}
