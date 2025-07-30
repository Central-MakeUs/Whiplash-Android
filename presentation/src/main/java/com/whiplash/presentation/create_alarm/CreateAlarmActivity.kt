package com.whiplash.presentation.create_alarm

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityCreateAlarmBinding
import com.whiplash.presentation.main.MainViewModel
import com.whiplash.presentation.map.SelectPlaceActivity
import com.whiplash.presentation.search_place.SearchPlaceActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
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

    private val mainViewModel: MainViewModel by viewModels()

    private val placeSelectionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val detailAddress = result.data?.getStringExtra("detailAddress") ?: ""
            val latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0

            Timber.d("## [장소 선택 완료] 주소: $detailAddress, 위도: $latitude, 경도: $longitude")
            if (detailAddress.isNotEmpty()) {
                binding.tvSearch.text = detailAddress
                binding.tvSearch.setTextColor(ContextCompat.getColor(this, R.color.grey_50))
            }
            mainViewModel.setSelectedPlace(detailAddress, latitude, longitude)
        }
    }

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

        setupView()
    }

    private fun setupView() {
        with(binding) {
            setupExpandableView()
            setRepeatDay()
            setTimePickers()

            whCreateAlarm.setTitle(getString(R.string.create_alarm_header))

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

            btnSaveAlarm.setOnClickListener {
                val time = getSelectedTime()
                Timber.d("## [시간] 오전 / 오후 : ${time.first}, 시 : ${time.second}, 분 : ${time.third}")
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
     * 현재 선택된 시간 가져옴
     */
    private fun getSelectedTime(): Triple<String, Int, Int> {
        val amPm = if (binding.npAmPm.value == 0) getString(R.string.time_am) else getString(R.string.time_pm)
        val hour = binding.npHours.value
        val minute = binding.npMinutes.value

        return Triple(amPm, hour, minute)
    }

}