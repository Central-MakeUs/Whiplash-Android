package com.whiplash.presentation.component.radiobutton

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatRadioButton
import com.whiplash.presentation.R

/**
 * 공통 라디오 버튼
 */
class WhiplashRadioButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatRadioButton(context, attrs, defStyleAttr) {

    init {
        setButtonDrawable(R.drawable.whiplash_radio_button_selector)
        isClickable = true
        isFocusable = true
    }
}