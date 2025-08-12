package com.whiplash.presentation.map

import android.Manifest
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import com.whiplash.presentation.component.loading.WhiplashLoadingScreen
import com.whiplash.presentation.databinding.ActivitySelectPlaceBinding
import com.whiplash.presentation.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.whiplash.domain.entity.auth.response.PlaceDetailEntity
import com.whiplash.presentation.search_place.SearchPlaceViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 장소 선택 화면
 */
@AndroidEntryPoint
class SelectPlaceActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        /**
         * @see onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private lateinit var binding: ActivitySelectPlaceBinding
    private lateinit var loadingScreen: WhiplashLoadingScreen

    private val searchPlaceViewModel: SearchPlaceViewModel by viewModels()

    private lateinit var naverMap: NaverMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // FusedLocationProviderClient와 지자기, 가속도 센서를 활용해 유저 위치를 리턴하는 구현체
    private lateinit var locationSource: FusedLocationSource

    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null

    // 마커를 표시할 위경도
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    // 바텀시트에 표시할 간단한 주소, 자세한 주소
    private var simpleAddress: String = ""
    private var detailAddress: String = ""

    // 화면 중앙에 고정된 마커와 원형 오버레이
    private var centerMarker: Marker? = null
    private var centerCircleOverlay: CircleOverlay? = null
    private var locationUpdateJob: Job? = null

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

        onBackPressedDispatcher.addCallback(this) {
            // 안드 13+ 대응
            // 이 화면에선 뒤로가기 버튼을 눌러도 뒤로 이동하지 않음
        }

        loadingScreen = WhiplashLoadingScreen(this)
        setupUserLocationSource()

        if (PermissionUtils.hasLocationPermission(this)) {
            setupNaverMap()
        } else {
            PermissionUtils.requestLocationPermissions(this)
        }

        observeSearchPlaceViewModel()

        getDataFromIntent()
        setupView()
        setupBottomSheet()
    }

    private fun observeSearchPlaceViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    searchPlaceViewModel.uiState.collect { _ ->
                        combine(
                            searchPlaceViewModel.uiState.map { it.isLoading },
                            searchPlaceViewModel.uiState.map { it.placeDetail }
                        ) { isLoading, placeDetail ->
                            Pair(isLoading, placeDetail)
                        }.collect { (isLoading, placeDetail) ->
                            when {
                                isLoading -> {
                                    Timber.d("## [장소 선택] 지도에서 위경도 확인. 서버로 보내서 주소 조회 중")
                                    with(binding) {
                                        llMapLoadingBottomSheet.visibility = View.VISIBLE
                                        clRegisterTargetPlaceContainer.visibility = View.GONE
                                    }
                                }

                                placeDetail != null -> {
                                    // 로딩 완료 + 장소 정보 있을 때
                                    val name = placeDetail.name
                                    val address = placeDetail.address
                                    detailAddress = placeDetail.address
                                    Timber.d("## [장소 선택] 서버로 위경도 넘겨 가져온 주소값 - name : $name, address : $address")

                                    with(binding) {
                                        llMapLoadingBottomSheet.visibility = View.GONE
                                        clRegisterTargetPlaceContainer.visibility = View.VISIBLE
                                        tvPlaceAddress.text = placeDetail.name
                                        tvPlaceDetailAddress.text = placeDetail.address
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private fun getDataFromIntent() {
        latitude = intent.getDoubleExtra("latitude", 0.0)
        longitude = intent.getDoubleExtra("longitude", 0.0)
        simpleAddress = intent.getStringExtra("simpleAddress") ?: ""
        detailAddress = intent.getStringExtra("detailAddress") ?: ""

        binding.tvPlaceAddress.text = simpleAddress.ifEmpty { "" }
        binding.tvPlaceDetailAddress. text = detailAddress. ifEmpty { "" }
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
            isNightModeEnabled = true
            mapType = NaverMap.MapType.Navi // Navi 유형만 다크모드를 지원해서 Navi 타입으로 설정해야 함
            locationSource = locationSource
            uiSettings.isLocationButtonEnabled = true
        }

        // 카메라 이동 리스너 설정
        setupCameraChangeListener()
        
        // 전달받은 위치가 있으면 해당 위치로, 없으면 내 위치로 이동
        if (latitude != 0.0 && longitude != 0.0) {
            val targetLocation = LatLng(latitude, longitude)
            naverMap.moveCamera(CameraUpdate.scrollTo(targetLocation))
            setupCenterMarkerAndCircle()
        } else {
            getCurrentLocationAndSetup()
        }
    }

    private fun setupCameraChangeListener() {
        naverMap.addOnCameraChangeListener { reason, animated ->
            locationUpdateJob?.cancel()

            // 카메라 이동 시마다 마커, 원형 오버레이 위치는 즉시 업데이트
            val cameraPosition = naverMap.cameraPosition
            updateCenterMarkerAndCircle(cameraPosition.target)

            locationUpdateJob = lifecycleScope.launch {
                delay(500)
                latitude = cameraPosition.target.latitude
                longitude = cameraPosition.target.longitude
                Timber.d("## [위치] 0.5초 후 위도 : $latitude, 경도 : $longitude")
                searchPlaceViewModel.getPlaceDetail(latitude, longitude)
            }
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

    private fun setupCenterMarkerAndCircle() {
        val centerPosition = naverMap.cameraPosition.target
        createCenterMarker(centerPosition)
        createCenterCircleOverlay(centerPosition)
    }

    private fun updateCenterMarkerAndCircle(position: LatLng) {
        centerMarker?.position = position
        centerCircleOverlay?.center = position
    }

    private fun createCenterMarker(position: LatLng) {
        // 기존 마커가 있으면 제거
        centerMarker?.map = null
        
        centerMarker = Marker().apply {
            this.position = position
            
            // LinearLayout에 텍스트뷰 + 이미지뷰를 세로로 배치
            val linearLayout = LinearLayout(this@SelectPlaceActivity).apply {
                orientation = LinearLayout.VERTICAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }

            // 텍스트뷰 동적 생성
            val textView = createMarkerTextView(getString(R.string.disable_alarm_here))
            linearLayout.addView(textView)

            // 마커 이미지뷰 생성
            val imageView = ImageView(this@SelectPlaceActivity).apply {
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

    private fun createCenterCircleOverlay(position: LatLng) {
        // 기존 원형 오버레이가 있으면 제거
        centerCircleOverlay?.map = null
        
        centerCircleOverlay = CircleOverlay().apply {
            center = position
            radius = 100.0
            color = resources.getColor(R.color.pink_10, null)
            outlineColor = resources.getColor(R.color.pink_400, null)
            outlineWidth = resources.getDimensionPixelSize(R.dimen.dp_1)
            map = naverMap
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.addressBottomSheet)
        bottomSheetBehavior?.apply {
            isDraggable = false
            isHideable = false
            state = BottomSheetBehavior.STATE_COLLAPSED
        }

        setupBottomSheetViews()
        setupBottomSheetListeners()
    }

    private fun setupBottomSheetViews() {
        with(binding) {
            tvPlaceAddress.text = simpleAddress.ifEmpty { "" }
            tvPlaceDetailAddress.text = detailAddress.ifEmpty { "" }
        }
    }

    private fun setupBottomSheetListeners() {
        with(binding) {
            btnCancelRegisterAddress.setOnClickListener {
                finish()
            }

            btnRegisterAddress.setOnClickListener {
                val intent = Intent().apply {
                    putExtra("detailAddress", detailAddress)
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                }
                setResult(RESULT_OK, intent)
                finish()
            }
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
    }

}