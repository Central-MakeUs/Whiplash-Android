package com.whiplash.presentation.component.button

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashButtonBinding
import androidx.core.content.withStyledAttributes

/**
 * 공통 버튼
 */
class WhiplashCommonButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashButtonBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        attrs?.let { attr ->
            context.withStyledAttributes(attr, R.styleable.WhiplashCommonButton) {

                // 배경색 설정
                val backgroundColor =
                    getColor(R.styleable.WhiplashCommonButton_buttonBackgroundColor, 0)
                if (backgroundColor != 0) {
                    setBackgroundColor(backgroundColor)
                }

                // 테두리 설정
                val borderColor = getColor(R.styleable.WhiplashCommonButton_borderColor, 0)
                val borderWidth = getDimensionPixelSize(R.styleable.WhiplashCommonButton_borderWidth, 0)

                if (backgroundColor != 0) {
                    if (borderColor != 0 && borderWidth > 0) {
                        setBackgroundWithBorder(backgroundColor, borderColor, borderWidth)
                    } else {
                        setBackgroundColor(backgroundColor)
                    }
                } else if (borderColor != 0 && borderWidth > 0) {
                    setBackgroundWithBorder(0, borderColor, borderWidth)
                }

                // 텍스트 설정
                val text = getString(R.styleable.WhiplashCommonButton_buttonText)
                text?.let { setText(it) }

                // 글자색 설정
                val textColor = getColor(R.styleable.WhiplashCommonButton_buttonTextColor, 0)
                if (textColor != 0) {
                    setTextColor(textColor)
                }

            }
        }
    }

    fun setText(text: String) {
        binding.btnWhiplash.text = text
    }

    override fun setBackgroundColor(color: Int) {
        val drawable = GradientDrawable().apply {
            setColor(color)
            cornerRadius = 4f * context.resources.displayMetrics.density
        }

        binding.btnWhiplash.background = drawable
    }

    private fun setBackgroundWithBorder(backgroundColor: Int, borderColor: Int, borderWidth: Int) {
        val drawable = GradientDrawable().apply {
            if (backgroundColor != 0) {
                setColor(backgroundColor)
            }
            cornerRadius = 4f * context.resources.displayMetrics.density

            if (borderColor != 0 && borderWidth > 0) {
                setStroke(borderWidth, borderColor)
            }
        }

        binding.btnWhiplash.background = drawable
    }

    private fun setTextColor(color: Int) {
        binding.btnWhiplash.setTextColor(color)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.btnWhiplash.setOnClickListener(listener)
    }

}