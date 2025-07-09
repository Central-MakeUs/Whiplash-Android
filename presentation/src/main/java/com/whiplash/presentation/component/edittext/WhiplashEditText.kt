package com.whiplash.presentation.component.edittext

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashEdittextBinding

/**
 * 공통 editText
 *
 * searchBar의 editText와는 다름
 */
class WhiplashEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashEdittextBinding.inflate(LayoutInflater.from(context), this, true)

    // 입력값 마스킹 처리 여부
    private var isSecureField = false
    // 입력값 표시 여부
    private var isContentVisible = false
    // editText 테두리를 빨갛게 바꿔야 하는지 여부
    private var hasError = false

    init {
        updateErrorState()
        attrs?.let { attr ->
            context.withStyledAttributes(attr, R.styleable.WhiplashEditText) {
                // hint 설정
                val hint = getString(R.styleable.WhiplashEditText_android_hint)
                hint?.let { binding.etInput.hint = it }

                // 텍스트 설정
                val text = getString(R.styleable.WhiplashEditText_android_text)
                text?.let { binding.etInput.setText(it) }

                isSecureField = getBoolean(R.styleable.WhiplashEditText_isPassword, false)
                hasError = getBoolean(R.styleable.WhiplashEditText_hasError, false)

                setupSecureField()
                updateErrorState()
            }
        }

        binding.ivToggle.setOnClickListener {
            toggleContentVisibility()
        }
    }

    private fun setupSecureField() {
        if (isSecureField) {
            binding.ivToggle.visibility = VISIBLE
            binding.etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            updateToggleIcon()
        } else {
            binding.ivToggle.visibility = GONE
            binding.etInput.inputType = InputType.TYPE_CLASS_TEXT
        }
    }

    private fun toggleContentVisibility() {
        isContentVisible = !isContentVisible

        if (isContentVisible) {
            binding.etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.etInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.etInput.setSelection(binding.etInput.text.length)
        updateToggleIcon()
    }

    private fun updateToggleIcon() {
        val iconRes = if (isContentVisible) {
            R.drawable.ic_hide_black_22
        } else {
            R.drawable.ic_show_black_22
        }
        binding.ivToggle.setImageResource(iconRes)
    }

    private fun updateErrorState() {
        val backgroundRes = if (hasError) {
            R.drawable.bg_whiplash_edittext_error
        } else {
            R.drawable.bg_whiplash_edittext
        }
        this.background = ContextCompat.getDrawable(context, backgroundRes)
    }

    fun setError(hasError: Boolean) {
        this.hasError = hasError
        updateErrorState()
    }

    fun getText(): String = binding.etInput.text.toString()

    fun setText(text: String) {
        binding.etInput.setText(text)
    }

    fun setHint(hint: String) {
        binding.etInput.hint = hint
    }
}