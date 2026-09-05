plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.predictxsports.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.predictxsports.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 13
        versionName = "1.0.1"
    }

    signingConfigs {
        create("release") {
            storeFile = file("../release.keystore")
            // P2-1：密碼從 ~/.gradle/gradle.properties 讀取，不再 commit 到 repo。
            // 若全域 gradle.properties 缺少這些 key，build 會失敗並提示。
            storePassword = providers.gradleProperty("PREDICTX_KEYSTORE_PASSWORD")
                .orNull
                ?: error("Missing PREDICTX_KEYSTORE_PASSWORD in ~/.gradle/gradle.properties")
            keyAlias = providers.gradleProperty("PREDICTX_KEY_ALIAS")
                .orNull
                ?: error("Missing PREDICTX_KEY_ALIAS in ~/.gradle/gradle.properties")
            keyPassword = providers.gradleProperty("PREDICTX_KEY_PASSWORD")
                .orNull
                ?: error("Missing PREDICTX_KEY_PASSWORD in ~/.gradle/gradle.properties")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.01.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity Compose (1.10.0+ 帶新版 androidx.fragment 1.8.x)
    implementation("androidx.activity:activity-compose:1.10.1")

    // Explicit fragment dependency (override BOM 預設 1.1.0 → 1.8.5)
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.fragment:fragment-compose:1.8.5")

    // ViewModel + Navigation
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Retrofit + kotlinx.serialization (API)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // Google Play Billing
    implementation("com.android.billingclient:billing:9.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Core
    implementation("androidx.core:core-ktx:1.15.0")
}