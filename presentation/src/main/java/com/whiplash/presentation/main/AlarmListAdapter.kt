package com.whiplash.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.whiplash.domain.entity.alarm.response.GetAlarmEntity
import com.whiplash.presentation.databinding.ItemHomeAlarmBinding
import com.whiplash.presentation.util.DateUtils

class AlarmListAdapter : ListAdapter<GetAlarmEntity, AlarmListAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = ItemHomeAlarmBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AlarmViewHolder(private val binding: ItemHomeAlarmBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: GetAlarmEntity) {
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

                wtAlarm.setChecked(true)
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