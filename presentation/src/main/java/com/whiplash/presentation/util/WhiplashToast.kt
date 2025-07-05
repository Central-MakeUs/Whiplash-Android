package com.whiplash.presentation.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashToastBinding

/**
 * 커스텀 토스트 메시지 표시 함수 관리
 */
object WhiplashToast {

    fun showSuccessToast(
        context: Context,
        message: String,
    ) = show(context, message, true)

    fun showErrorToast(
        context: Context,
        message: String,
    ) = show(context, message, false)

    private fun show(
        context: Context,
        message: String,
        isSuccess: Boolean,
    ) {
        val iconRes = if (isSuccess) R.drawable.ic_check_22 else R.drawable.ic_error_red_22

        val binding = LayoutWhiplashToastBinding.inflate(LayoutInflater.from(context))
        with(binding) {
            ivToastIcon.setImageResource(iconRes)
            tvToastText.text = message
        }

        Toast(context).apply {
            duration = Toast.LENGTH_SHORT
            view = binding.root
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 100)
            show()
        }
    }

}