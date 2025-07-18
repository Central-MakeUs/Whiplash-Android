package com.whiplash.presentation.map

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivitySelectPlaceBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * 장소 선택 화면
 */
@AndroidEntryPoint
class SelectPlaceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectPlaceBinding

    private lateinit var naverMap: NaverMap

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

        setupNaverMap()
        setupView()
    }

    private fun setupView() {
        with(binding) {
            whSelectPlace.setTitle(getString(R.string.select_place_header))
        }
    }

    private fun setupNaverMap() {
        val fm = supportFragmentManager
        val naverMapFragment = fm.findFragmentById(R.id.fcvNaverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.fcvNaverMap, it).commit()
            }
        naverMapFragment.getMapAsync(this)
    }

    @UiThread
    override fun onMapReady(map: NaverMap) {
        naverMap = map
    }

}