package com.whiplash.presentation.create_alarm

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.whiplash.domain.entity.alarm.request.AddAlarmRequestEntity
import com.whiplash.domain.repository.alarm.AlarmSchedulerRepository
import com.whiplash.presentation.R
import com.whiplash.presentation.component.bottom_sheet.AlarmSoundBottomSheet
import com.whiplash.presentation.component.loading.WhiplashLoadingScreen
import com.whiplash.presentation.databinding.ActivityCreateAlarmBinding
import com.whiplash.presentation.main.MainViewModel
import com.whiplash.presentation.map.SelectPlaceActivity
import com.whiplash.presentation.search_place.SearchPlaceActivity
import com.whiplash.presentation.util.WhiplashToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

/**
 * [com.whiplash.presentation.main.MainActivity]에서 상단 오른쪽 흰색 + 클릭 후 이동하는 알람 설정 화면
 *
 * 장소 선택, 알람 목적, 휠을 돌려서 알람 시간 설정, 반복 여부(월~일), 푸시 알림 여부를 설정
 */
@AndroidEntryPoint
class CreateAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAlarmBinding
    private lateinit var loadingScreen: WhiplashLoadingScreen

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var alarmScheduler: AlarmSchedulerRepository

    private val placeSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val detailAddress = result.data?.getStringExtra("detailAddress") ?: ""
            val latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0

            Timber.d("## [장소 선택 완료] 알람 설정 화면에 돌아와서 확인한 주소 : $detailAddress, 위도 : $latitude, 경도 : $longitude")
            if (detailAddress.isNotEmpty()) {
                binding.tvSearch.text = detailAddress
                binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.grey_50))
            }
            mainViewModel.setSelectedPlace(detailAddress, latitude, longitude)
        }
    }

    private var alarmSoundBottomSheet: AlarmSoundBottomSheet? = null

    // 알람 소리 바텀시트에서 선택한 알람. 기본값 "알람 소리1"
    private var selectedAlarmSoundId: Int = -1
    private var selectedAlarmSoundText: String = "" // 화면에 표시할 텍스트
    private var selectedAlarmSoundApiText: String = "" // 알람 등록 api로 넘길 텍스트

    private var isAlarmScheduled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_alarm_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeMainViewModel()
        setupView()
    }

    private fun observeMainViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.uiState.collect { state ->
                        if (state.isLoading) {
                            loadingScreen.show()
                        } else {
                            loadingScreen.hide()
                        }

                        if (state.errorMessage?.isNotEmpty() == true) {
                            WhiplashToast.showErrorToast(this@CreateAlarmActivity, state.errorMessage)
                        }

                        // 알람 생성 결과는 1번만 처리
                        if (state.isAlarmCreated && !isAlarmScheduled) {
                            isAlarmScheduled = true
                            // 서버에 알람 등록 성공 후 로컬 알람 스케줄링
                            scheduleLocalAlarm()
                            WhiplashToast.showSuccessToast(this@CreateAlarmActivity, getString(R.string.alarm_created))
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun setupView() {
        loadingScreen = WhiplashLoadingScreen(this)

        with(binding) {
            setupExpandableView()
            setRepeatDay()
            setTimePickers()

            whCreateAlarm.setTitle(getString(R.string.create_alarm_header))

            selectedAlarmSoundText = getString(R.string.sound_1)
            selectedAlarmSoundApiText = "알람 소리1"
            tvAlarmSoundDetail.text = selectedAlarmSoundText

            // 도착 목표 장소는?
            clSelectPlaceContainer.setOnClickListener {
                placeSelectionLauncher.launch(
                    Intent(this@CreateAlarmActivity, SearchPlaceActivity::class.java)
                )
            }

            // 지도에서 찾기
            clSearchInMapContainer.setOnClickListener {
                placeSelectionLauncher.launch(
                    Intent(this@CreateAlarmActivity, SelectPlaceActivity::class.java)
                )
            }

            // 알람 소리 설정
            llAlarmSound.setOnClickListener {
                // 알람 소리 선택, 미리듣기 뷰가 있는 바텀 시트 프래그먼트 표시
                showAlarmSoundBottomSheet()
            }

            // 저장하기
            btnSaveAlarm.setOnClickListener {
                val time = getSelectedTime24Hour()
                Timber.d("## [시간] 24시간 형식: $time")
                val selectedDays = getSelectedDays()
                val detailAddress = mainViewModel.uiState.value.selectedPlace?.detailAddress
                val latitude = mainViewModel.uiState.value.selectedPlace?.latitude
                val longitude = mainViewModel.uiState.value.selectedPlace?.longitude

                mainViewModel.addAlarm(
                    request = AddAlarmRequestEntity(
                        address = detailAddress ?: "",
                        latitude = latitude ?: 0.0,
                        longitude = longitude ?: 0.0,
                        alarmPurpose = binding.etAlarmPurpose.getText(),
                        time = time,
                        repeatDays = selectedDays,
                        soundType = selectedAlarmSoundApiText
                    )
                )
            }
        }
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
        }
    }

    private fun setRepeatDay() {
        // 월~일
        val dayButtons = arrayOf(
            binding.btnMon, binding.btnTue, binding.btnWed, binding.btnThur,
            binding.btnFri, binding.btnSat, binding.btnSun
        )
        val dayNames = resources.getStringArray(R.array.day_names_korean)

        dayButtons.forEachIndexed { index, button ->
            button.setText(dayNames[index])
        }
    }

    private fun getSelectedDays(): List<String> {
        val selectedDays = mutableListOf<String>()
        val dayButtons = arrayOf(
            binding.btnMon, binding.btnTue, binding.btnWed, binding.btnThur,
            binding.btnFri, binding.btnSat, binding.btnSun
        )
        val dayNames = resources.getStringArray(R.array.day_names_korean)

        dayButtons.forEachIndexed { index, button ->
            if (button.isSelected) {
                selectedDays.add(dayNames[index])
            }
        }

        return selectedDays
    }

    private fun setTimePickers() {
        with(binding) {
            val pretendardTypeFace = ResourcesCompat.getFont(this@CreateAlarmActivity, R.font.pretendard_bold)
            val paperlogyTypeFace = ResourcesCompat.getFont(this@CreateAlarmActivity, R.font.paperlogy_bold)

            // 오전 / 오후 설정
            npAmPm.apply {
                displayedValues = resources.getStringArray(R.array.am_pm_values)
                setSelectedTypeface(pretendardTypeFace)
            }

            // 시간 설정
            npHours.apply {
                setSelectedTypeface(paperlogyTypeFace)
            }

            // 분 설정
            npMinutes.apply {
                setSelectedTypeface(paperlogyTypeFace)
            }
        }

        setCurrentTime()
    }

    /**
     * 현재 시간으로 NumberPicker 초기화
     */
    private fun setCurrentTime() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        if (hour >= 12) {
            binding.npAmPm.value = 1
            binding.npHours.value = if (hour == 12) 12 else hour - 12
        } else {
            binding.npAmPm.value = 0
            binding.npHours.value = if (hour == 0) 12 else hour
        }

        binding.npMinutes.value = minute
    }

    /**
     * 현재 선택된 시간을 24시간 형식(18:30)으로 가져옴
     */
    private fun getSelectedTime24Hour(): String {
        val isAmSelected = binding.npAmPm.value == 0
        val hour12 = binding.npHours.value
        val minute = binding.npMinutes.value

        val hour24 = when {
            isAmSelected && hour12 == 12 -> 0       // 오전 12시 -> 0시
            isAmSelected -> hour12                  // 오전 1-11시 -> 1-11시
            !isAmSelected && hour12 == 12 -> 12     // 오후 12시 -> 12시
            else -> hour12 + 12                     // 오후 1-11시 -> 13-23시
        }

        return String.format("%02d:%02d", hour24, minute)
    }

    private fun showAlarmSoundBottomSheet() {
        if (alarmSoundBottomSheet?.isVisible == true) return

        val bottomSheetFragment = AlarmSoundBottomSheet.newInstance(
            onAlarmSoundSelected = { displayText, selectedId, apiText ->
                binding.tvAlarmSoundDetail.text = displayText
                selectedAlarmSoundId = selectedId
                selectedAlarmSoundText = displayText
                selectedAlarmSoundApiText = apiText
            },
            selectedRadioButtonId = selectedAlarmSoundId
        )
        bottomSheetFragment.show(supportFragmentManager, "AlarmSoundBottomSheet")
    }

    private fun scheduleLocalAlarm() {
        val time = getSelectedTime24Hour()
        val selectedDays = getSelectedDays()
        val detailAddress = mainViewModel.uiState.value.selectedPlace?.detailAddress ?: ""
        val alarmPurpose = binding.etAlarmPurpose.getText()
        val latitude = mainViewModel.uiState.value.selectedPlace?.latitude ?: 0.0
        val longitude = mainViewModel.uiState.value.selectedPlace?.longitude ?: 0.0

        val alarmId = (detailAddress + alarmPurpose + time + selectedDays.joinToString()).hashCode()

        alarmScheduler.scheduleAlarm(
            alarmId = alarmId,
            time = time,
            repeatDays = selectedDays,
            alarmPurpose = alarmPurpose,
            address = detailAddress,
            soundType = selectedAlarmSoundApiText,
            latitude = latitude,
            longitude = longitude,
        )
    }

}