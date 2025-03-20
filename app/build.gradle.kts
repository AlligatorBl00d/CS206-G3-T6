plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") //firebase plugin 
}

android {
    namespace = "com.example.myapplication" // i have ensured that this matches firebase
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

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //import firebase BoM bill of materials 
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
     implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx") // for cloud messaging notifications
    // front end stuff
    implementation("androidx.navigation:navigation-compose:2.7.3")
    // To recognize Latin script
    implementation (libs.text.recognition)
    implementation (libs.text.recognition.v1600)

    // CameraX Core
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // ML Kit Text Recognition (OCR)
    implementation("com.google.mlkit:text-recognition:16.0.1")  // Latin-based scripts (English, etc.)

    // Optional: If your app needs multilingual support, add specific language models
    // implementation("com.google.mlkit:text-recognition-chinese:16.0.0")
    // implementation("com.google.mlkit:text-recognition-devanagari:16.0.0")
    // implementation("com.google.mlkit:text-recognition-japanese:16.0.0")
    // implementation("com.google.mlkit:text-recognition-korean:16.0.0")

    // Coil for image loading in Jetpack Compose
    implementation(libs.coil.compose)

    // Lifecycle & ViewModel (if you need to manage states across recompositions)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Permissions for requesting camera access
    implementation("com.google.accompanist:accompanist-permissions:0.31.1-alpha")

}