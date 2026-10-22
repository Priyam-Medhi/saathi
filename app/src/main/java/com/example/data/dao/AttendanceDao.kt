package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY dateMillis DESC, id DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId ORDER BY dateMillis DESC, id DESC")
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE dateMillis >= :startOfDay AND dateMillis < :endOfDay")
    fun getRecordsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId AND dateMillis >= :startOfDay AND dateMillis < :endOfDay LIMIT 1")
    suspend fun getRecordForSubjectAndDay(subjectId: Long, startOfDay: Long, endOfDay: Long): AttendanceRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecord>): List<Long>

    @Update
    suspend fun updateRecord(record: AttendanceRecord)

    @Delete
    suspend fun deleteRecord(record: AttendanceRecord)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId")
    suspend fun deleteAllForSubject(subjectId: Long)
}
