plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add the safe args plugin here
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "tn.isilan.projet"
    compileSdk = 36  // Note: Consider updating to 34 (latest stable)
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "tn.isilan.projet"
        minSdk = 24
        targetSdk = 36  // Note: Consider updating to 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // FIX: Remove this duplicate room dependency
    // implementation(libs.androidx.room.common.jvm) // <-- REMOVE THIS

    // FIX: Remove this duplicate layout dependency
    // implementation(libs.androidx.foundation.layout) // <-- REMOVE THIS

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Room - Add kapt plugin for annotation processing
    implementation("androidx.room:room-runtime:2.6.1") // Use consistent version
    implementation("androidx.room:room-ktx:2.6.1") // For Kotlin extensions


    // Navigation - Use consistent versions
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    // Add Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    kapt("androidx.room:room-compiler:2.6.1")

    // FIX: Remove this - it's a plugin, not a dependency
    // implementation("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4") // <-- REMOVE THIS
}