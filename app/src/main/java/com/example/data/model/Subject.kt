package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val weeklyClassCount: Int = 4,
    val targetPercentage: Double = 75.0,
    val colorHex: String = "#8FAF8B",
    val scheduledDaysOfWeek: String = "1,2,3,4,5", // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
