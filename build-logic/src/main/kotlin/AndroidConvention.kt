import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion

private const val COMPILE_SDK = 36
private const val MIN_SDK = 30
private const val TARGET_SDK = 36
private const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

internal fun ApplicationExtension.configureAndroidApplication() {
    configureAndroidCommon()
    defaultConfig {
        targetSdk = TARGET_SDK
    }
}

internal fun LibraryExtension.configureAndroidLibrary() {
    configureAndroidCommon()
}

private fun CommonExtension.configureAndroidCommon() {
    compileSdk = COMPILE_SDK

    defaultConfig.minSdk = MIN_SDK
    defaultConfig.testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER

    compileOptions.sourceCompatibility = JavaVersion.VERSION_11
    compileOptions.targetCompatibility = JavaVersion.VERSION_11

    lint.abortOnError = true
    lint.warningsAsErrors = true
    lint.disable += "GradleDependency"
}
