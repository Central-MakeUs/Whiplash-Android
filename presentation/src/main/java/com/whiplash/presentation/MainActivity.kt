package com.whiplash.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.databinding.ActivityMainBinding
import com.whiplash.presentation.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val loginViewModel: LoginViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Timber.e("## [구글 로그인] 로그인 성공 : ${result.data}")
            loginViewModel.handleSignInResult(result.data)
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

//        with(binding) {
//            tvLogin.setOnClickListener {
//                Timber.e("## [구글 로그인] 버튼 클릭됨")
//                startGoogleSignIn()
//            }
//
//            tvLogout.setOnClickListener {
//                Timber.e("## [구글 로그아웃] 버튼 클릭됨")
//                loginViewModel.signOut()
//            }
//        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = loginViewModel.getGoogleSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }
}