package com.whiplash.presentation.component.header

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashHeaderBinding

class WhiplashHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding =
        LayoutWhiplashHeaderBinding.inflate(LayoutInflater.from(context), this, true)

    private var onLeftIconClickListener: (() -> Unit)? = null
    private var onRightIconClickListener: (() -> Unit)? = null

    companion object {
        // attrs.xml의 leftIconType > back 의미
        const val LEFT_ICON_BACK = 0

        // attrs.xml의 rightIconType > close 의미
        const val RIGHT_ICON_CLOSE = 0
    }

    init {
        attrs?.let { attr ->
            context.withStyledAttributes(attr, R.styleable.WhiplashHeader) {
                // 헤더 제목
                val title = getString(R.styleable.WhiplashHeader_headerTitle)
                title?.let { binding.tvTitle.text = it }

                val showLeftIcon = getBoolean(R.styleable.WhiplashHeader_showLeftIcon, false)
                val showRightIcon = getBoolean(R.styleable.WhiplashHeader_showRightIcon, false)

                val leftIconType = getInt(R.styleable.WhiplashHeader_leftIconType, 0)
                val rightIconType = getInt(R.styleable.WhiplashHeader_rightIconType, 0)

                setupLeftIcon(showLeftIcon, leftIconType)
                setupRightIcon(showRightIcon, rightIconType)
            }
        }

        setupClickListeners()
    }

    private fun setupLeftIcon(show: Boolean, iconType: Int) {
        binding.ivLeft.visibility = if (show) View.VISIBLE else View.GONE

        when (iconType) {
            LEFT_ICON_BACK -> binding.ivLeft.setImageResource(R.drawable.ic_left_arrow_black_28)
        }
    }

    private fun setupRightIcon(show: Boolean, iconType: Int) {
        binding.ivRight.visibility = if (show) View.VISIBLE else View.GONE

        when (iconType) {
            RIGHT_ICON_CLOSE -> binding.ivRight.setImageResource(R.drawable.ic_dismiss_black_28)
        }
    }

    private fun setupClickListeners() {
        binding.ivLeft.setOnClickListener {
            onLeftIconClickListener?.invoke() ?: run {
                // 기본 동작 = 액티비티 종료
                (context as? Activity)?.finish()
            }
        }

        binding.ivRight.setOnClickListener {
            onRightIconClickListener?.invoke() ?: run {
                // 기본 동작 = 액티비티 종료
                (context as? Activity)?.finish()
            }
        }
    }

    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun showLeftIcon(show: Boolean, iconType: Int = 0) {
        setupLeftIcon(show, iconType)
    }

    fun showRightIcon(show: Boolean, iconType: Int = 0) {
        setupRightIcon(show, iconType)
    }

    fun setOnLeftIconClickListener(listener: () -> Unit) {
        onLeftIconClickListener = listener
    }

    fun setOnRightIconClickListener(listener: () -> Unit) {
        onRightIconClickListener = listener
    }
}