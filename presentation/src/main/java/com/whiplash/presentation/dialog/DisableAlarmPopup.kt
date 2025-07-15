package com.whiplash.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutDisableAlarmPopupBinding
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

/**
 * 홈 화면에서 알람 끄기 시도 시 표시되는 팝업
 */
class DisableAlarmPopup @Inject constructor(
    @ActivityContext private val context: Context
): DefaultLifecycleObserver {

    private val dialog = Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_disable_alarm_popup)
        setCanceledOnTouchOutside(false)
    }

    init {
        // android.view.WindowLeaked 에러가 발생하지 않게 액티비티 컨텍스트인 경우 생명주기 옵저버로 등록
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
    }

    /**
     * 남은 알람 끄기 가능 횟수가 0보다 크면 알람 끄기, 0이라면 확인이 표시되는 팝업
     *
     * @param title 경고 밑 텍스트
     * @param subContent count가 0인 경우 표시되는 텍스트. 0보다 큰 숫자면 표시되지 않음
     * @param count 남은 알람 끄기 횟수
     * @param disableAlarmClickListener 오른쪽 버튼 텍스트가 "알람 끄기"인 경우 호출
     * @param okClickListener 오른쪽 버튼 텍스트가 "확인"인 경우 호출
     * @param cancelText 왼쪽 버튼 텍스트
     * @param cancelClickListener 왼쪽 버튼 클릭 리스너
     */
    fun show(
        title: String,
        subContent: String? = null,
        count: Int,
        disableAlarmClickListener: () -> Unit,
        okClickListener: () -> Unit,
        cancelText: String,
        cancelClickListener: () -> Unit,
    ) {
        val binding = LayoutDisableAlarmPopupBinding.inflate(LayoutInflater.from(context))

        dialog.apply {
            setContentView(binding.root)
            window?.let {
                it.decorView.setBackgroundResource(android.R.color.transparent)
                it.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
            }

            with(binding) {
                tvDisableAlarmContent.text = title
                tvRemainDisableAlarmCount.text = createLemonColorCount(count)
                btnCancelDisable.setText(cancelText)

                // 알람 끄기 횟수를 모두 사용
                if (count == 0) {
                    tvDisableAlarmSubContent.apply {
                        visibility = View.VISIBLE
                        text = subContent
                    }

                    btnAlarmDisable.apply {
                        setText(ContextCompat.getString(context, R.string.ok_string))
                        setOnClickListener {
                            okClickListener()
                            dismiss()
                        }
                    }
                } else {
                    tvDisableAlarmSubContent.visibility = View.GONE
                    btnAlarmDisable.apply {
                        setText(ContextCompat.getString(context, R.string.disable_alarm))
                        setOnClickListener {
                            disableAlarmClickListener()
                            dismiss()
                        }
                    }
                }

                ivDisableAlarmDismiss.setOnClickListener {
                    dismiss()
                }

                btnCancelDisable.setOnClickListener {
                    cancelClickListener()
                    dismiss()
                }
            }
        }

        dialog.show()
    }

    private fun createLemonColorCount(count: Int): SpannableString {
        val text = "${count}회"
        val spannableString = SpannableString(text)

        // 숫자에만 lemon_400 색깔 적용
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.lemon_400)),
            0,
            count.toString().length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.grey_300)),
            count.toString().length,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return spannableString
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (dialog.isShowing) {
            dialog.dismiss()
        }

        // 생명주기 옵저버가 알아서 처리하지만 명시적으로 처리하게 해서 예외상황 방지
        owner.lifecycle.removeObserver(this)
    }

}