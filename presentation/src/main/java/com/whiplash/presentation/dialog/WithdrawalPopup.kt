package com.whiplash.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.LayoutWithdrawalPopupBinding
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WithdrawalPopup @Inject constructor(
    @ApplicationContext private val context: Context
): DefaultLifecycleObserver {

    private val dialog = Dialog(context).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_withdrawal_popup)
        setCanceledOnTouchOutside(false)
    }

    init {
        // android.view.WindowLeaked 에러가 발생하지 않게 액티비티 컨텍스트인 경우 생명주기 옵저버로 등록
        if (context is LifecycleOwner) {
            context.lifecycle.addObserver(this)
        }
    }

    fun show(withdrawalClickListener: () -> Unit) {
        val binding = LayoutWithdrawalPopupBinding.inflate(LayoutInflater.from(context))

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
                btnCancelWithdrawal.setOnClickListener {
                    dismiss()
                }

                btnWithdrawal.setOnClickListener {
                    withdrawalClickListener()
                    dismiss()
                }
            }
        }

        dialog.show()
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