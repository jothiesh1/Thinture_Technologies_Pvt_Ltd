plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.thinturetechnologiespvtltd"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.thinturetechnologiespvtltd"
        minSdk = 21
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
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.ui:ui:1.6.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.4")
    implementation("androidx.compose.material:material-icons-extended")
    implementation ("com.patrykandpatrick.vico:compose:1.13.0")
    implementation ("com.patrykandpatrick.vico:core:1.13.0")
    implementation ("com.patrykandpatrick.vico:compose-m3:1.13.0")
    implementation("org.osmdroid:osmdroid-android:6.1.16")


    // Retrofit for networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

// Converter for JSON (you can also use Moshi or Scalars if needed)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Optional: OkHttp logging interceptor for debug logging
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")


}