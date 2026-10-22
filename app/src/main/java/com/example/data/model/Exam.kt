package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ExamType(val label: String) {
    MIDSEM("Midsem"),
    ENDSEM("Endsem"),
    MINOR("Minor Exam"),
    QUIZ("Quiz"),
    LAB_EXAM("Lab Practical"),
    OTHER("Other")
}

data class TopicItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "exams")
data class Exam(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String,
    val examType: ExamType = ExamType.MIDSEM,
    val isDateTbd: Boolean = false,
    val dateTimeMillis: Long? = null,
    val venue: String = "",
    val topics: List<TopicItem> = emptyList(),
    val notes: String = "",
    val reminderMinutes: Int = 1440 // 1 day before
)
