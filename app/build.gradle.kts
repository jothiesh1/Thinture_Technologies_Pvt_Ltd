plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.gpsapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.gpsapp"
        minSdk = 23
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.6.10"
    }
}

dependencies {
    // Version variables
    val lifecycleVersion = "2.9.1"
    val navVersion = "2.9.0"
    val vicoVersion = "1.13.0"

    // Core Android libraries
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.fragment:fragment-ktx:1.8.8")

    // Compose BOM - manages versions
    implementation(platform("androidx.compose:compose-bom:2025.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material3:material3")

    // Navigation
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // ViewModel + Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")

    // Charts (Vico + MPAndroidChart)
    implementation("com.patrykandpatrick.vico:compose:$vicoVersion")
    implementation("com.patrykandpatrick.vico:core:$vicoVersion")
    implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // osmdroid map
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Paging
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")

    // Logging
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug-only tooling
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.36.0")

    implementation("androidx.datastore:datastore-preferences:1.1.7")

    implementation("androidx.compose.ui:ui-text:")
}
