package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.AttendanceStatus
import com.example.data.model.EventCategory
import com.example.data.model.EventRecurrence
import com.example.data.model.ExamType
import com.example.data.model.TopicItem

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus?): String = status?.name ?: AttendanceStatus.PRESENT.name

    @TypeConverter
    fun toAttendanceStatus(value: String?): AttendanceStatus =
        value?.let { runCatching { AttendanceStatus.valueOf(it) }.getOrDefault(AttendanceStatus.PRESENT) }
            ?: AttendanceStatus.PRESENT

    @TypeConverter
    fun fromEventCategory(cat: EventCategory?): String = cat?.name ?: EventCategory.OTHER.name

    @TypeConverter
    fun toEventCategory(value: String?): EventCategory =
        value?.let { runCatching { EventCategory.valueOf(it) }.getOrDefault(EventCategory.OTHER) }
            ?: EventCategory.OTHER

    @TypeConverter
    fun fromEventRecurrence(rec: EventRecurrence?): String = rec?.name ?: EventRecurrence.NONE.name

    @TypeConverter
    fun toEventRecurrence(value: String?): EventRecurrence =
        value?.let { runCatching { EventRecurrence.valueOf(it) }.getOrDefault(EventRecurrence.NONE) }
            ?: EventRecurrence.NONE

    @TypeConverter
    fun fromExamType(type: ExamType?): String = type?.name ?: ExamType.MIDSEM.name

    @TypeConverter
    fun toExamType(value: String?): ExamType =
        value?.let { runCatching { ExamType.valueOf(it) }.getOrDefault(ExamType.MIDSEM) }
            ?: ExamType.MIDSEM

    @TypeConverter
    fun fromTopicList(topics: List<TopicItem>?): String {
        if (topics.isNullOrEmpty()) return ""
        return topics.joinToString(";;") { "${it.id}:::${it.title.replace(":::", "")}:::${it.isCompleted}" }
    }

    @TypeConverter
    fun toTopicList(value: String?): List<TopicItem> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(";;").mapNotNull { itemStr ->
            val parts = itemStr.split(":::")
            if (parts.size >= 3) {
                TopicItem(
                    id = parts[0],
                    title = parts[1],
                    isCompleted = parts[2].toBooleanStrictOrNull() ?: false
                )
            } else null
        }
    }
}
