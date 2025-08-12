package com.whiplash.presentation.alarm

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.CircleOverlay
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivityAlarmBinding
import com.whiplash.presentation.main.MainViewModel
import com.whiplash.presentation.search_place.SearchPlaceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 기기 상단에 표시되는 알람 클릭 시 이동하는 화면
 *
 * 네이버 지도를 표시하고 알람이 울리는 시간을 이미지뷰 위에 표시, 봐주세요 + 장소 인증하기 버튼 있음
 */
@AndroidEntryPoint
class AlarmActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAlarmBinding
    private val mainViewModel: MainViewModel by viewModels()
    private val searchPlaceViewModel: SearchPlaceViewModel by viewModels()

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // FusedLocationProviderClient와 지자기, 가속도 센서를 활용해 유저 위치를 리턴하는 구현체
    private lateinit var locationSource: FusedLocationSource

    // 마커 표시할 위경도
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private var hour: Int = 0
    private var minute: Int = 0

    // 화면 중앙에 고정된 마커, 원형 오버레이
    private var centerMarker: Marker? = null
    private var centerCircleOverlay: CircleOverlay? = null
    private var locationUpdateJob: Job? = null

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
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        hour = intent.getIntExtra("hour", 0)
        minute = intent.getIntExtra("minute", 0)
        Timber.e("## [알람 울림] alarmId : $alarmId, alarmPurpose : $alarmPurpose, address : $address")
        Timber.e("## [알람 울림] 위도 : $latitude, 경도 : $longitude, 시간 : $hour, 분 : $minute")

        searchPlaceViewModel.getPlaceDetail(latitude, longitude)
        observeSearchPlaceVieWModel()
        setupNaverMap()
        setupView()
    }

    private fun observeSearchPlaceVieWModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    searchPlaceViewModel.uiState.collect { state ->
                        // 위경도 보내서 얻은 장소 조회 결과
                        val placeDetail = state.placeDetail
                        if (placeDetail != null) {
                            val detailAddress = placeDetail.name
                            binding.tvLocalAlarmAddress.text = detailAddress
                        }
                    }
                }
            }
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

    private fun setupView() {
        with(binding) {
            val hourString = formatHour24(hour)
            val minuteString = formatMinute(minute)

            tvLocalAlarmTime.text = hourString
            tvLocalAlarmMinute.text = minuteString

            btnCancelLocalAlarm.setOnClickListener {
                // 봐주세요 클릭 시 바텀 시트 표시
            }

            btnCheckIn.setOnClickListener {
                // 장소 인증 api 호출. 호출 성공 시 알람 끄고 도착 인증 화면으로 이동
            }

//            root.setOnClickListener {
//                // 알람 정지 브로드캐스트 전송
//                val stopIntent = Intent("com.whiplash.akuma.STOP_ALARM").apply {
//                    putExtra("alarmId", intent.getIntExtra("alarmId", -1))
//                }
//                sendBroadcast(stopIntent)
//                finish()
//            }
        }
    }

    private fun formatHour24(hour: Int): String = String.format("%02d", hour)

    private fun formatMinute(minute: Int): String = String.format("%02d", minute)

    @UiThread
    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.apply {
            isNightModeEnabled = true
            mapType = NaverMap.MapType.Navi // Navi 유형만 다크모드를 지원해서 Navi 타입으로 설정해야 함
            locationSource = locationSource
            uiSettings.isLocationButtonEnabled = true
        }

        if (latitude != 0.0 && longitude != 0.0) {
            val targetLocation = LatLng(latitude, longitude)
            naverMap.moveCamera(CameraUpdate.scrollTo(targetLocation))
            setupCenterMarkerAndCircle()
        } else {
            getCurrentLocationAndSetup()
        }
    }

    private fun setupCenterMarkerAndCircle() {
        val centerPosition = naverMap.cameraPosition.target
        createCenterMarker(centerPosition)
        createCenterCircleOverlay(centerPosition)
    }

    private fun createCenterMarker(position: LatLng) {
        // 기존 마커가 있으면 제거
        centerMarker?.map = null

        centerMarker = Marker().apply {
            this.position = position

            // LinearLayout에 텍스트뷰 + 이미지뷰를 세로로 배치
            val linearLayout = LinearLayout(this@AlarmActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }

            // 텍스트뷰 동적 생성
            val textView = createMarkerTextView(getString(R.string.disable_alarm_here))
            linearLayout.addView(textView)

            // 마커 이미지뷰 생성
            val imageView = ImageView(this@AlarmActivity).apply {
                setImageResource(R.drawable.ic_place_marker_48)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = -textView.layoutParams.height - resources.getDimensionPixelSize(R.dimen.dp_8)
                }
            }
            linearLayout.addView(imageView)

            icon = OverlayImage.fromView(linearLayout)
            anchor = PointF(0.5f, 0.8f)
            map = naverMap
        }
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

    private fun createCenterCircleOverlay(position: LatLng) {
        // 기존 원형 오버레이가 있으면 제거
        centerCircleOverlay?.map = null

        centerCircleOverlay = CircleOverlay().apply {
            center = position
            radius = 100.0
            color = resources.getColor(R.color.lemon_10p, null)
            outlineColor = resources.getColor(R.color.lemon_10p, null)
            outlineWidth = resources.getDimensionPixelSize(R.dimen.dp_1)
            map = naverMap
        }
    }

    private fun getCurrentLocationAndSetup() {
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

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                latitude = it.latitude
                longitude = it.longitude
                naverMap.apply {
                    moveCamera(CameraUpdate.scrollTo(latLng))
                    locationOverlay.position = latLng
                    locationOverlay.isVisible = true
                }
                setupCenterMarkerAndCircle()
            }
        }
    }

}