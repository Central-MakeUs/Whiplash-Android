package com.whiplash.presentation.alarm

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
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
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import com.whiplash.presentation.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.core.view.isVisible
import com.whiplash.domain.entity.alarm.request.TurnOffAlarmRequestEntity
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * 기기 상단에 표시되는 알람 클릭 시 이동하는 화면
 *
 * 네이버 지도를 표시하고 알람이 울리는 시간을 이미지뷰 위에 표시, 봐주세요 + 장소 인증하기 버튼 있음
 */
@AndroidEntryPoint
class AlarmActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        /**
         * @see onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

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
    private var alarmId: Long = 0
    private var address: String = ""

    // 화면 중앙에 고정된 마커, 원형 오버레이
    private var centerMarker: Marker? = null
    private var centerCircleOverlay: CircleOverlay? = null
    private var locationUpdateJob: Job? = null

    // 알람 끄기 위치 확인 로딩 바텀시트
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var disableAlarmBehavior: BottomSheetBehavior<View>? = null

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

        onBackPressedDispatcher.addCallback(this) {
            // 안드 13+ 대응
            // 이 화면에선 뒤로가기 버튼을 눌러도 뒤로 이동하지 않음
        }

        setupUserLocationSource()
        setupBottomSheet()
        setupDisableAlarmBottomSheet()

        alarmId = intent.getIntExtra("alarmId", -1).toLong()
        val alarmPurpose = intent.getStringExtra("alarmPurpose") ?: ""
        address = intent.getStringExtra("address") ?: ""
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        hour = intent.getIntExtra("hour", 0)
        minute = intent.getIntExtra("minute", 0)
        Timber.e("## [알람 울림] alarmId : $alarmId, alarmPurpose : $alarmPurpose, address : $address")
        Timber.e("## [알람 울림] 위도 : $latitude, 경도 : $longitude, 시간 : $hour, 분 : $minute")

        searchPlaceViewModel.getPlaceDetail(latitude, longitude)
        observeMainViewModel()
        observeSearchPlaceVieWModel()
        setupNaverMap()
        setupView()
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.checkInBottomSheet)
        bottomSheetBehavior?.apply {
            isDraggable = false
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        // 처음에는 바텀시트 숨김
        binding.checkInBottomSheet.visibility = View.GONE
    }

    private fun setupDisableAlarmBottomSheet() {
        disableAlarmBehavior = BottomSheetBehavior.from(binding.disableAlarmBottomSheet)
        disableAlarmBehavior?.apply {
            isDraggable = false
            isHideable = true
            state = BottomSheetBehavior.STATE_HIDDEN
        }

        binding.disableAlarmBottomSheet.visibility = View.GONE
    }

    private fun observeMainViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainViewModel.uiState.collect { state ->
                        // 장소 인증 로딩 시에만 checkInBottomSheet 표시
                        if (state.isLoading && !state.isAlarmCheckedIn) {
                            binding.checkInBottomSheet.visibility = View.VISIBLE
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
                            // disableAlarmBottomSheet는 숨김
                            disableAlarmBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                            binding.disableAlarmBottomSheet.visibility = View.GONE
                        } else if (!state.isLoading && binding.checkInBottomSheet.isVisible) {
                            // 장소 인증 로딩이 끝났을 때만 checkInBottomSheet 숨김
                            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                            binding.checkInBottomSheet.visibility = View.GONE
                        }

                        // 남은 알람 끄기 횟수 - UI만 업데이트, 바텀시트 상태는 변경하지 않음
                        val remainCount = state.remainCount
                        remainCount?.let { count ->
                            updateDisableBottomSheetUI(count)
                        }

                        // 알람 도착 인증 결과
                        val isAlarmCheckedIn = state.isAlarmCheckedIn
                        if (isAlarmCheckedIn) {
                            val stopIntent = Intent("com.whiplash.akuma.STOP_ALARM").apply {
                                component = ComponentName(
                                    "com.whiplash.akuma",
                                    "com.whiplash.akuma.alarm.StopAlarmReceiver"
                                )
                                putExtra("alarmId", alarmId.toInt())
                            }
                            sendBroadcast(stopIntent)
                            Timber.d("## [알람 정지] ComponentName으로 브로드캐스트 전송. alarmId: $alarmId")

                            // 브로드캐스트 처리용 딜레이
                            lifecycleScope.launch {
                                delay(100)

                                bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                                binding.checkInBottomSheet.visibility = View.GONE
                                disableAlarmBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
                                binding.disableAlarmBottomSheet.visibility = View.GONE

                                navigateTo<AlarmCheckInSuccessActivity> {
                                    putExtra("address", address)
                                    finishCurrentActivity()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 바텀시트 UI 업데이트
    private fun updateDisableBottomSheetUI(count: Int) {
        with(binding) {
            tvRemainDisableAlarmCount.text = "${count}회"

            if (count > 0) {
                ivDisableAlarmBottomSheet.setImageDrawable(
                    ContextCompat.getDrawable(this@AlarmActivity, R.drawable.ic_notice_44)
                )
                tvDisableBottomSheetTitle.text = getString(R.string.check_in_alarm_disable_title)
                tvDisableBottomSheetSubTitle.visibility = View.VISIBLE
                tvDisableBottomSheetSubTitle.text = getString(R.string.check_in_alarm_not_disable_sub_title)
                btnCheckInPlace.visibility = View.GONE
                llDisableButtonsContainer.visibility = View.VISIBLE
                llRemainDisableAlarmCountContainer.visibility = View.VISIBLE
            } else {
                ivDisableAlarmBottomSheet.setImageDrawable(
                    ContextCompat.getDrawable(this@AlarmActivity, R.drawable.ic_warning_44)
                )
                tvDisableBottomSheetTitle.text = "알람 끄기 횟수를 모두 사용했어요"
                tvDisableBottomSheetSubTitle.visibility = View.GONE
                btnCheckInPlace.visibility = View.VISIBLE
                llDisableButtonsContainer.visibility = View.GONE
                llRemainDisableAlarmCountContainer.visibility = View.GONE
            }
        }
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
                showDisableBottomSheet()
            }

            btnCheckIn.setOnClickListener {
                // 장소 인증 api 호출. 호출 성공 시 알람 끄고 도착 인증 화면으로 이동
                mainViewModel.checkInAlarm(alarmId, latitude, longitude)
            }

            btnCancelDisable.setOnClickListener {
                // 비활성화 바텀시트 > 취소
                hideDisableBottomSheet()
            }

            btnAlarmDisable.setOnClickListener {
                Timber.d("## [비활성화] 1회 사용하기 클릭")
                // TODO: 1회 사용하기 API 호출 후 알람 끄기
                hideDisableBottomSheet()
                finish()
            }

            btnCheckInPlace.setOnClickListener {
                mainViewModel.checkInAlarm(alarmId, latitude, longitude)
            }
        }
    }

    private fun showDisableBottomSheet() {
        with(binding) {
            checkInBottomSheet.visibility = View.GONE
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN

            disableAlarmBottomSheet.visibility = View.VISIBLE
            disableAlarmBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun hideDisableBottomSheet() {
        with(binding) {
            disableAlarmBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
            disableAlarmBottomSheet.visibility = View.GONE
        }
    }

    private fun formatHour24(hour: Int): String = String.format("%02d", hour)

    private fun formatMinute(minute: Int): String = String.format("%02d", minute)

    private fun setupUserLocationSource() {
        locationSource = FusedLocationSource(this, 1000)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

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

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 안드 13 미만 버전 대응
        // 이 화면에선 뒤로가기 버튼을 눌러도 뒤로 이동하지 않음
        super.onBackPressed()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.getRemainingDisableCount()
    }

}