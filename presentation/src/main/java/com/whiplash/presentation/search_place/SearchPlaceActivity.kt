package com.whiplash.presentation.search_place

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.presentation.R
import com.whiplash.presentation.component.loading.WhiplashLoadingScreen
import com.whiplash.presentation.databinding.ActivitySearchPlaceBinding
import com.whiplash.presentation.map.SelectPlaceActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * [com.whiplash.presentation.create_alarm.CreateAlarmActivity]에서 "도착 목표 장소는?" 클릭 시 이동하는 장소 선택 화면
 */
@AndroidEntryPoint
class SearchPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPlaceBinding
    private lateinit var loadingScreen: WhiplashLoadingScreen

    private val searchPlaceViewModel: SearchPlaceViewModel by viewModels()
    private lateinit var searchPlaceAdapter: SearchPlaceAdapter

    private var searchJob: Job? = null

    private val selectPlaceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // SelectPlaceActivity에서 받은 결과를 CreateAlarmActivity로 전달
            setResult(RESULT_OK, result.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_place_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupView()
        observeSearchPlaceViewModel()
    }
    
    private fun observeSearchPlaceViewModel() {
        lifecycleScope.launch { 
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    searchPlaceViewModel.uiState.collect { state ->
                        if (state.isLoading) {
                            loadingScreen.show()
                        } else {
                            loadingScreen.hide()
                        }

                        // 장소 검색 결과
                        if (state.placeList.isNotEmpty()) {
                            searchPlaceAdapter.submitList(state.placeList)
                            binding.tvSearchResult.visibility = View.VISIBLE
                            binding.rvSearchPlace.visibility = View.VISIBLE
                        } else {
                            binding.tvSearchResult.visibility = View.GONE
                            binding.rvSearchPlace.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun setupView() {
        loadingScreen = WhiplashLoadingScreen(this)
        with(binding) {
            whSearchPlace.setTitle(getString(R.string.select_place_header))
            setupRecyclerView()
            setupSearchBar()
        }
    }

    private fun setupRecyclerView() {
        searchPlaceAdapter = SearchPlaceAdapter {
            val latitude = it.latitude
            val longitude = it.longitude
            val simpleAddress = it.name
            val detailAddress = it.address

            Timber.d("## [장소 선택] 위도 : $latitude, 경도 : $longitude")
            Timber.d("## [장소 선택] 간단한 주소 : $simpleAddress, 상세 주소 : $detailAddress")

            selectPlaceLauncher.launch(
                Intent(this, SelectPlaceActivity::class.java).apply {
                    putExtra("latitude", latitude)
                    putExtra("longitude", longitude)
                    putExtra("simpleAddress", simpleAddress)
                    putExtra("detailAddress", detailAddress)
                }
            )
        }

        binding.rvSearchPlace.apply {
            adapter = searchPlaceAdapter
            addItemDecoration(DividerItemDecoration(this@SearchPlaceActivity, DividerItemDecoration.VERTICAL))
        }
    }
    
    private fun setupSearchBar() {
        binding.wsbPlace.setOnTextChangeListener { query ->
            searchJob?.cancel()
            
            if (query.isNotBlank()) {
                searchJob = lifecycleScope.launch {
                    delay(300)
                    Timber.d("## [장소 선택] 검색어 : $query")
                    searchPlaceViewModel.searchPlace(query.trim())
                }
            } else {
                // 검색어가 비어있으면 리사이클러뷰 숨김
                binding.tvSearchResult.visibility = View.GONE
                binding.rvSearchPlace.visibility = View.GONE
            }
        }
    }

}