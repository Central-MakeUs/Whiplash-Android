package com.whiplash.presentation.component.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import com.whiplash.presentation.databinding.ViewUserInfoBinding

/**
 * [com.whiplash.presentation.user_info.UserInfoActivity]에 표시되는 뷰
 *
 * 현재 버전만 텍스트가 표시되고 그 외의 뷰들은 > 아이콘을 표시한다
 */
class UserInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: ViewUserInfoBinding =
        ViewUserInfoBinding.inflate(LayoutInflater.from(context), this, true)

    private var onItemClickListener: ((String) -> Unit)? = null
    private var currentText: String = ""

    fun setIcon(@DrawableRes iconRes: Int) = binding.ivUserView.setImageResource(iconRes)

    fun setText(text: String) {
        currentText = text
        binding.tvUserView.text = text
    }

    fun setAppVersion(version: String) {
        binding.tvAppVersion.text = version
    }

    fun showAppVersion(show: Boolean) {
        binding.tvAppVersion.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showRightArrow(show: Boolean) {
        binding.ivUserInfoRightArrow.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
        binding.root.setOnClickListener {
            listener.invoke(currentText)
        }
    }
}