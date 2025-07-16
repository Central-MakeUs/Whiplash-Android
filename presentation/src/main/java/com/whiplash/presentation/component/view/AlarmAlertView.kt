package com.whiplash.presentation.component.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.whiplash.presentation.databinding.ViewHomeAlertBinding

/**
 * 홈 화면 상단, 알람 설정 화면 하단에 표시되는 UI
 */
class AlarmAlertView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewHomeAlertBinding.inflate(LayoutInflater.from(context), this, true)

    fun setAlertTexts(
        firstText: String,
        secondText: String,
    ) {
        with(binding) {
            tvFirstAlert.text = firstText
            tvSecondAlert.text = secondText
        }
    }

}