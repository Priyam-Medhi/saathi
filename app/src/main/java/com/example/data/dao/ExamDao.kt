package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY isDateTbd ASC, dateTimeMillis ASC, id ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isDateTbd = 0 AND dateTimeMillis >= :fromTime ORDER BY dateTimeMillis ASC")
    fun getUpcomingScheduledExams(fromTime: Long): Flow<List<Exam>>

    @Query("SELECT * FROM exams WHERE isDateTbd = 1 ORDER BY id DESC")
    fun getTbdExams(): Flow<List<Exam>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExams(exams: List<Exam>): List<Long>

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)
}
