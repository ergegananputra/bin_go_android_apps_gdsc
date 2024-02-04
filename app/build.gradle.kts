plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // KSP
    id("com.google.devtools.ksp")

    // Room
//    id("androidx.room")

    // Navigation
    id("androidx.navigation.safeargs.kotlin")

    // Google
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.gdsc.bingo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gdsc.bingo"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
//    room {
//        schemaDirectory("$projectDir/schemas")
//    }
}

dependencies {
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Room
//    implementation("androidx.room:room-ktx:2.6.1")
//    implementation("androidx.room:room-runtime:2.6.1")
//    ksp("androidx.room:room-compiler:2.6.1")

    // Coil
    implementation("io.coil-kt:coil:2.5.0")

    // Coil SVG
    implementation("io.coil-kt:coil-svg:2.5.0")

    // Google Maps
    implementation("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:1.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}