package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EventCategory(val label: String, val colorHex: String) {
    HACKATHON("Hackathon", "#7EA7D8"),
    SPORTS("Sports", "#8FAF8B"),
    CLUB_MEET("Club Meet", "#C9BFE0"),
    ACADEMIC("Academic", "#E0B872"),
    CULTURAL("Cultural", "#E5A190"),
    OTHER("Other", "#9DBBB5")
}

enum class EventRecurrence(val label: String) {
    NONE("Does not repeat"),
    DAILY("Every day"),
    WEEKLY("Every week"),
    MONTHLY("Every month")
}

@Entity(tableName = "academic_events")
data class AcademicEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: EventCategory = EventCategory.OTHER,
    val startMillis: Long,
    val endMillis: Long,
    val location: String = "",
    val description: String = "",
    val recurrence: EventRecurrence = EventRecurrence.NONE,
    val reminderMinutes: Int = 60 // e.g. 60 min, 1440 min (1 day)
)
