package com.whiplash.presentation.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
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
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
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

    private var bottomSheetFragment: PlaceBottomSheetFragment? = null

    companion object {
        /**
         * @see onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    // 마커 표시할 위경도
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

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

        getDataFromIntent()
        setupView()
    }

    private fun getDataFromIntent() {
        // FIXME : 테스트 때문에 임시값 사용. 실제 값 받게 되면 그걸 사용한다
        latitude = 37.498095
        longitude = 127.027610

        // 인텐트에서 받아온 데이터
        // placeAddress = intent.getStringExtra("address")
        // placeLatitude = intent.getDoubleExtra("latitude", 0.0)
        // placeLongitude = intent.getDoubleExtra("longitude", 0.0)
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
        }

        // 전달받은 위치에 마커 + "여기서 알람 끄기!" 표시하면서 카메라 이동
        if (latitude != 0.0 && longitude != 0.0) {
            val targetLocation = LatLng(latitude, longitude)
            naverMap.moveCamera(CameraUpdate.scrollTo(targetLocation))

            createCustomMarker(targetLocation, getString(R.string.disable_alarm_here))
        } else {
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

        showPlaceBottomSheet()
    }

    private fun showPlaceBottomSheet() {
        // 이미 바텀 시트가 표시되고 있으면 중복 표시하지 않는다
        if (bottomSheetFragment?.isVisible == true) {
            return
        }

        // FIXME : 테스트 값 대신 실제값 사용하게 수정
        val bottomSheetFragment = PlaceBottomSheetFragment.newInstance(
            address = "구리시 갈매동",
            detailAddress = "경기 구리시 갈매동"
        )
        bottomSheetFragment.show(supportFragmentManager, "PlaceBottomSheetFragment")
    }

    // 커스텀 마커 뷰 생성 (텍스트뷰 + 마커 이미지를 세로로 배치)
    private fun createCustomMarker(position: LatLng, address: String) {
        val marker = Marker()
        marker.position = position

        // LinearLayout에 텍스트뷰 + 이미지뷰를 세로로 배치
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
        }

        // 텍스트뷰 동적 생성
        val textView = createMarkerTextView(address)
        linearLayout.addView(textView)

        // 마커 이미지뷰 생성
        val imageView = ImageView(this).apply {
            setImageResource(R.drawable.ic_place_marker_48)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        linearLayout.addView(imageView)

        marker.icon = OverlayImage.fromView(linearLayout)
        marker.map = naverMap
    }

    // 마커 위에 표시할 텍스트뷰 동적 생성
    private fun createMarkerTextView(text: String): TextView {
        val textView = TextView(this)
        textView.apply {
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams = params
            this.text = text
            textSize = 14f
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundResource(R.drawable.bg_marker_text)
            setPadding(
                resources.getDimensionPixelSize(R.dimen.dp_10),
                resources.getDimensionPixelSize(R.dimen.dp_4),
                resources.getDimensionPixelSize(R.dimen.dp_10),
                resources.getDimensionPixelSize(R.dimen.dp_4)
            )
            gravity = android.view.Gravity.CENTER
        }

        return textView
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