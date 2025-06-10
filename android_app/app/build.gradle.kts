import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

val secretsFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()

if (secretsFile.exists()) {
    try {
        FileInputStream(secretsFile).use { fis ->
            secretsProperties.load(fis)
        }
    } catch (_: FileNotFoundException) {
        project.logger.warn("Keystore properties file not found at ${secretsFile.absolutePath}.")
    } catch (e: Exception) {
        project.logger.error("Error loading keystore properties file: ${e.message}")
    }
} else {
    project.logger.warn("Keystore properties file not found at ${secretsFile.absolutePath}.")
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)

    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
}



android {
    namespace = "com.example.travelsharingapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.ToTravel"
        minSdk = 31
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (secretsFile.exists() && secretsProperties.getProperty("RELEASE_STORE_FILE") != null) {
                storeFile = file(secretsProperties.getProperty("RELEASE_STORE_FILE"))
                storePassword = secretsProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = secretsProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = secretsProperties.getProperty("RELEASE_KEY_PASSWORD")
            } else {
                project.logger.warn("Release signing config in build.gradle.kts is not fully set up due to missing secrets.properties.")
            }
        }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }

        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
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
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.camera.core)
    implementation(libs.androidx.camera.camera.lifecycle)
    implementation(libs.androidx.camera.camera.video)
    implementation(libs.androidx.camera.camera.view)
    implementation(libs.androidx.camera.camera.extensions)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.gson)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.maps.compose)

    implementation(libs.play.services.location)
    implementation(platform(libs.kotlin.bom))
    implementation(libs.material)
    implementation(libs.places)
    implementation (libs.accompanist.systemuicontroller)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging.ktx)

    implementation(libs.integrity)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.material3.window.size.class1.android)
    implementation(libs.androidx.window)
    implementation(libs.androidx.window.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"
}
