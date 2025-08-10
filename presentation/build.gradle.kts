plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.whiplash.presentation"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildFeatures {
        viewBinding = true
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
    implementation(project(":domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.core.testing)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // hilt
    implementation(libs.hilt)
    kapt(libs.hilt.compiler)

    // coroutine
    implementation(libs.kotlinx.coroutines.android)

    // image
    implementation(libs.coil)

    // androidx-lifecycle-viewmodel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // log
    implementation(libs.timber)

    // 구글 로그인
    implementation(libs.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // 카카오 로그인
    implementation(libs.kakao.v2.user)

    // 네이버 지도
    implementation(libs.naver.map.sdk)
    implementation(libs.play.services.location)

    // 알람 설정 화면에서 사용하는 NumberPicker 라이브러리
    implementation(libs.number.picker)

    // FCM
    implementation(libs.firebase.messaging)
}