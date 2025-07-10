package com.whiplash.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.databinding.ActivitySplashBinding
import com.whiplash.presentation.login.GoogleLoginManager
import com.whiplash.presentation.login.KakaoLoginManager
import com.whiplash.presentation.login.LoginActivity
import com.whiplash.presentation.main.MainActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    @Inject
    lateinit var googleLoginManager: GoogleLoginManager

    @Inject
    lateinit var kakaoLoginManager: KakaoLoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            delay(1000)
            checkLoginStatusAndNavigate()
        }
    }

    /**
     * 로그인 상태 확인 후 적절한 화면으로 이동
     */
    private suspend fun checkLoginStatusAndNavigate() {
        try {
            // 구글 로그인 상태 확인
            val googleUser = googleLoginManager.getCurrentUser()
            if (googleUser != null) {
                Timber.d("## [스플래시] 구글 로그인 상태 확인됨. 이메일 : ${googleUser.email} -> 메인 액티비티 이동")
                navigateToMain()
                return
            }

            // 카카오 로그인 상태 확인
            kakaoLoginManager.getCurrentUser()
                .onSuccess { kakaoUser ->
                    if (kakaoUser != null) {
                        Timber.d("## [스플래시] 카카오 로그인 상태 확인됨. 이메일 : ${kakaoUser.email} -> 메인 액티비티 이동")
                        navigateToMain()
                    } else {
                        Timber.d("## [스플래시] 카카오 로그인한 적 없음 - 로그인 화면으로 이동")
                        navigateToLogin()
                    }
                }
                .onFailure { e ->
                    Timber.e("## [스플래시] 카카오 로그인 상태 확인 실패 : ${e.message}")
                    Timber.d("## [스플래시] 로그인 상태 확인 실패 - 로그인 화면으로 이동")
                    navigateToLogin()
                }
        } catch (e: Exception) {
            Timber.e("## [스플래시] 로그인 상태 확인 중 에러 : ${e.message}")
            navigateToLogin()
        }
    }

    private fun navigateToMain() {
        navigateTo<MainActivity> {
            finishCurrentActivity()
        }
    }

    private fun navigateToLogin() {
        navigateTo<LoginActivity> {
            finishCurrentActivity()
        }
    }
}