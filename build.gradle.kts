plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
}

group = "online.vapcom"
version = "1.2.0"

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "picocbor"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {}
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {}
        iosMain.dependencies {}
    }
}

android {
    namespace = "online.vapcom.picocbor"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
