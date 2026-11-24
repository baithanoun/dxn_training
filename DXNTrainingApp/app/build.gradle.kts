import java.io.FileInputStream
import java.util.Properties
import org.gradle.api.GradleException

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// Release signing is read from DXNTrainingApp/keystore.properties (gitignored).
// The owner creates the keystore; never commit it or its passwords.
val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
val hasKeystore = keystorePropertiesFile.exists()
if (hasKeystore) {
    FileInputStream(keystorePropertiesFile).use { keystoreProperties.load(it) }
}

// Set the real HTTPS origin via DXN_RELEASE_BASE_URL (env) or -PDXN_RELEASE_BASE_URL=...
// or a `dxn.releaseBaseUrl` entry in local.properties. The placeholder is rejected for release builds.
val releaseBaseUrl = (
    System.getenv("DXN_RELEASE_BASE_URL")
        ?: (project.findProperty("DXN_RELEASE_BASE_URL") as String?)
        ?: "https://<domain>/"
    ).trim().let { if (it.endsWith("/")) it else "$it/" }

android {
    namespace = "com.dxn.training"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.dxn.training"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // debug: emulator (10.0.2.2 = host machine)
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
        }
        release {
            buildConfigField("String", "BASE_URL", "\"$releaseBaseUrl\"")
            // Release builds require a real keystore (see README). No debug fallback.
            signingConfig = if (hasKeystore) signingConfigs.getByName("release") else null
            isMinifyEnabled = true
            isShrinkResources = true
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.05.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Fail fast on release builds that still use the placeholder origin.
gradle.taskGraph.whenReady {
    val releaseTasks = allTasks.filter { it.name.contains("Release", ignoreCase = true) }
    val isRelease = releaseTasks.isNotEmpty()
    if (isRelease && releaseBaseUrl.contains("<domain>")) {
        throw GradleException(
            "DXN_RELEASE_BASE_URL is still the placeholder. Set it to the real https://... origin before building a release."
        )
    }
    if (isRelease && !hasKeystore) {
        throw GradleException(
            "Release signing keystore missing: copy keystore.properties.example to keystore.properties (outside git) and fill it — see README. Publishing without a real keystore is not allowed."
        )
    }
}
