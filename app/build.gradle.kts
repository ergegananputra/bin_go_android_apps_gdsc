import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // KSP
    id("com.google.devtools.ksp")

    // Room
    id("androidx.room")

    // Navigation
    id("androidx.navigation.safeargs.kotlin")

    // Google
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")

    // Realm
    id("io.realm.kotlin")
}

val localProperties = Properties().apply {
    FileInputStream(rootProject.file("local.properties")).use { load(it) }
}

val keystorePath: String by localProperties
val keystorePassword: String by localProperties
val keyAliasProperties: String by localProperties

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

    signingConfigs {
        create("release") {
            storeFile = file(keystorePath)
            storePassword = keystorePassword
            keyAlias = keyAliasProperties
            keyPassword = keystorePassword
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
    room {
        schemaDirectory("$projectDir/schemas")
//        schemaDirectory("src/main/schemas")
    }

    sourceSets {
        // Adds exported schema location as test app assets.
        // follow this instruction: https://developer.android.com/training/data-storage/room/migrating-db-versions#kotlin_1
        // TODO: build test case
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }


    packaging {
        resources{
            excludes.add("**/LICENSE.md")
            excludes.add("**/LICENSE-notice.md")
        }

    }
}

dependencies {
    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Room
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-common:2.6.1")
    implementation("androidx.activity:activity-ktx:1.9.0")
    ksp("androidx.room:room-compiler:2.6.1")

    // Realm
    implementation("io.realm.kotlin:library-base:1.11.0")

    // Security
    implementation("androidx.security:security-crypto:1.0.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    // Add this to your app/build.gradle
    implementation("com.firebase:geofire-android-common:3.2.0")

    // Coil
    implementation("io.coil-kt:coil:2.5.0")

    // Coil SVG
    implementation("io.coil-kt:coil-svg:2.5.0")

    // Google Maps
    implementation("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.4.0")
    implementation ("com.github.delight-im:Android-SimpleLocation:v1.1.0")

    // Youtube
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")

    // Flex Box
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Shimmer from facebook
    implementation("com.facebook.shimmer:shimmer:0.5.0")


    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")

    // Location & Polyline
    implementation ("com.github.delight-im:Android-SimpleLocation:v1.1.0")

    // WYSIWYG
    implementation("com.github.onecode369:WYSIWYG:4.0")

    // HTML Parser
    val ksoupVersion = "0.3.1"
    implementation("com.mohamedrejeb.ksoup:ksoup-html:$ksoupVersion")
    implementation("com.mohamedrejeb.ksoup:ksoup-entities:$ksoupVersion")

    // Swipe Refresh Layout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-alpha01")

    // Lottie Animation
    implementation ("com.airbnb.android:lottie:6.4.0")

    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}