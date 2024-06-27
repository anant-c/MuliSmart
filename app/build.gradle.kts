plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.mulismarthome"
    compileSdk = 34

    useLibrary ("org.apache.http.legacy")
    packaging {
        resources.merges.add ("META-INF/*")
    }


    defaultConfig {
        applicationId = "com.example.mulismarthome"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {
    implementation (libs.json.simple)
    implementation(libs.azure.ai.translation.text)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.glide)
    implementation(libs.volley)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.retrofit)
    implementation(libs.tensorflow.lite)
    implementation (libs.tensorflow.lite.select.tf.ops)
    implementation (libs.tensorflow.lite.support.v031)
//    implementation (libs.converter.gson)
//    implementation (libs.gson) // Optional, if not already included

}

configurations {
    all {
        exclude(group = "*", module = "junit")
    }
}