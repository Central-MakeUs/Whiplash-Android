package com.whiplash.presentation.component.bottom_sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.FragmentAlarmSoundBottomSheetBinding

class AlarmSoundBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FragmentAlarmSoundBottomSheetBinding? = null
    private val binding
        get() = _binding!!

    private var onAlarmSoundSelectedListener: ((String, Int) -> Unit)? = null
    private var selectedRadioButtonId: Int = -1

    companion object {
        private const val KEY_SELECTED_ID = "selected_radio_button_id"

        fun newInstance(
            onAlarmSoundSelected: (String, Int) -> Unit,
            selectedRadioButtonId: Int = -1
        ): AlarmSoundBottomSheet {
            return AlarmSoundBottomSheet().apply {
                this.onAlarmSoundSelectedListener = onAlarmSoundSelected
                this.selectedRadioButtonId = selectedRadioButtonId
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlarmSoundBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBottomSheetBehavior()
        restoreSelectedState()
        setupViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_ID, binding.rgAlarmSound.checkedRadioButtonId)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            selectedRadioButtonId = it.getInt(KEY_SELECTED_ID, -1)
            restoreSelectedState()
        }
    }

    private fun restoreSelectedState() {
        if (selectedRadioButtonId != -1) {
            binding.rgAlarmSound.check(selectedRadioButtonId)
        } else {
            // 최초 진입 시에는 "알람 소리1"이 선택된 상태
            binding.rgAlarmSound.check(R.id.rbOption2)
        }
    }

    private fun setupBottomSheetBehavior() {
        dialog?.setOnShowListener {
            val bottomSheetDialog = it as com.google.android.material.bottomsheet.BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(sheet)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
        }
    }

    private fun setupViews() {
        with(binding) {
            btnPreListening.setOnClickListener {
                val checkedId = rgAlarmSound.checkedRadioButtonId
                val selectedText = when (checkedId) {
                    R.id.rbNothing -> getString(R.string.not_sound)
                    R.id.rbOption2 -> getString(R.string.sound_1)
                    R.id.rbOption3 -> getString(R.string.sound_2)
                    R.id.rbOption4 -> getString(R.string.sound_3)
                    R.id.rbOption5 -> getString(R.string.sound_4)
                    else -> getString(R.string.sound_1) // 기본값
                }

                selectedRadioButtonId = checkedId
                onAlarmSoundSelectedListener?.invoke(selectedText, checkedId)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}