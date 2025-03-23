plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Firebase plugin
}

android {
    namespace = "com.example.myapplication" // Ensure this matches Firebase
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
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
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 🔹 **AndroidX Core**
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // 🔹 **Jetpack Compose Core UI**
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.ui:ui-text")
    // In your app-level build.gradle (should already be there)
    implementation ("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.compose.material:material-icons-extended:1.5.0")

    // 🔹 **Material 3 Components**
    implementation("androidx.compose.material3:material3")

    // 🔹 **Jetpack Compose Extensions**
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0")

    // 🔹 **Navigation**
    implementation("androidx.navigation:navigation-compose:2.7.3") // Latest stable

    // 🔹 **Firebase BoM (Bill of Materials)**
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx") // Cloud Messaging

    // 🔹 **ML Kit (Text Recognition)**
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // 🔹 **Coil (Image Loading)**
    implementation("io.coil-kt:coil-compose:2.2.2")

    // 🔹 **Testing Dependencies**
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}