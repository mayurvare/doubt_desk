plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.firstapp"
    compileSdk = 35



    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.firstapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "OPENROUTER_API_KEY",
            "\"Bearer ${project.properties["OPENROUTER_API_KEY"]}\""
        )


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
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
//    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation(libs.googleid)
    implementation(libs.firebase.database)
//    implementation("com.google.android.gms:play-services-auth:20.5.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.ai.client.generativeai:generativeai:0.9.0")


// Firebase (if not already included)
    implementation ("com.google.firebase:firebase-database")
    implementation ("com.google.firebase:firebase-auth")

// RecyclerView and core
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("com.google.android.material:material:1.11.0")

    // Firebase Auth
    implementation ("com.google.firebase:firebase-auth:22.3.1")
// Google Sign-In
    implementation ("com.google.android.gms:play-services-auth:21.0.0")




}