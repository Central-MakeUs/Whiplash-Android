package com.whiplash.presentation.login

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityLoginBinding
import com.whiplash.presentation.main.MainActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    @Inject
    lateinit var googleLoginManager: GoogleLoginManager

    @Inject
    lateinit var kakaoLoginManager: KakaoLoginManager

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d("## [구글 로그인] launcher 결과 - resultCode: ${result.resultCode}, RESULT_OK: $RESULT_OK")
        
        if (result.resultCode == RESULT_OK) {
            lifecycleScope.launch {
                try {
                    googleLoginManager.handleGoogleSignIn(result.data)
                        .onSuccess { user ->
                            handleLoginSuccess("구글", user.email)
                        }
                        .onFailure { e ->
                            Timber.e("## [구글 로그인] handleGoogleSignIn 실패 : ${e.message}")
                            showLoginError("구글 로그인에 실패했습니다: ${e.message}")
                        }
                } catch (e: Exception) {
                    Timber.e("## [구글 로그인] launcher에서 에러 발생: ${e.message}")
                    showLoginError("구글 로그인 중 오류가 발생했습니다")
                }
            }
        } else {
            Timber.e("## [구글 로그인] 로그인 취소 or 실패 - resultCode: ${result.resultCode}")
            showLoginError("구글 로그인이 취소되었습니다")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupLoginButtons()
    }

    private fun setupLoginButtons() {
        with(binding) {
            // 카카오 로그인
            ivKakaoLogin.setOnClickListener {
                startKakaoLogin()
            }

            // 구글 로그인
            ivGoogleLogin.setOnClickListener {
                startGoogleLogin()
            }
        }
    }

    private fun startGoogleLogin() {
        try {
            val signInIntent = googleLoginManager.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Timber.e("## [구글 로그인] Intent 생성 실패: ${e.message}")
            showLoginError("에러가 발생했습니다. 잠시 후 다시 시도해 주세요")
        }
    }

    private fun startKakaoLogin() = lifecycleScope.launch {
        try {
            kakaoLoginManager.signIn(this@LoginActivity)
                .onSuccess { user ->
                    Timber.d("## [카카오 로그인] 카카오 로그인 성공: $user")
                    handleLoginSuccess("카카오", user.email)
                }
                .onFailure { e ->
                    Timber.e("## [카카오 로그인] 카카오 로그인 실패: ${e.message}")
                    showLoginError("카카오 로그인에 실패했습니다. 잠시 후 다시 시도해 주세요")
                }
        } catch (e: Exception) {
            Timber.e("## [카카오 로그인] 에러 발생: ${e.message}")
            showLoginError("카카오 로그인 중 오류가 발생했습니다")
        }
    }

    private fun handleLoginSuccess(provider: String, email: String?) {
        Timber.d("## [$provider 로그인] 성공 - 이메일: $email")
        navigateTo<MainActivity> {
            finishCurrentActivity()
        }
    }

    private fun showLoginError(message: String) {
        Timber.e("## [로그인 에러] $message")
    }
}