package com.whiplash.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.whiplash.presentation.R
import com.whiplash.presentation.create_alarm.CreateAlarmActivity
import com.whiplash.presentation.databinding.ActivityMainBinding
import com.whiplash.presentation.dialog.DisableAlarmPopup
import com.whiplash.presentation.user_info.UserInfoActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * 알람 리사이클러뷰 표시 및 알람 등록 버튼, 상단에 알림 관련 문구 표시 등이 표시되는 메인 화면
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var disableAlarmPopup: DisableAlarmPopup

    private lateinit var alarmListAdapter: AlarmListAdapter

    private val mainViewModel: MainViewModel by viewModels()

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

        setupRecyclerView()
        observeMainViewModel()

        with(binding) {
            setHomeAlertPopupTexts()

            ivAddAlarm.setOnClickListener {
                navigateTo<CreateAlarmActivity>{}
            }

            ivDotMenu.setOnClickListener {
                showThreeDotMenu()
            }
        }

        mainViewModel.getAlarms()
    }

    private fun setupRecyclerView() {
        alarmListAdapter = AlarmListAdapter()
        binding.rvHomeAlarm.adapter = alarmListAdapter
    }

    private fun observeMainViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.uiState.collect { state ->
                        with(binding) {
                            // 알람 목록 조회(현재는 mock data)
                            val alarmList = state.alarmList
                            Timber.d("## [알람 목록 조회] 액티비티에서 확인 : $state")
                            if (alarmList.isEmpty()) {
                                rvHomeAlarm.visibility = View.GONE
                                wevHome.visibility = View.VISIBLE
                            } else {
                                rvHomeAlarm.visibility = View.VISIBLE
                                alarmListAdapter.submitList(alarmList)
                                wevHome.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setHomeAlertPopupTexts() {
        binding.homeAlert.setAlertTexts(
            firstText = getString(R.string.home_alert_first_text),
            secondText = getString(R.string.home_alert_second_text),
        )
    }

    private fun showThreeDotMenu() {
        val popupView = LayoutInflater.from(this).inflate(R.layout.menu_home_popup, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // popupWindow는 뷰 바인딩 대상이 아니라 findViewById()로 내부 뷰에 접근
        val tvRemoveAlarm = popupView.findViewById<TextView>(R.id.tvRemoveAlarm)
        val tvManageUserInfo = popupView.findViewById<TextView>(R.id.tvManageUserInfo)

        tvRemoveAlarm.setOnClickListener {
            popupWindow.dismiss()
            Timber.d("## [팝업] 알람 삭제 클릭")
        }

        tvManageUserInfo.setOnClickListener {
            popupWindow.dismiss()
            Timber.d("## [팝업] 회원 정보 관리 클릭")
            navigateTo<UserInfoActivity> {}
        }

        // 화면의 전체 width 조회
        val screenWidth = resources.displayMetrics.widthPixels
        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = popupView.measuredWidth

        // 화면에서 ivDotMenu의 위치
        val location = IntArray(2)
        binding.ivDotMenu.getLocationOnScreen(location)
        val anchorLeft = location[0]

        // xOffset을 계산해서 오른쪽에서 20dp 떨어진 곳에 popupWindow 표시
        val xOffset =
            screenWidth - (anchorLeft + popupWidth) - (20 * resources.displayMetrics.density).toInt()

        popupWindow.showAsDropDown(binding.ivDotMenu, xOffset, 0)
    }

}