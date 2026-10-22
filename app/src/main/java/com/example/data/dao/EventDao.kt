package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AcademicEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM academic_events ORDER BY startMillis ASC")
    fun getAllEvents(): Flow<List<AcademicEvent>>

    @Query("SELECT * FROM academic_events WHERE endMillis >= :fromTime ORDER BY startMillis ASC")
    fun getUpcomingEvents(fromTime: Long): Flow<List<AcademicEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AcademicEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<AcademicEvent>): List<Long>

    @Update
    suspend fun updateEvent(event: AcademicEvent)

    @Delete
    suspend fun deleteEvent(event: AcademicEvent)

    @Query("DELETE FROM academic_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
