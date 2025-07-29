package com.whiplash.network.api

import com.whiplash.network.dto.response.ResponseGetPlaceDetail
import com.whiplash.network.dto.response.ResponseSearchPlace
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {

    /**
     * 장소 목록 검색
     *
     * ```kotlin
     * {
     *   "isSuccess": true,
     *   "code": "SUCCESS",
     *   "message": "성공",
     *   "result": [
     *     {
     *       "name": "경기도청",
     *       "address": "",
     *       "latitude": 37.2889398,
     *       "longitude": 127.053822
     *     },
     *     {
     *       "name": "경기도청 북부청사",
     *       "address": "경기도 의정부시 신곡동 800 경기도청 북부청사",
     *       "latitude": 37.7474404,
     *       "longitude": 127.0717166
     *     },
     *     {
     *       "name": "캐리비안베이",
     *       "address": "경기도 용인시 처인구 포곡읍 전대리 310",
     *       "latitude": 37.2981411,
     *       "longitude": 127.2005786
     *     },
     *     {
     *       "name": "한국민속촌",
     *       "address": "경기도 용인시 기흥구 보라동 35 한국민속촌",
     *       "latitude": 37.2594023,
     *       "longitude": 127.1205573
     *     },
     *     {
     *       "name": "스타필드 수원",
     *       "address": "경기도 수원시 장안구 정자동 111-14 스타필드 수원",
     *       "latitude": 37.2873924,
     *       "longitude": 126.9915726
     *     }
     *   ]
     * }
     * ```
     */
    @GET("places/search")
    suspend fun searchPlace(
        @Query("query") query: String,
    ): Response<ResponseSearchPlace>

    /**
     * 장소 상세 조회
     *
     * ```kotlin
     * {
     *   "isSuccess": true,
     *   "code": "SUCCESS",
     *   "message": "성공",
     *   "result": {
     *     "address": "경기도 수원시 영통구 하동 광교호수로 139",
     *     "name": "광교호수공원시설"
     *   }
     * }
     * ```
     */
    @GET("places/detail")
    suspend fun getPlaceDetail(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
    ): Response<ResponseGetPlaceDetail>

}