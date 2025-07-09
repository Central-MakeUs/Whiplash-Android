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
            activateButton.setText("테스트")
            activateButton.setOnClickListener {
                // 선택 상태 토글은 내부에서 자동 처리됨
                val isSelected = activateButton.isSelected
                Timber.d("버튼 선택 상태: $isSelected")
            }

            // 요일 버튼들 설정
            btnMon.setText("월")
            btnTue.setText("화")
            btnWed.setText("수")

            // 각 버튼 클릭 리스너 설정
            btnMon.setOnClickListener {
                Timber.d("월요일 선택: ${btnMon.isSelected}")
            }

            btnTue.setOnClickListener {
                Timber.d("화요일 선택: ${btnTue.isSelected}")
            }

            btnWed.setOnClickListener {
                Timber.d("수요일 선택: ${btnWed.isSelected}")
            }

            // 라디오 버튼
            rbOption1.setOnCheckedChangeListener { _, isChecked ->
                Timber.d("옵션 1 선택: $isChecked")
            }

            rbOption2.setOnCheckedChangeListener { _, isChecked ->
                Timber.d("옵션 2 선택: $isChecked")
            }

            rbOption3.setOnCheckedChangeListener { _, isChecked ->
                Timber.d("옵션 3 선택: $isChecked")
            }

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.rbOption1 -> Timber.d("라디오 그룹: 옵션 1 선택됨")
                    R.id.rbOption2 -> Timber.d("라디오 그룹: 옵션 2 선택됨")
                    R.id.rbOption3 -> Timber.d("라디오 그룹: 옵션 3 선택됨")
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        val signInIntent = googleLoginViewModel.getGoogleSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }
}