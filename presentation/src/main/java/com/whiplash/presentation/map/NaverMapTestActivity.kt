package com.whiplash.presentation.map

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityMainBinding
import com.whiplash.presentation.databinding.ActivityNaverMapTestBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 네이버 지도 테스트용
 */
@AndroidEntryPoint
class NaverMapTestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNaverMapTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityNaverMapTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.naver_map_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}