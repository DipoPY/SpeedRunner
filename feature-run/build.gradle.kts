plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.dipo.featurerun"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
    kotlinOptions { jvmTarget = "17" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":background"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.ui)
    implementation(libs.androidx.foundation)
    implementation(libs.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.osmdroid)
    implementation(libs.android.sdk)
    implementation(libs.play.services.location)

    implementation(libs.coil.compose)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
