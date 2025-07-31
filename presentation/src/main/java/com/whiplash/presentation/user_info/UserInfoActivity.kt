package com.whiplash.presentation.user_info

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityUserInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.login.KakaoLoginManager
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    @Inject
    lateinit var kakaoLoginManager: KakaoLoginManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user_info_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUserInfoViews()
    }

    private fun setupUserInfoViews() {
        with(binding) {
            whUserInfo.setTitle("회원 정보")

            // 현재 버전
            uivCurrentVersion.apply {
                setIcon(R.drawable.ic_profile_go_onboarding)
                setText(getString(R.string.current_version))
                setAppVersion(getCurrentAppVersion())
                showAppVersion(true)
                showRightArrow(false)
                setOnItemClickListener { /* 버전 체크 로직이 들어가야 하나? */ }
            }

            // 이용약관
            uivTermsAndConditions.apply {
                setIcon(R.drawable.ic_profile_go_onboarding_1)
                setText(getString(R.string.terms_policy))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    openWebPage("https://m.naver.com")
                    Timber.d("## [회원정보] 이용약관 클릭")
                }
            }

            // 개인정보처리방침
            uivPolicyPersonalInfo.apply {
                setIcon(R.drawable.ic_profile_go_onboarding_2)
                setText(getString(R.string.personal_info_policy))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 개인정보처리방침 클릭")
                    openWebPage("https://m.naver.com")
                }
            }

            // 정보동의 설정
            uivSettingAgreements.apply {
                setIcon(R.drawable.ic_profile_agree)
                setText(getString(R.string.setting_agreements))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 정보동의 설정 클릭")
                }
            }

            // 문의하기
            uivInquiry.apply {
                setIcon(R.drawable.ic_profile_ask)
                setText(getString(R.string.inquiry_string))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 문의하기 클릭")
                }
            }

            // 로그아웃하기
            uivLogout.apply {
                setIcon(R.drawable.ic_profile_login_info)
                setText(getString(R.string.logout_string))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 로그아웃 클릭")
                    lifecycleScope.launch {
                        kakaoLoginManager.signOut()
                    }
                }
            }

            // 회원탈퇴
            uivWithdrawal.apply {
                setIcon(R.drawable.ic_profile_out)
                setText(getString(R.string.withdrawal_string))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 회원탈퇴 클릭")
                }
            }
        }
    }

    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName ?: ""
            if (versionName.isNotEmpty()) "v$versionName" else ""
        } catch (e: Exception) {
            Timber.e(e, "앱 버전 정보를 가져올 수 없습니다.")
            ""
        }
    }

    private fun openWebPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }

}