package com.whiplash.presentation.user_info

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityUserInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.whiplash.presentation.dialog.LogoutPopup
import com.whiplash.presentation.dialog.WithdrawalPopup
import com.whiplash.presentation.login.KakaoLoginManager
import com.whiplash.presentation.util.WhiplashToast
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class UserInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserInfoBinding

    private val userInfoViewModel: UserInfoViewModel by viewModels()

    @Inject
    lateinit var logoutPopup: LogoutPopup

    @Inject
    lateinit var withdrawalPopup: WithdrawalPopup

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
                    Timber.d("## [회원정보] 이용약관 클릭")
                    openWebPage("https://nonstop-alibi-f82.notion.site/2431862b9eb28060b16ec47a60fcef22?source=copy_link")
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
                    openWebPage("https://nonstop-alibi-f82.notion.site/2431862b9eb2800b95cdcf5daea2e83e?source=copy_link")
                }
            }

            // 동의여부 설정
            uivSettingAgreements.apply {
                setIcon(R.drawable.ic_profile_agree)
                setText(getString(R.string.setting_agreements))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    Timber.d("## [회원정보] 동의여부 설정 클릭")
                    openNotificationSettings()
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
                    sendInquiryMail()
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
                    logoutPopup.show(
                        logoutClickListener = {
                            //
                        }
                    )
                }
            }

            // 회원탈퇴
            uivWithdrawal.apply {
                setIcon(R.drawable.ic_profile_out)
                setText(getString(R.string.withdrawal_string))
                showAppVersion(false)
                showRightArrow(true)
                setOnItemClickListener {
                    withdrawalPopup.show(
                        withdrawalClickListener = {
                            //
                        }
                    )
                }
            }
        }
    }

    private fun sendInquiryMail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("hyg100@naver.com"))
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "문의하기"))
        } catch (e: Exception) {
            WhiplashToast.showErrorToast(this, "이메일 앱을 찾을 수 없습니다")
        }
    }

    private fun openNotificationSettings() {
        // 안드 13+ 에서 알림 권한 확인 및 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    // 권한이 이미 허용됨 - 설정 화면으로 이동
                    openAppNotificationSettings()
                }
                else -> {
                    // 권한이 없는 경우 - 항상 권한 요청을 먼저 시도
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        1001
                    )
                }
            }
        } else {
            // 안드 13 미만에선 바로 설정 화면 이동
            openAppNotificationSettings()
        }
    }

    private fun openAppNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "알림 설정 화면을 열 수 없습니다")
            WhiplashToast.showErrorToast(this, "설정 화면을 열 수 없습니다")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한 허용됨 - 아무것도 하지 않음 (설정 화면으로 이동하지 않음)
                Timber.d("## [회원정보] 알림 권한 허용됨")
                return
            } else {
                // 권한 거절됨 - shouldShowRequestPermissionRationale로 상태 확인
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    // "다시 묻지 않음" 선택됨 - 설정 화면으로 이동
                    openAppNotificationSettings()
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