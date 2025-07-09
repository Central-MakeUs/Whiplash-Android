package com.whiplash.presentation.component.toggle

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.whiplash.presentation.databinding.LayoutWhiplashToggleBinding
import timber.log.Timber

/**
 * 커스텀 토글 스위치
 */
class WhiplashToggle @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashToggleBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        with(binding) {
            toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    Timber.d("## [공통 토글] 선택됨")
                } else {
                    Timber.d("## [공통 토글] 선택해제됨")
                }
            }
        }
    }

    fun setChecked(checked: Boolean) {
        binding.toggleSwitch.isChecked = checked
    }

    fun isChecked(): Boolean {
        return binding.toggleSwitch.isChecked
    }

    fun setOnCheckedChangeListener(listener: (Boolean) -> Unit) {
        binding.toggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            listener(isChecked)
        }
    }
}