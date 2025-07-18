package com.whiplash.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.geometry.LatLng
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivitySelectPlaceBinding
import com.whiplash.presentation.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * 장소 선택 화면
 */
@AndroidEntryPoint
class SelectPlaceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectPlaceBinding

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // FusedLocationProviderClient와 지자기, 가속도 센서를 활용해 유저 위치를 리턴하는 구현체
    private lateinit var locationSource: FusedLocationSource

    companion object {
        /**
         * @see onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

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

        setupUserLocationSource()

        if (PermissionUtils.hasLocationPermission(this)) {
            setupNaverMap()
        } else {
            PermissionUtils.requestLocationPermissions(this)
        }

        setupView()
    }

    private fun setupUserLocationSource() {
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        naverMap.apply {
            locationSource = locationSource
            uiSettings.isLocationButtonEnabled = true
            locationOverlay.isVisible = true
        }

        // 네이버 지도를 사용하면 내가 만든 권한 요청 함수 대신 아래 코드를 사용하라는 컴파일 에러가 표시되어 아래 로직 사용
        // 앱 실행에 오류는 없지만 이 로직을 사용하기로 함
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // 마지막으로 알려진 위치
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                // 위치 값을 받아오면 내 위치에 마커 표시
                val latLng = LatLng(it.latitude, it.longitude)
                naverMap.apply {
                    moveCamera(CameraUpdate.scrollTo(latLng))
                    locationOverlay.position = latLng
                    locationOverlay.isVisible = true
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        PermissionUtils.handleLocationPermissionResult(
            requestCode,
            permissions,
            grantResults,
            onAllPermissionsGranted = {
                setupNaverMap()
            },
            onPartialPermissionsGranted = {
                setupNaverMap()
            },
            onPermissionDenied = {
                // 권한 거부
            }
        )
        if (::naverMap.isInitialized && requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (!locationSource.isActivated) {
                naverMap.locationOverlay.isVisible = false
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}