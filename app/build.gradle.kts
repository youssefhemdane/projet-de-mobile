plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

}

android {
    namespace = "tn.isilan.projet"
    compileSdk = 36
    buildFeatures {
        viewBinding =true
    }

    defaultConfig {
        applicationId = "tn.isilan.projet"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.androidx.room.common.jvm)
    implementation(libs.androidx.foundation.layout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //Room
    implementation("androidx.room:room-runtime:2.8.0")
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
// RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.1")

    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // Safe Args (TRÈS IMPORTANT)
    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.4")

}