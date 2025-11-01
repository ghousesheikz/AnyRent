plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.shaikhomes.anyrent"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.shaikhomes.anyrent"
        minSdk = 24
        targetSdk = 34
        versionCode = 36
        versionName = "36.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    // retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.6.4")
    implementation("com.squareup.okhttp3:logging-interceptor:3.12.13")
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("androidx.activity:activity:1.9.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.jraska:falcon:2.2.0")
    implementation("com.github.KevinSchildhorn:OTPView:0.2.5")
    implementation("com.github.clans:fab:1.6.4")
    implementation("com.github.ibrahimsn98:SmoothBottomBar:1.7.9")
    implementation("com.google.zxing:core:3.5.1") // QR Code generation core library
    implementation("com.journeyapps:zxing-android-embedded:4.3.0") // Android compatibility
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    implementation("com.github.bumptech.glide:glide:4.15.0") // Latest stable Glide version
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.0")
}