import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    implementation(project(":app:shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "id.ardion.quran"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        getByName("debug") {
            val localKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
            if (localKeystore.exists()) {
                storeFile = localKeystore
            }
        }
    }

    defaultConfig {
        applicationId = "id.ardion.quran"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionCode = 1
            versionName = "1.0.0-dev"
            resValue("string", "app_name", "Bacalah (DEV)")
            buildConfigField("String", "BASE_URL", "\"https://api.alquran.cloud/v1/\"")
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionCode = 100
            versionName = "1.0.0-stg"
            resValue("string", "app_name", "Bacalah (STG)")
            buildConfigField("String", "BASE_URL", "\"https://api.alquran.cloud/v1/\"")
        }
        create("prod") {
            dimension = "environment"
            versionCode = 1000
            versionName = "1.0.0"
            resValue("string", "app_name", "Bacalah")
            buildConfigField("String", "BASE_URL", "\"https://api.alquran.cloud/v1/\"")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }
}
