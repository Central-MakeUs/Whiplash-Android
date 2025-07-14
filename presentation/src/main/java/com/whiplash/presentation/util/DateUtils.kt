package com.whiplash.presentation.util

object DateUtils {
    private fun convertDayToKorean(day: String): String {
        return when (day) {
            "MONDAY" -> "월"
            "TUESDAY" -> "화"
            "WEDNESDAY" -> "수"
            "THURSDAY" -> "목"
            "FRIDAY" -> "금"
            "SATURDAY" -> "토"
            "SUNDAY" -> "일"
            else -> day
        }
    }

    fun convertDaysToKorean(days: List<String>): String =
        days.joinToString(", ") { convertDayToKorean(it) }
}