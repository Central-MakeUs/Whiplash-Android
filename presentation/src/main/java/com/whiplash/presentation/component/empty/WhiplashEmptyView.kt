package com.whiplash.presentation.component.empty

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashEmptyBinding

/**
 * 공통 empty view
 */
class WhiplashEmptyView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashEmptyBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        attrs?.let { attr ->
            context.withStyledAttributes(attr, R.styleable.WhiplashEmptyView) {
                val title = getString(R.styleable.WhiplashEmptyView_emptyTitle)
                title?.let { setTitle(it) }

                val message = getString(R.styleable.WhiplashEmptyView_emptyMessage)
                message?.let { setMessage(it) }
            }
        }
    }

    fun setTitle(title: String) {
        binding.tvEmptyTitle.text = title
    }

    fun setMessage(message: String) {
        binding.tvEmptyMessage.text = message
    }

}