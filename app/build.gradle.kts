plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.ricordino"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ricordino"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        create("qaDebug") {
            initWith(getByName("debug"))
            isDebuggable = true
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
        }
        create("production") {
            initWith(getByName("release"))
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // No release keystore exists yet — sign with the debug key so this variant stays
            // installable for testing. Swap in a real signingConfig once a keystore is generated.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    // Only qaDebug and production should be selectable — the AGP-default debug/release
    // build types exist internally (qaDebug/production are derived from them via initWith)
    // but aren't meant to be built directly.
    androidComponents {
        beforeVariants(selector().withBuildType("debug")) { it.enable = false }
        beforeVariants(selector().withBuildType("release")) { it.enable = false }
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

    testOptions {
        unitTests {
            // Room's in-memory DAO test needs a real Context, which Robolectric provides.
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    // No dynamic-feature modules yet, but the base app is set up to support Play Feature
    // Delivery: future subscription-gated features land as com.android.dynamic-feature
    // modules listed here (see settings.gradle.kts) and depend on :app for shared code.
    dynamicFeatures += mutableSetOf()
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    implementation(libs.mlkit.text.recognition)

    implementation(libs.coroutines.android)
    implementation(libs.coroutines.play.services)

    testImplementation(libs.junit)
    testImplementation(libs.room.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
}
