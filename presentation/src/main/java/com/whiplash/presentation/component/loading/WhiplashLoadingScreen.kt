package com.whiplash.presentation.component.loading

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.whiplash.presentation.databinding.LayoutWhiplashLoadingBinding

/**
 * api 호출 중 같은 로딩 상황에 표시할 로딩 화면
 */
class WhiplashLoadingScreen(private val context: Context) {

    private var binding: LayoutWhiplashLoadingBinding? = null
    private var rootView: ViewGroup? = null

    fun show() {
        if (context is Activity) {
            rootView = context.findViewById(android.R.id.content)
            binding = LayoutWhiplashLoadingBinding.inflate(LayoutInflater.from(context))
            rootView?.addView(binding?.root)
        }
    }

    fun hide() {
        binding?.root?.let { rootView?.removeView(it) }
        binding = null
    }
}