import com.google.protobuf.gradle.proto
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.google.services)
    alias(libs.plugins.crashlytics)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
        }
    }
}

android {
    sourceSets {
        getByName("main") {
            proto {
                srcDir("../proto-src/definitions")
            }
        }
    }

    namespace = "com.force.confbb"
    compileSdk = 36

    val properties = Properties().apply {
        load(rootProject.file("local.properties").inputStream())
    }
    val keystoreFile = properties["KEYSTORE_FILE"]?.toString()
    val keystorePassword = properties["KEYSTORE_PASSWORD"]?.toString()
    val alias = properties["KEY_ALIAS"]?.toString()
    val password = properties["KEY_PASSWORD"]?.toString()

    android {
        signingConfigs {
            create("release") {
                storeFile = keystoreFile?.let { rootProject.file(it) }
                storePassword = keystorePassword
                keyAlias = alias
                keyPassword = password
            }
        }
    }


    defaultConfig {
        applicationId = "com.force.confbb"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.material3.adaptive.navigation.suite.android)
    implementation(libs.androidx.navigation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.accompanist.permissions)
    implementation(libs.android.protobuf)
    implementation(libs.androidx.anotations)
    implementation(libs.androidx.room) {
        exclude(group = "com.intellij", module = "annotations")
    }
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-crashlytics-ndk")
    implementation("com.google.firebase:firebase-analytics")
    implementation(project(":connection"))
    implementation(project(":model"))
    implementation(project(":crypto"))
    implementation(project(":misc"))
    ksp(libs.hilt.compiler)
    ksp(libs.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
