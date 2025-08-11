package com.whiplash.presentation.alarm

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityAlarmBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alarm_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val alarmId = intent.getIntExtra("alarmId", -1)
        val alarmPurpose = intent.getStringExtra("alarmPurpose") ?: ""
        val address = intent.getStringExtra("address") ?: ""
        Timber.e("## [알람 울림] alarmId : $alarmId, alarmPurpose : $alarmPurpose, address : $address")
    }

    private fun setupAlarmUI(purpose: String, address: String) {
        with(binding) {
//            tvAlarmPurpose.text = purpose
//            tvAlarmAddress.text = address

            root.setOnClickListener {
                // 알람 정지 브로드캐스트 전송
                val stopIntent = Intent("com.whiplash.akuma.STOP_ALARM").apply {
                    putExtra("alarmId", intent.getIntExtra("alarmId", -1))
                }
                sendBroadcast(stopIntent)
                finish()
            }
        }
    }

}