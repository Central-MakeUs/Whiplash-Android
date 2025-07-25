import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.whiplash.akuma"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.whiplash.akuma"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        buildConfigField("String", "KAKAO_NATIVE_KEY", "\"${localProperties.getProperty("KAKAO_NATIVE_KEY", "")}\"")
        buildConfigField("String", "NAVER_MAP_CLIENT_ID", "\"${localProperties.getProperty("NAVER_MAP_CLIENT_ID", "")}\"")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":presentation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 구글 로그인
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // 카카오 로그인
    implementation(libs.kakao.v2.user)

    // hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // dataStore
    implementation(libs.datastore)

    // kotlinx-serialization
    implementation(libs.kotlinx.serialization.json)

    // coroutine
    implementation(libs.kotlinx.coroutines.android)

    // coil
    implementation(libs.coil)

    // androidx-lifecycle-viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // log
    implementation(libs.timber)

    // 네이버 지도
    implementation(libs.naver.map.sdk)
}