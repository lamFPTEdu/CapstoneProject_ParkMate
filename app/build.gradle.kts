plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
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

        // Lấy api key từ gradle. properties
        val openmapApiKey: String = project.findProperty("openmapApiKey") as? String ?: ""
        val graphhopperApiKey: String = project.findProperty("graphhopperApiKey") as? String ?: ""

        // BuildConfig field - luôn wrap trong dấu ngoặc kép
        buildConfigField("String", "OPENMAP_API_KEY", "\"${openmapApiKey}\"")
        buildConfigField("String", "GRAPHHOPPER_API_KEY", "\"${graphhopperApiKey}\"")

        // Tối ưu hóa cho mọi thiết bị
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    buildTypes {
        debug {
            // BASE_URL dùng cho môi trường debug
            buildConfigField("String", "BASE_URL", "\"https://avokadu.com/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // BASE_URL cho release (có thể thay đổi khi deploy production)
            buildConfigField("String", "BASE_URL", "\"https://avokadu.com/\"")
        }
    }

    buildFeatures {
        buildConfig = true // Bật tạo lớp BuildConfig để dùng buildConfigField
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

    // SwipeRefreshLayout
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // MapLibre SDK cho bản đồ
    implementation("org.maplibre.gl:android-sdk:11.0.0")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")

    // AltBeacon library for BLE support
    implementation("org.altbeacon:android-beacon-library:2.20.6")

    // Biometric authentication
    implementation("androidx.biometric:biometric:1.1.0")
    
    // Security crypto for encrypted SharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

}