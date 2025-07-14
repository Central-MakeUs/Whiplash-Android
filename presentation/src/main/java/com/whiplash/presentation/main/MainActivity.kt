package com.whiplash.presentation.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 알람 리사이클러뷰 표시 및 알람 등록 버튼, 상단에 알림 관련 문구 표시 등이 표시되는 메인 화면
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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
}