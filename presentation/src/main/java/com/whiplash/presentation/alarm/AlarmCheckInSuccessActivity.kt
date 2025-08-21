package com.whiplash.presentation.alarm

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityAlarmCheckInSuccessBinding
import com.whiplash.presentation.main.MainActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint

/**
 * 장소 도착 인증 성공 시 이동하는 화면
 */
@AndroidEntryPoint
class AlarmCheckInSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmCheckInSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmCheckInSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.alarm_check_in_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.whCheckInSuccess.setTitle(getString(R.string.success_check_in_header))

        val address = intent.getStringExtra("address")
        binding.tvCheckInSuccessAddress.text = address

        binding.btnRegisterAddress.setOnClickListener {
            navigateTo<MainActivity> {
                finishCurrentActivity()
            }
        }
    }
}