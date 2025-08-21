package com.whiplash.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.presentation.R
import com.whiplash.presentation.databinding.ItemHomeAlarmBinding
import com.whiplash.presentation.util.DateUtils

class AlarmListAdapter(
    private val onItemClick: (Int) -> Unit = {},
    private val onToggleClick: (GetAlarmEntity) -> Unit = {}
) : ListAdapter<GetAlarmEntity, AlarmListAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    private var isDeleteMode = false
    private var selectedPosition = -1

    fun setDeleteMode(deleteMode: Boolean) {
        isDeleteMode = deleteMode
        if (!deleteMode) {
            selectedPosition = -1
        }

        notifyDataSetChanged()
    }

    fun toggleSelection(position: Int) {
        selectedPosition = if (selectedPosition == position) -1 else position
        notifyDataSetChanged()
    }

    fun clearSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun getSelectedAlarm(): GetAlarmEntity? =
        if (selectedPosition in 0 ..< itemCount) {
            getItem(selectedPosition)
        } else {
            null
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemHomeAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding, onItemClick, onToggleClick)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position), isDeleteMode, selectedPosition == position)
    }

    class AlarmViewHolder(
        private val binding: ItemHomeAlarmBinding,
        private val onItemClick: (Int) -> Unit,
        private val onToggleClick: (GetAlarmEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onItemClick(adapterPosition)
            }
        }

        fun bind(alarm: GetAlarmEntity, isDeleteMode: Boolean, isSelected: Boolean) {
            with(binding) {
                // 알람명
                tvHomeAlarmTopText.text = alarm.alarmPurpose

                // 09:00, 15:00 형태로 오는 시간을 오전 / 오후로 나눠 표시
                val (amPm, time) = formatTime(alarm.time)
                tvAmPm.text = amPm
                tvAlarmTime.text = time

                // 알람 설정한 요일들을 한글로 변환
                tvRepeatDays.text = DateUtils.convertDaysToKorean(alarm.repeatsDays)

                // 장소명
                tvAddress.text = alarm.address

                // 토글 상태는 서버 응답대로 설정
                wtAlarm.setChecked(alarm.isToggleOn)

                // 토글 클릭 리스너 설정
                wtAlarm.setOnCheckedChangeListener { isChecked ->
                    if (alarm.isToggleOn && !isChecked) {
                        // 토글이 true에서 false로 변경될 때만 팝업 표시
                        onToggleClick(alarm)
                        // 팝업에서 취소하면 다시 true로 되돌리기 위해
                        wtAlarm.setChecked(true)
                    } else if (!alarm.isToggleOn && isChecked) {
                        // false 상태에서 클릭 시 상태 변경 막기
                        wtAlarm.setChecked(false)
                    }
                }

                // 체크박스 가시성, 상태 설정
                if (isDeleteMode) {
                    ivAlarmCheck.visibility = android.view.View.VISIBLE
                    ivAlarmCheck.setImageResource(
                        if (isSelected) {
                            R.drawable.ic_check_pressed_22
                        } else {
                            R.drawable.ic_check_default_22
                        }
                    )
                } else {
                    ivAlarmCheck.visibility = android.view.View.GONE
                }
            }
        }

        /**
         * "09:00" 형태의 시간을 오전 / 오후, 9:00 형태로 분리
         *
         * @param time 09:00, 15:43 형태의 시간
         * @return 오전 9시, 오후 3시 43
         */
        private fun formatTime(time: String): Pair<String, String> {
            val parts = time.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1]

            return if (hour < 12) {
                val displayHour = if (hour == 0) 12 else hour
                "오전" to "$displayHour:$minute"
            } else {
                val displayHour = if (hour == 12) 12 else hour - 12
                "오후" to "$displayHour:$minute"
            }
        }
    }

    private class AlarmDiffCallback : DiffUtil.ItemCallback<GetAlarmEntity>() {
        override fun areItemsTheSame(oldItem: GetAlarmEntity, newItem: GetAlarmEntity): Boolean {
            return oldItem.alarmId == newItem.alarmId
        }

        override fun areContentsTheSame(oldItem: GetAlarmEntity, newItem: GetAlarmEntity): Boolean {
            return oldItem == newItem
        }
    }
}