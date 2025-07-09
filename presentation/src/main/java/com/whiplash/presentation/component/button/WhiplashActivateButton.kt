package com.whiplash.presentation.component.button

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.whiplash.presentation.databinding.LayoutWhiplashActivateButtonBinding

/**
 * 요일 선택 시 활성화, 비활성화 처리되어 보이는 버튼
 */
class WhiplashActivateButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashActivateButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        with(binding) {
            tvActivateButton.setOnClickListener {
                isSelected = !isSelected
            }
        }
    }

    fun setText(text: String) {
        binding.tvActivateButton.text = text
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        binding.tvActivateButton.isSelected = selected
    }

}