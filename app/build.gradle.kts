plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")

    //step 4
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-kapt") // **Bunu ekledik**
    id("com.google.dagger.hilt.android") // **Hilt ile kullanacaksan bunu da ekle**
}

android {
    namespace = "com.cumaliguzel.free"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cumaliguzel.free"
        minSdk = 26
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
    implementation(libs.googleid)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
        // Tüm uygulama bağımlılıklarını burada
        implementation("androidx.navigation:navigation-compose:2.8.7")
        // Google Maps Jetpack Compose entegrasyonu
        implementation("com.google.maps.android:maps-compose:6.2.1")
        // Konum servisleri (FusedLocationProviderClient için)
        implementation("com.google.android.gms:play-services-location:21.3.0")
        // Google Maps API servisleri
        implementation("com.google.maps:google-maps-services:0.2.5")
        // Harita çizimleri için yardımcı kütüphane
        implementation("com.google.maps.android:android-maps-utils:2.4.0")
        // 📌 Google Places API - Konum önerileri ve otomatik tamamlama için GEREKLİ!
        implementation("com.google.android.libraries.places:places:4.1.0")
         implementation("androidx.compose.runtime:runtime-livedata:1.7.8")
         implementation ("androidx.compose.material:material-icons-extended:1.5.1")
         implementation( "com.airbnb.android:lottie-compose:5.2.0")
         implementation ("com.squareup.retrofit2:retrofit:2.11.0")
         implementation ("com.squareup.retrofit2:converter-gson:2.11.0")
       // Kotlin Coroutines - Asenkron işlemler için
         implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
         implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
        // Hilt - Dependency Injection
        implementation("com.google.dagger:hilt-android:2.50")
        kapt("com.google.dagger:hilt-compiler:2.50")
         // Hilt'in ViewModel entegrasyonu
         implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
       //firebase
       implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
       implementation("com.google.firebase:firebase-firestore:25.1.1")
       implementation ("com.google.firebase:firebase-storage:21.0.1")
    //auth
       implementation("com.google.firebase:firebase-auth")
       implementation("com.google.android.gms:play-services-gcm:17.0.0")
       implementation("androidx.credentials:credentials:1.3.0")
       implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
       implementation("com.google.android.gms:play-services-auth:21.3.0")
    //
      implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
      implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
      implementation("io.coil-kt.coil3:coil-compose:3.0.4")
      implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")
    //coil for picture
    implementation("io.coil-kt.coil3:coil-compose:3.0.4")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.4")









}