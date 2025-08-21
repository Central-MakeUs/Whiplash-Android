package com.whiplash.presentation.main

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.whiplash.presentation.R
import com.whiplash.presentation.component.loading.WhiplashLoadingScreen
import com.whiplash.presentation.create_alarm.CreateAlarmActivity
import com.whiplash.presentation.databinding.ActivityMainBinding
import com.whiplash.presentation.dialog.DisableAlarmPopup
import com.whiplash.presentation.user_info.UserInfoActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import com.whiplash.presentation.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import androidx.core.view.isVisible
import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.presentation.alarm.AlarmActivity
import com.whiplash.presentation.component.bottom_sheet.RemoveAlarmBottomSheet
import com.whiplash.presentation.util.WhiplashToast
import java.time.Instant
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * 알람 리사이클러뷰 표시 및 알람 등록 버튼, 상단에 알림 관련 문구 표시 등이 표시되는 메인 화면
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loadingScreen: WhiplashLoadingScreen

    @Inject
    lateinit var disableAlarmPopup: DisableAlarmPopup

    private lateinit var alarmListAdapter: AlarmListAdapter

    private val mainViewModel: MainViewModel by viewModels()

    private var isDeleteMode = false
    private var previousExpandableHeaderVisibility = View.GONE
    private var previousExpandableContentVisibility = View.GONE

    private var removeAlarmBottomSheet: RemoveAlarmBottomSheet? = null

    private var currentTurnOffAlarmId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadingScreen = WhiplashLoadingScreen(this)

        requestNotificationPermission()
        setupExpandableView()
        setupRecyclerView()
        observeMainViewModel()

        with(binding) {
            tvHomeAlarmList.setOnClickListener {
                navigateTo<AlarmActivity> {}
            }

            ivAddAlarm.setOnClickListener {
                navigateTo<CreateAlarmActivity>{}
            }

            ivDotMenu.setOnClickListener {
                showThreeDotMenu()
            }

            tvCancelDelete.setOnClickListener {
                endDeleteMode()
            }
        }
    }

    /**
     * 알림 권한 요청
     */
    private fun requestNotificationPermission() {
        if (PermissionUtils.hasNotificationPermission(this)) {
            Timber.d("## [권한] 알림 권한이 이미 허용됨")
            return
        }

        performNotificationPermissionRequest()
    }

    /**
     * 실제 알림 권한 요청 수행
     */
    private fun performNotificationPermissionRequest() {
        val requested = PermissionUtils.requestNotificationPermission(this)
        if (requested) {
            Timber.d("## [권한] 알림 권한 요청함")
        } else {
            Timber.d("## [권한] 알림 권한 요청 불필요 (Android 13 미만)")
        }
    }

    /**
     * 권한 요청 결과 처리
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        
        // 알림 권한 결과 처리
        PermissionUtils.handleNotificationPermissionResult(
            requestCode = requestCode,
            permissions = permissions,
            grantedResults = grantedResults,
            onPermissionGranted = {
                Timber.d("## [권한] 알림 권한 허용됨")
            },
            onPermissionDenied = {
                Timber.d("## [권한] 알림 권한 거부됨")
            }
        )
    }

    private fun setupRecyclerView() {
        alarmListAdapter = AlarmListAdapter(
            onItemClick = { position ->
                if (isDeleteMode) {
                    alarmListAdapter.toggleSelection(position)
                    showRemoveAlarmBottomSheet()
                }
            },
            onToggleClick = { alarm ->
                // on 상태인 토글 클릭 시 팝업 표시
                // off 상태면 토글 클릭해도 무반응
                showDisableAlarmPopup(alarm)
            }
        )
        binding.rvHomeAlarm.adapter = alarmListAdapter
    }

    private fun showDisableAlarmPopup(alarm: GetAlarmEntity) {
        val remainCount = mainViewModel.uiState.value.remainCount ?: 0
        Timber.d("## [알람 끄기] 남은 알람 끄기 횟수 : $remainCount")

        disableAlarmPopup.show(
            title = getString(R.string.check_in_alarm_disable_title),
            subContent = null,
            count = remainCount,
            disableAlarmClickListener = {
                currentTurnOffAlarmId = alarm.alarmId
                Timber.d("## [알람 끄기] disableAlarmClickListener 호출. currentTurnOffAlarmId : $currentTurnOffAlarmId")
                mainViewModel.turnOffAlarm(
                    alarmId = alarm.alarmId,
                    turnOffAlarmRequestEntity = TurnOffAlarmRequestEntity(
                        clientNow = Instant.now().toString()
                    )
                )
            },
            okClickListener = {
                //
            },
            cancelText = "취소",
            cancelClickListener = {}
        )
    }

    private fun setupExpandableView() {
        var isExpanded = false

        binding.llExpandableHeader.setOnClickListener {
            isExpanded = !isExpanded

            if (isExpanded) {
                binding.llExpandableContent.visibility = View.VISIBLE
                binding.ivExpandArrow.setImageResource(R.drawable.ic_up_arrow_white_22)
            } else {
                binding.llExpandableContent.visibility = View.GONE
                binding.ivExpandArrow.setImageResource(R.drawable.ic_down_arrow_white_22)
            }
            
            updateRecyclerViewPosition()
        }
    }

    private fun observeMainViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.uiState.collect { state ->
                        with(binding) {
                            val isLoading = state.isLoading
                            if (isLoading) {
                                loadingScreen.show()
                            } else {
                                loadingScreen.hide()
                            }

                            val errorMessage = state.errorMessage
                            if (!errorMessage.isNullOrEmpty()) {
                                WhiplashToast.showErrorToast(this@MainActivity, errorMessage)
                            }

                            // 알람 목록 조회
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

                            // 알람 삭제 성공 여부
                            val isAlarmDeleted = state.isAlarmDeleted
                            if (isAlarmDeleted) {
                                WhiplashToast.showSuccessToast(this@MainActivity, "알람 삭제가 완료되었습니다")
                                mainViewModel.resetIsAlarmDeleted()
                            }

                            // 알람 끄기 결과
                            val isAlarmTurnedOff = state.isAlarmTurnedOff
                            if (isAlarmTurnedOff) {
                                // 특정 알람만 토글 off로 변경하고 로컬 알람에서도 제거
                                currentTurnOffAlarmId?.let { alarmId ->
                                    alarmListAdapter.updateAlarmToggleState(alarmId, false)
                                    cancelLocalAlarm(alarmId)
                                    Timber.d("## [알람 끄기] api 호출 성공. alarmId : $alarmId, uiState 값 초기화")
                                }
                                mainViewModel.resetIsAlarmTurnedOff()
                                mainViewModel.getAlarms()
                                currentTurnOffAlarmId = null
                            }
                        }
                    }
                }
            }
        }
    }

    // 여러 요일에 알람이 울리는 경우 알람을 끈 날(=오늘)에만 알람이 울리지 않게 취소
    private fun cancelLocalAlarm(alarmId: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 현재 요일 확인 (Calendar.SUNDAY = 1, Calendar.MONDAY = 2...)
        val calendar = java.util.Calendar.getInstance()
        val currentDayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK)

        // AlarmSchedulerRepositoryImpl에서 사용하는 것과 같은 requestCode 패턴
        // alarmId는 Int만 사용할 수 있어서 Int 캐스팅은 필수
        val requestCode = alarmId.toInt() * 10 + currentDayOfWeek

        val intent = Intent("com.whiplash.akuma.ALARM_TRIGGER").apply {
            component = ComponentName("com.whiplash.akuma", "com.whiplash.akuma.alarm.AlarmReceiver")
            putExtra("alarmId", alarmId.toInt())
            putExtra("dayOfWeek", currentDayOfWeek)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 끄려는 요일의 알람만 취소
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()

        val dayNames = mapOf(
            java.util.Calendar.SUNDAY to "일",
            java.util.Calendar.MONDAY to "월",
            java.util.Calendar.TUESDAY to "화",
            java.util.Calendar.WEDNESDAY to "수",
            java.util.Calendar.THURSDAY to "목",
            java.util.Calendar.FRIDAY to "금",
            java.util.Calendar.SATURDAY to "토"
        )

        Timber.d("## [AlarmManager] 특정 요일 알람 취소 완료. alarmId : $alarmId, 요일 : ${dayNames[currentDayOfWeek]}($currentDayOfWeek), requestCode : $requestCode")
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
            startDeleteMode()
        }

        tvManageUserInfo.setOnClickListener {
            popupWindow.dismiss()
            Timber.d("## [팝업] 회원 정보 클릭")
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

    private fun showRemoveAlarmBottomSheet() {
        if (removeAlarmBottomSheet?.isVisible == true) return

        val bottomSheet = RemoveAlarmBottomSheet.newInstance(
            onRemoveReasonSelectedListener = { reason ->
                invokeAlarmRemove(reason)
            },
            onDismissListener = {
                // 바텀 시트가 취소 버튼으로 닫힐 때 삭제할 알람 선택 상태 해제
                alarmListAdapter.clearSelection()
            }
        )
        bottomSheet.show(supportFragmentManager, "RemoveAlarmBottomSheet")
    }

    private fun invokeAlarmRemove(reason: String) {
        val selectedAlarm = alarmListAdapter.getSelectedAlarm()
        Timber.d("## 알람 삭제 사유 : $reason, 선택된 알람 : $selectedAlarm")

        selectedAlarm?.let { alarm ->
            mainViewModel.deleteAlarm(
                alarmId = alarm.alarmId,
                reason = reason
            )
        } ?: run {
            WhiplashToast.showErrorToast(this@MainActivity, "존재하지 않는 알람입니다. 다시 시도해 주세요")
        }

        endDeleteMode()
    }

    private fun startDeleteMode() {
        isDeleteMode = true
        
        with(binding) {
            // 현재 expandable view들의 가시성 저장
            previousExpandableHeaderVisibility = llExpandableHeader.visibility
            previousExpandableContentVisibility = llExpandableContent.visibility

            // expandable view 숨기기
            llExpandableHeader.visibility = View.GONE
            llExpandableContent.visibility = View.GONE

            // 취소 버튼 표시
            tvCancelDelete.visibility = View.VISIBLE

            // 리사이클러뷰 위치를 tvCancelDelete 아래로 조정
            updateRecyclerViewPosition()
        }

        alarmListAdapter.setDeleteMode(true)
    }

    private fun endDeleteMode() {
        isDeleteMode = false

        with(binding) {
            // 취소 버튼 숨기기
            tvCancelDelete.visibility = View.GONE

            // expandable view 이전 상태로 복원
            llExpandableHeader.visibility = previousExpandableHeaderVisibility
            llExpandableContent.visibility = previousExpandableContentVisibility

            // 리사이클러뷰 위치 복원
            updateRecyclerViewPosition()
        }

        alarmListAdapter.setDeleteMode(false)
    }

    private fun updateRecyclerViewPosition() {
        val params = binding.glRvTop.layoutParams as ConstraintLayout.LayoutParams

        when {
            binding.tvCancelDelete.isVisible -> {
                // 삭제 모드일 때
                params.guideBegin = 150.dpToPx()
            }
            binding.llExpandableContent.isVisible -> {
                // expandable content가 보일 때
                params.guideBegin = 350.dpToPx()
            }
            binding.llExpandableHeader.isVisible -> {
                // header만 보일 때
                params.guideBegin = 150.dpToPx()
            }
            else -> {
                // 기본 상태
                params.guideBegin = 108.dpToPx()
            }
        }

        binding.glRvTop.layoutParams = params
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()

    override fun onResume() {
        super.onResume()
        mainViewModel.getAlarms()
        mainViewModel.getRemainingDisableCount()
    }

}