package com.whiplash.presentation.map

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivitySelectPlaceBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 장소 선택 화면
 */
@AndroidEntryPoint
class SelectPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelectPlaceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.naver_map_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupView()
    }

    private fun setupView() {
        with(binding) {
            whSelectPlace.setTitle(getString(R.string.select_place_header))
        }
    }

}