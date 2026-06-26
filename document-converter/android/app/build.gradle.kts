import java.io.File
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val keystorePropertiesFile = rootProject.file("../../.local/document-converter-android/release-keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun releaseSecret(propertyName: String, environmentName: String): String {
    return (keystoreProperties[propertyName] as String?)
        ?: System.getenv(environmentName)
        ?: ""
}

val releaseStoreFilePath = releaseSecret("storeFile", "DOCUMENT_CONVERTER_STORE_FILE")
val releaseStorePassword = releaseSecret("storePassword", "DOCUMENT_CONVERTER_STORE_PASSWORD")
val releaseKeyAlias = releaseSecret("keyAlias", "DOCUMENT_CONVERTER_KEY_ALIAS")
val releaseKeyPassword = releaseSecret("keyPassword", "DOCUMENT_CONVERTER_KEY_PASSWORD")
val releaseSigningEnabled = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { it.isNotBlank() }

fun resolveReleaseStoreFile(path: String): File {
    val file = File(path)
    return if (file.isAbsolute) file else keystorePropertiesFile.parentFile.resolve(path)
}

android {
    namespace = "com.documentconverter.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.documentconverter.android"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (releaseSigningEnabled) {
            create("release") {
                storeFile = resolveReleaseStoreFile(releaseStoreFilePath)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (releaseSigningEnabled) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/versions/**"
            )
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.05.00"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.documentfile:documentfile:1.1.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
