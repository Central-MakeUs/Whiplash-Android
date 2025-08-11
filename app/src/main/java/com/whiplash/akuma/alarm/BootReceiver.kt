package com.whiplash.akuma.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // 기기 재부팅 시 저장된 알람들을 다시 스케줄링
                // 실제로는 데이터베이스에서 활성화된 알람 목록을 가져와서 재등록해야 함
                // 현재는 기본 구조만 제공
            }
        }
    }
}