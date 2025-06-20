plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.yydaniel.bestdoricarddownloader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yydaniel.bestdoricarddownloader"
        minSdk = 28
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 28
        versionCode = 1
        versionName = "1.0.3.20250620"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    applicationVariants.all {
        val variant = this
        variant.outputs
            .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
            .forEach { output ->
                val buildType = variant.buildType.name
                val versionName = variant.versionName
                val versionCode = variant.versionCode

                output.outputFileName = "YYDanielJr-Bestdori_Card_Downloader-${buildType}_v${versionName}(${versionCode}).apk"
            }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.volley)
    implementation(libs.commons.io)
}