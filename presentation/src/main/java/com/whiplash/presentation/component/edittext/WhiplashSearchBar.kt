package com.whiplash.presentation.component.edittext

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWhiplashSearchbarBinding

/**
 * 공통 searchBar
 */
class WhiplashSearchBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = LayoutWhiplashSearchbarBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        // 초기 배경 설정
        updateBackground(false)

        attrs?.let { attr ->
            context.withStyledAttributes(attr, R.styleable.WhiplashSearchBar) {
                val hint = getString(R.styleable.WhiplashSearchBar_android_hint)
                hint?.let { binding.etSearch.hint = it }

                val text = getString(R.styleable.WhiplashSearchBar_android_text)
                text?.let { binding.etSearch.setText(it) }
            }
        }

        setupTextWatcher()
        setupClearButton()
        setupFocusListener()
    }

    private fun setupTextWatcher() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateClearButtonVisibility()
            }
        })
    }

    private fun setupClearButton() {
        binding.ivClear.setOnClickListener {
            binding.etSearch.setText("")
            binding.etSearch.clearFocus()
        }
    }

    private fun setupFocusListener() {
        binding.etSearch.setOnFocusChangeListener { _, hasFocus ->
            updateBackground(hasFocus)
        }
    }

    private fun updateBackground(hasFocus: Boolean) {
        val backgroundRes = if (hasFocus) {
            R.drawable.bg_whiplash_searchbar_focused
        } else {
            R.drawable.bg_whiplash_searchbar_normal
        }
        background = ContextCompat.getDrawable(context, backgroundRes)
    }

    private fun updateClearButtonVisibility() {
        val hasText = binding.etSearch.text.isNotEmpty()
        binding.ivClear.visibility = if (hasText) View.VISIBLE else View.GONE
    }

    fun getText(): String = binding.etSearch.text.toString()

    fun setText(text: String) {
        binding.etSearch.setText(text)
    }

    fun setHint(hint: String) {
        binding.etSearch.hint = hint
    }

    fun clearText() {
        binding.etSearch.setText("")
    }

    fun setOnTextChangeListener(listener: (String) -> Unit) {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                listener(s.toString())
            }
        })
    }
}