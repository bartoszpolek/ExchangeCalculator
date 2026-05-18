plugins {
    id("exchange.android.library")
    id("exchange.android.compose")
}

android {
    namespace = "com.example.exchange.core.designsystem"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
    testImplementation(libs.assertk)
}
