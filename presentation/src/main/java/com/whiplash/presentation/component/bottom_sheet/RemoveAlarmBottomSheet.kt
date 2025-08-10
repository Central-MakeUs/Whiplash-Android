package com.whiplash.presentation.component.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.FragmentRemoveAlarmBinding

class RemoveAlarmBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FragmentRemoveAlarmBinding? = null
    private val binding
            get() = _binding!!

    private var onRemoveReasonSelectedListener: ((String) -> Unit)? = null
    private var onDismissListener: (() -> Unit)? = null
    private var reason: String = ""

    companion object {
        fun newInstance(
            onRemoveReasonSelectedListener: ((String) -> Unit), // 삭제 사유 라디오 버튼 선택 + 삭제 버튼 클릭 시
            onDismissListener: () -> Unit                       // 취소를 눌러서 바텀 시트를 닫으면 해당 알람의 선택 상태 해제
        ): RemoveAlarmBottomSheet {
            return RemoveAlarmBottomSheet().apply {
                this.onRemoveReasonSelectedListener = onRemoveReasonSelectedListener
                this.onDismissListener = onDismissListener
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRemoveAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 바텀 시트 밖을 눌러도 안 닫히게 설정. 취소 버튼 클릭을 통해서만 바텀 시트 닫기 가능
        dialog?.setCanceledOnTouchOutside(false)
        isCancelable = false

        setupViews()
        setDefaultSelection()
    }

    private fun setupViews() {
        with(binding) {
            rgRemoveReason.setOnCheckedChangeListener { _, checkedId ->
                reason = when (checkedId) {
                    R.id.rbRemoveReason1 -> getString(R.string.remove_alarm_reason_1)
                    R.id.rbRemoveReason2 -> getString(R.string.remove_alarm_reason_2)
                    R.id.rbRemoveReason3 -> getString(R.string.remove_alarm_reason_3)
                    R.id.rbRemoveReason4 -> getString(R.string.remove_alarm_reason_4)
                    R.id.rbRemoveReason5 -> getString(R.string.remove_alarm_reason_5)
                    else -> ""
                }
            }

            btnCancelRemove.setOnClickListener {
                onDismissListener?.invoke()
                dismiss()
            }

            btnRemove.setOnClickListener {
                onRemoveReasonSelectedListener?.invoke(reason)
                dismiss()
            }
        }
    }

    private fun setDefaultSelection() {
        with(binding) {
            // 바텀 시트가 표시될 때 1번 이유가 선택된 상태로 표시
            rbRemoveReason1.isChecked = true
            reason = getString(R.string.remove_alarm_reason_1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}