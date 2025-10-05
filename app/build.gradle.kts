plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.parkmate.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.parkmate.android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            // BASE_URL dùng cho môi trường debug
            buildConfigField("String", "BASE_URL", "\"http://parkmate-alb-942390189.ap-southeast-1.elb.amazonaws.com/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // BASE_URL cho release (có thể thay đổi khi deploy production)
            buildConfigField("String", "BASE_URL", "\"http://parkmate-alb-942390189.ap-southeast-1.elb.amazonaws.com/\"")
        }
    }

    buildFeatures {
        buildConfig = true // Bật tạo lớp BuildConfig để dùng buildConfigField
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

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.15.1")

    // Network libraries
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
}