import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}




android {
    namespace = "com.example.watchface01"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.watchface01"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Load the API key from local.properties
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Load the API key from local.properties
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }
        val apiKey = localProperties.getProperty("WEATHER_API_KEY") ?: ""
        buildConfigField("String", "WEATHER_API_KEY", "\"$apiKey\"")

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
        compose = true
        buildConfig = true
    }

}

dependencies {

    implementation(libs.play.services.wearable)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.wear.tooling.preview)
    implementation("androidx.wear.compose:compose-material:1.4.0")
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation("androidx.wear:wear:1.2.0")
    implementation("com.google.android.gms:play-services-wearable:17.1.0")


    // Dependencies or Libraries for Weather and WearOS face watch

    // Compose dependencies
    implementation(libs.material)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.ui.ui.tooling)

    // Retrofit dependencies
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // Coroutines dependencies
    implementation(libs.kotlinx.coroutines.core)
}

