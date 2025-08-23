package com.whiplash.presentation.component.bottom_sheet

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.FragmentAlarmSoundBottomSheetBinding
import com.whiplash.presentation.util.WhiplashToast

class AlarmSoundBottomSheet: BottomSheetDialogFragment() {

    private var _binding: FragmentAlarmSoundBottomSheetBinding? = null
    private val binding
        get() = _binding!!

    private var onAlarmSoundSelectedListener: ((String, Int, String) -> Unit)? = null
    private var selectedRadioButtonId: Int = -1

    private var soundPool: SoundPool? = null
    private var soundIds: IntArray = IntArray(4)
    private var currentSoundId: Int = 0
    
    // 재생 상태를 추적하는 변수 추가
    private var isPlaying: Boolean = false
    private var playbackHandler: android.os.Handler? = null
    private var playbackRunnable: Runnable? = null

    companion object {
        private const val KEY_SELECTED_ID = "selected_radio_button_id"

        fun newInstance(
            onAlarmSoundSelected: (String, Int, String) -> Unit,
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

        setupSoundPool()
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

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool?.let { pool ->
            soundIds[0] = pool.load(requireContext(), R.raw.sound1, 1)
            soundIds[1] = pool.load(requireContext(), R.raw.sound2, 1)
            soundIds[2] = pool.load(requireContext(), R.raw.sound3, 1)
            soundIds[3] = pool.load(requireContext(), R.raw.sound4, 1)
        } ?: run {
            WhiplashToast.showErrorToast(requireActivity(), "알람 소리 초기화 오류가 발생했습니다. 잠시 후 다시 시도해 주세요")
        }
    }

    private fun playSelectedAlarmSound() {
        soundPool?.let { pool ->
            val checkedId = binding.rgAlarmSound.checkedRadioButtonId
            
            // "소리없음"이 선택된 경우 재생하지 않음
            if (checkedId == R.id.rbNothing) {
                WhiplashToast.showErrorToast(requireActivity(), "소리없음이 선택되어 있습니다.")
                return
            }

            if (isPlaying) {
                // 현재 재생 중이면 정지
                if (currentSoundId != 0) {
                    pool.stop(currentSoundId)
                }
                stopPlayback()
                return
            }

            // 재생 시작
            val soundIndex = when (checkedId) {
                R.id.rbOption1 -> 0
                R.id.rbOption2 -> 1
                R.id.rbOption3 -> 2
                R.id.rbOption4 -> 3
                else -> 0
            }

            currentSoundId = pool.play(
                soundIds[soundIndex],
                1.0f,
                1.0f,
                1,
                0,
                1.0f
            )

            if (currentSoundId != 0) {
                startPlayback()
                // 3초 후에 자동으로 재생 완료 처리 (알람 소리 길이에 맞게 조정 필요)
                schedulePlaybackEnd(3000)
            }
        } ?: run {
            WhiplashToast.showErrorToast(requireActivity(), "알람 소리 재생 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요")
        }
    }

    private fun startPlayback() {
        isPlaying = true
        updatePlayPauseIcon()
    }

    private fun stopPlayback() {
        isPlaying = false
        currentSoundId = 0
        cancelScheduledPlaybackEnd()
        updatePlayPauseIcon()
    }

    private fun updatePlayPauseIcon() {
        val iconRes = if (isPlaying) {
            R.drawable.ic_pause_22
        } else {
            R.drawable.ic_play_22
        }
        binding.ivAlarmPreListening.setImageResource(iconRes)
    }

    private fun schedulePlaybackEnd(delayMillis: Long) {
        cancelScheduledPlaybackEnd()
        
        playbackHandler = android.os.Handler(android.os.Looper.getMainLooper())
        playbackRunnable = Runnable {
            if (isPlaying) {
                stopPlayback()
            }
        }
        playbackHandler?.postDelayed(playbackRunnable!!, delayMillis)
    }

    private fun cancelScheduledPlaybackEnd() {
        playbackRunnable?.let { runnable ->
            playbackHandler?.removeCallbacks(runnable)
        }
        playbackRunnable = null
        playbackHandler = null
    }

    private fun restoreSelectedState() {
        if (selectedRadioButtonId != -1) {
            binding.rgAlarmSound.check(selectedRadioButtonId)
        } else {
            // 최초 진입 시에는 "알람 소리1"이 선택된 상태
            binding.rgAlarmSound.check(R.id.rbOption1)
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
                
                // UI에 표시할 텍스트
                val displayText = when (checkedId) {
                    R.id.rbNothing -> getString(R.string.not_sound)
                    R.id.rbOption1 -> getString(R.string.sound_1)
                    R.id.rbOption2 -> getString(R.string.sound_2)
                    R.id.rbOption3 -> getString(R.string.sound_3)
                    R.id.rbOption4 -> getString(R.string.sound_4)
                    else -> getString(R.string.sound_1)
                }
                
                // 알람 등록 api로 넘길 값
                val apiText = when (checkedId) {
                    R.id.rbNothing -> "소리없음"
                    R.id.rbOption1 -> "알람 소리1"
                    R.id.rbOption2 -> "알람 소리2"
                    R.id.rbOption3 -> "알람 소리3"
                    R.id.rbOption4 -> "알람 소리4"
                    else -> "알람 소리1"
                }

                selectedRadioButtonId = checkedId
                onAlarmSoundSelectedListener?.invoke(displayText, checkedId, apiText)
                dismiss()
            }

            ivAlarmPreListening.setOnClickListener {
                playSelectedAlarmSound()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelScheduledPlaybackEnd()
        soundPool?.release()
        soundPool = null
        _binding = null
    }
}