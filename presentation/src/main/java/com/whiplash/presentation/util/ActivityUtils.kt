package com.whiplash.presentation.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable

/**
 * 액티비티 이동 관련 유틸 함수 관리
 */
object ActivityUtils {

    /**
     * 액티비티 이동 함수
     *
     * @param intentExtras putExtra()에 담을 데이터를 가진 map
     * @param intentFlags 인텐트 플래그. 여러 플래그 사용 시 or 연산으로 조합해야 함
     * @param hasFinishCurrentActivity 현재 액티비티에서 finish() 호출 여부
     */
    inline fun <reified T : Activity> Context.navigateTo(
        intentExtras: Map<String, Any>? = null,
        intentFlags: Int? = null,
        hasFinishCurrentActivity: Boolean = false
    ) {
        val intent = Intent(this, T::class.java).apply {
            intentFlags?.let { flags = it }
            intentExtras?.forEach { (key, value) ->
                when (value) {
                    is String -> putExtra(key, value)
                    is Int -> putExtra(key, value)
                    is Long -> putExtra(key, value)
                    is Float -> putExtra(key, value)
                    is Double -> putExtra(key, value)
                    is Boolean -> putExtra(key, value)
                    is ByteArray -> putExtra(key, value)
                    is Parcelable -> putExtra(key, value)
                    else -> throw IllegalArgumentException("지원하지 않는 extra type : ${value::class.java}")
                }
            }
        }

        startActivity(intent)

        if (hasFinishCurrentActivity && this is Activity) {
            finish()
        }
    }

    /**
     * DSL 스타일의 액티비티 이동 함수
     *
     * 사용 예시
     *
     * ```kotlin
     * context.navigateTo<MainActivity> {
     *     putExtra("id", "test")
     *     putExtra("name", "user")
     *     // 둘 이상의 인텐트 플래그 사용 시 or 연산 필수
     *     setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
     *     finishCurrentActivity()
     * }
     * ```
     *
     * @see NavigationBuilder
     */
    inline fun <reified T : Activity> Context.navigateTo(
        builder: NavigationBuilder<T>.() -> Unit
    ) = NavigationBuilder(this, T::class.java).apply(builder).navigate()

    /**
     * 액티비티 이동을 DSL 형태로 처리하는 빌더 클래스
     *
     * @param T 이동할 액티비티의 타입
     * @param context 현재 컨텍스트
     * @param destination 이동할 액티비티
     */
    class NavigationBuilder<T: Activity>(
        private val context: Context,
        private val destination: Class<T>,
    ) {
        private val intentExtras = mutableMapOf<String, Any>()
        private var intentFlags: Int? = null
        private var hasFinishCurrentActivity: Boolean = false

        fun putExtra(key: String, value: Any) {
            intentExtras[key] = value
        }

        /**
         * 기존 인텐트 플래그에 매개변수 플래그를 덮어씌움
         *
         * @param flags 해당 인텐트에 새로 설정할 플래그
         */
        fun setFlags(flags: Int) {
            intentFlags = flags
        }

        /**
         * 기존 인텐트 플래그 외의 새 인텐트 플래그 추가
         *
         * @param flags 해당 인텐트에 새로 추가할 플래그
         */
        fun addFlags(flags: Int) {
            intentFlags = (intentFlags ?: 0) or flags
        }

        fun finishCurrentActivity() {
            this.hasFinishCurrentActivity = true
        }

        fun navigate() {
            val intent = Intent(context, destination).apply {
                intentFlags?.let { flags = it }
                intentExtras.forEach { (key, value) ->
                    when (value) {
                        is String -> putExtra(key, value)
                        is Int -> putExtra(key, value)
                        is Long -> putExtra(key, value)
                        is Float -> putExtra(key, value)
                        is Double -> putExtra(key, value)
                        is Boolean -> putExtra(key, value)
                        is ByteArray -> putExtra(key, value)
                        is Parcelable -> putExtra(key, value)
                        else -> throw IllegalArgumentException("지원하지 않는 extra type : ${value::class.java}")
                    }
                }
            }

            context.startActivity(intent)

            if (hasFinishCurrentActivity && context is Activity) {
                context.finish()
            }
        }
    }
}