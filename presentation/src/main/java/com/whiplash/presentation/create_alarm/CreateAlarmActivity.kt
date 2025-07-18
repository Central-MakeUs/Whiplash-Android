package com.whiplash.presentation.create_alarm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityCreateAlarmBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class CreateAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAlarmBinding

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
            btnMon.setText("월")
            btnTue.setText("화")
            btnWed.setText("수")
            btnThur.setText("목")
            btnFri.setText("금")
            btnSat.setText("토")
            btnSun.setText("일")

            // 토글은 기본적으로 체크 상태
            tgPushAlarm.setChecked(true)

            setCreateAlarmAlertTexts()
            setTimePickers()
        }
    }

    private fun setTimePickers() {
        with(binding) {
            // 오전 / 오후 설정
            npAmPm.apply {
                displayedValues = arrayOf("오전", "오후")
                minValue = 0
                maxValue = 1
                value = 0 // 오전이 기본값
            }

            // 시간 설정
            npHours.apply {
                minValue = 1
                maxValue = 12
                value = 0
            }

            // 분 설정
            npMinutes.apply {
                minValue = 0
                maxValue = 59
                value = 0
                setFormatter { value -> String.format("%02d", value)}
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
        val amPm = if (binding.npAmPm.value == 0) "오전" else "오후"
        val hour = binding.npHours.value
        val minute = binding.npMinutes.value

        return Triple(amPm, hour, minute)
    }

    private fun setCreateAlarmAlertTexts() {
        binding.createAlarmAlert.setAlertTexts(
            firstText = "지금 설정한 알람은 삭제할 수 없어요!",
            secondText = "소리가 매우 자극적일 수 있어요!"
        )
    }

}