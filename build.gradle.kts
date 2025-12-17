// Top-level build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // Add safe args plugin
    id("androidx.navigation.safeargs.kotlin") version "2.7.2" apply false
}