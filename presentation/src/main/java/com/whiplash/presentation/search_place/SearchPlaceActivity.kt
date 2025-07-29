package com.whiplash.presentation.search_place

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import com.whiplash.domain.entity.auth.response.SearchPlaceEntity
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ActivitySearchPlaceBinding
import com.whiplash.presentation.map.SelectPlaceActivity
import com.whiplash.presentation.util.ActivityUtils.navigateTo
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * [com.whiplash.presentation.create_alarm.CreateAlarmActivity]에서 "도착 목표 장소는?" 클릭 시 이동하는 장소 선택 화면
 */
@AndroidEntryPoint
class SearchPlaceActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchPlaceBinding

    private lateinit var searchPlaceAdapter: SearchPlaceAdapter

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
        testRecyclerView()
    }

    private fun setupView() {
        with(binding) {
            whSearchPlace.setTitle(getString(R.string.select_place_header))
            setupRecyclerView()
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
            navigateTo<SelectPlaceActivity> {
                putExtra("latitude", latitude)
                putExtra("longitude", longitude)
                putExtra("simpleAddress", simpleAddress)
                putExtra("detailAddress", detailAddress)
            }
        }

        binding.rvSearchPlace.apply {
            adapter = searchPlaceAdapter
            addItemDecoration(DividerItemDecoration(this@SearchPlaceActivity, DividerItemDecoration.VERTICAL))
        }
    }

    // TODO : api 적용 전 사용하는 임시값
    private fun testRecyclerView() {
        val list = listOf(
            SearchPlaceEntity(
                name = "경기도청",
                address = "",
                latitude = 37.2889398,
                longitude = 127.053822
            ),
            SearchPlaceEntity(
                name = "경기도청 북부청사",
                address = "경기도 의정부시 신곡동 800 경기도청 북부청사",
                latitude = 37.7474404,
                longitude = 127.0717166
            ),
            SearchPlaceEntity(
                name = "캐리비안베이",
                address = "경기도 용인시 처인구 포곡읍 전대리 310",
                latitude = 37.2981411,
                longitude = 127.2005786
            ),
            SearchPlaceEntity(
                name = "한국민속촌",
                address = "경기도 용인시 기흥구 보라동 35 한국민속촌",
                latitude = 37.2594023,
                longitude = 127.1205573
            ),
            SearchPlaceEntity(
                name = "스타필드 수원",
                address = "경기도 수원시 장안구 정자동 111-14 스타필드 수원",
                latitude = 37.2873924,
                longitude = 126.9915726
            ),
        )

        searchPlaceAdapter.submitList(list)
        binding.tvSearchResult.visibility = View.VISIBLE
    }

}