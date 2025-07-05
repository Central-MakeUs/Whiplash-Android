package com.whiplash.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.databinding.ActivityMainBinding
import com.whiplash.presentation.login.GoogleLoginViewModel
import com.whiplash.presentation.login.KakaoLoginViewModel
import com.whiplash.presentation.util.KakaoLoginManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val googleLoginViewModel: GoogleLoginViewModel by viewModels()
    private val kakaoLoginViewModel: KakaoLoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Timber.e("## [구글 로그인] 로그인 성공 : ${result.data}")
            googleLoginViewModel.handleSignInResult(result.data)
        } else {
            Timber.e("## [구글 로그인] 로그인 취소 or 실패")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        with(binding) {
            btnKakaoLogin.setOnClickListener {
                kakaoLoginViewModel.startKakaoLogin()
                lifecycleScope.launch {
                    KakaoLoginManager.login(this@MainActivity)
                        .onSuccess { token ->
                            kakaoLoginViewModel.handleKakaoLoginSuccess(token.accessToken)
                        }
                        .onFailure { e ->
                            kakaoLoginViewModel.handleKakaoLoginFailure(
                                e.message ?: "카카오 로그인에 실패했습니다"
                            )
                        }
                }
            }

            btnKakaoLogout.setOnClickListener {
                kakaoLoginViewModel.signOut()
            }
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleLoginViewModel.getGoogleSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }
}