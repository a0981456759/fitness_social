plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fitness_social"
    compileSdk = 34

    signingConfigs {
        getByName("debug") {
            storeFile = file("STORE_FILE")
            storePassword = "STORE_PASSWORD"
            keyAlias = "KEY_ALIAS"
            keyPassword = "KEY_PASSWORD"
        }
    }

    defaultConfig {
        applicationId = "com.example.fitness_social"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    // Import the Firebase BoM (see: https://firebase.google.com/docs/android/learn-more#bom)
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(platform(libs.firebase.bom))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)
    // Firebase Authentication
    implementation(libs.firebase.auth)
    // Firebase Storage
    implementation(libs.firebase.storage)
    // Google Identity Services SDK (only required for Auth with Google)
    implementation(libs.play.services.auth)

    // Firebase Realtime Database
    implementation(libs.firebase.database)
    implementation(libs.firebase.ui.database)
    implementation(libs.activity)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)

    implementation (libs.libphonenumber)
    implementation(libs.play.services.location)
    implementation(libs.places)
    implementation(libs.okhttp)
    implementation(libs.maps.utils)
    implementation (libs.mpandroidchart)
}