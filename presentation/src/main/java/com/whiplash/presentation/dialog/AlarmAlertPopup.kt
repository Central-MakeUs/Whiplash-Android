package com.whiplash.presentation.dialog

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.whiplash.presentation.databinding.LayoutHomeAlertPopupBinding

class AlarmAlertPopup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutHomeAlertPopupBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        with(binding) {
            ivCloseHomeAlertPopup.setOnClickListener {
                visibility = View.GONE
            }
        }
    }

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