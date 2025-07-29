package com.whiplash.presentation.login

import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.R
import com.whiplash.presentation.component.loading.WhiplashLoadingScreen
import com.whiplash.presentation.databinding.ActivityLoginBinding
import com.whiplash.presentation.main.MainActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingScreen: WhiplashLoadingScreen

    private val loginViewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Timber.d("## [구글 로그인] launcher 결과 - resultCode: ${result.resultCode}, RESULT_OK: $RESULT_OK")
        
        if (result.resultCode == RESULT_OK) {
            val deviceId = getAndroidDeviceId()
            loginViewModel.handleGoogleSignIn(result.data, deviceId)
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

        setupView()
        setupLoginButtons()
        observeViewModel()
    }

    private fun setupView() {
        loadingScreen = WhiplashLoadingScreen(this)
        with(binding) {
            whLogin.setTitle("로그인")
        }
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

    private fun observeViewModel() {
        lifecycleScope.launch {
            loginViewModel.uiState.collect { state ->
                when {
                    state.isLoading -> {
                        // 로딩 UI 표시
                        Timber.d("## 로그인 진행 중...")
                    }
                    state.isLoginSuccess -> {
                        Timber.d("## 로그인 성공")
                        handleLoginSuccess()
                    }
                    state.errorMessage != null -> {
                        Timber.e("## 로그인 실패: ${state.errorMessage}")
                        showLoginError(state.errorMessage)
                    }
                }
            }
        }
    }

    private fun startGoogleLogin() {
        try {
            val signInIntent = loginViewModel.getGoogleSignInIntent()
            googleSignInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            Timber.e("## [구글 로그인] Intent 생성 실패: ${e.message}")
            showLoginError("에러가 발생했습니다. 잠시 후 다시 시도해 주세요")
        }
    }

    private fun startKakaoLogin() {
        val deviceId = getAndroidDeviceId()
        loginViewModel.handleKakaoSignIn(this, deviceId)
    }

    private fun getAndroidDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"
    }

    private fun handleLoginSuccess() {
        navigateTo<MainActivity> {
            finishCurrentActivity()
        }
    }

    private fun showLoginError(message: String) {
        Timber.e("## [로그인 에러] $message")
    }
}