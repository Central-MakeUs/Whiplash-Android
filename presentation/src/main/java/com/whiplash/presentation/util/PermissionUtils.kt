package com.whiplash.presentation.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

    private val LOCATION_PERMISSIONS = arrayOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * 유저가 현재 위치 권한을 허용한 상태인지 확인
     *
     * 안드 12+에서 대략적 위치 권한만 허용 시 false 리턴
     *
     * @param context 권한 상태를 확인하기 위해 필요한 컨텍스트
     * @return true : 모든 위치 권한 허용됨, false : 일부 or 모든 위치 권한 거절됨
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 위치 권한 요청
     *
     * @param activity 권한을 요청하려는 액티비티
     */
    fun requestLocationPermissions(activity: Activity) =
        ActivityCompat.requestPermissions(
            activity,
            LOCATION_PERMISSIONS,
            LOCATION_PERMISSION_REQUEST_CODE
        )

    /**
     * 유저에게 위치 권한 요청 이유를 설명해야 하는지 확인
     *
     * 권한을 거부한 적 있다면 true 리턴
     *
     * @param activity 권한 설명 필요 여부를 확인하려는 액티비티
     * @return true : 권한 요청 이유 설명 필요, false:  권한 요청 이유 설명 불필요
     */
    fun shouldShowLocationPermissionRationale(activity: Activity): Boolean =
        LOCATION_PERMISSIONS.any { permission ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        }

    /**
     * 위치 권한 요청 결과 처리
     *
     * 안드 12+에선 대략적 위치만 허용할 수 있기 때문에 일부 위치 권한만 허용한 경우도 처리 필요
     *
     * FIXME : 기획에 따라 수정 필요
     *
     * @param requestCode 권한 요청 코드
     * @param permissions 요청된 권한 배열
     * @param grantedResults 권한 승인 결과 배열
     * @param onAllPermissionsGranted 모든 위치 권한 허용 시 실행
     * @param onPartialPermissionsGranted 일부 위치 권한만 허용 시 실행 (대략적 위치만 허용)
     * @param onPermissionDenied 모든 위치 권한 거부 시 실행
     */
    fun handleLocationPermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantedResults: IntArray,
        onAllPermissionsGranted: () -> Unit,
        onPartialPermissionsGranted: () -> Unit = {},
        onPermissionDenied: () -> Unit
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) return

        if (grantedResults.isEmpty()) {
            onPermissionDenied()
            return
        }

        val fineLocationGranted = permissions.zip(grantedResults.toTypedArray())
            .any { (permission, result) ->
                permission == android.Manifest.permission.ACCESS_FINE_LOCATION &&
                        result == PackageManager.PERMISSION_GRANTED
            }

        val coarseLocationGranted = permissions.zip(grantedResults.toTypedArray())
            .any { (permission, result) ->
                permission == android.Manifest.permission.ACCESS_COARSE_LOCATION &&
                        result == PackageManager.PERMISSION_GRANTED
            }

        when {
            fineLocationGranted && coarseLocationGranted -> onAllPermissionsGranted()
            coarseLocationGranted -> onPartialPermissionsGranted()
            else -> onPermissionDenied()
        }
    }

}