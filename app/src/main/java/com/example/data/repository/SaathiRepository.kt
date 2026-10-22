package com.example.data.repository

import com.example.data.dao.AttendanceDao
import com.example.data.dao.EventDao
import com.example.data.dao.ExamDao
import com.example.data.dao.SubjectDao
import com.example.data.model.AcademicEvent
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Exam
import com.example.data.model.Subject
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class SaathiRepository(
    private val subjectDao: SubjectDao,
    private val attendanceDao: AttendanceDao,
    private val eventDao: EventDao,
    private val examDao: ExamDao
) {
    // Subjects
    val activeSubjects: Flow<List<Subject>> = subjectDao.getActiveSubjects()
    val allSubjects: Flow<List<Subject>> = subjectDao.getAllSubjects()

    suspend fun getSubjectById(id: Long): Subject? = subjectDao.getSubjectById(id)
    suspend fun insertSubject(subject: Subject): Long = subjectDao.insertSubject(subject)
    suspend fun updateSubject(subject: Subject) = subjectDao.updateSubject(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.deleteSubject(subject)
    suspend fun setArchived(subjectId: Long, isArchived: Boolean) = subjectDao.setArchived(subjectId, isArchived)

    // Attendance
    val allAttendanceRecords: Flow<List<AttendanceRecord>> = attendanceDao.getAllRecords()
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForSubject(subjectId)

    suspend fun logAttendance(
        subjectId: Long,
        status: AttendanceStatus,
        dateMillis: Long = System.currentTimeMillis(),
        notes: String = ""
    ): Long {
        val record = AttendanceRecord(
            subjectId = subjectId,
            dateMillis = dateMillis,
            status = status,
            notes = notes
        )
        return attendanceDao.insertRecord(record)
    }

    suspend fun setPastAttendance(
        subjectId: Long,
        attendedCount: Int,
        missedCount: Int,
        cancelledCount: Int = 0,
        replaceExisting: Boolean = true
    ) {
        if (replaceExisting) {
            attendanceDao.deleteAllForSubject(subjectId)
        }
        val records = mutableListOf<AttendanceRecord>()
        val now = System.currentTimeMillis()
        val total = attendedCount + missedCount + cancelledCount
        if (total <= 0) return

        var step = 0
        // Cancelled records
        for (i in 0 until cancelledCount) {
            step++
            val time = now - (step * 86400000L * 1.5).toLong()
            records.add(
                AttendanceRecord(
                    subjectId = subjectId,
                    dateMillis = time,
                    status = AttendanceStatus.CANCELLED,
                    notes = "Past semester log"
                )
            )
        }
        // Absent records
        for (i in 0 until missedCount) {
            step++
            val time = now - (step * 86400000L * 1.5).toLong()
            records.add(
                AttendanceRecord(
                    subjectId = subjectId,
                    dateMillis = time,
                    status = AttendanceStatus.ABSENT,
                    notes = "Past semester log"
                )
            )
        }
        // Present records
        for (i in 0 until attendedCount) {
            step++
            val time = now - (step * 86400000L * 1.5).toLong()
            records.add(
                AttendanceRecord(
                    subjectId = subjectId,
                    dateMillis = time,
                    status = AttendanceStatus.PRESENT,
                    notes = "Past semester log"
                )
            )
        }

        attendanceDao.insertRecords(records)
    }

    suspend fun markTodayAttendance(subjectId: Long, status: AttendanceStatus) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis

        cal.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = cal.timeInMillis

        val existing = attendanceDao.getRecordForSubjectAndDay(subjectId, startOfDay, endOfDay)
        if (existing != null) {
            // Update today's record
            attendanceDao.updateRecord(existing.copy(status = status, dateMillis = System.currentTimeMillis()))
        } else {
            // Insert new record
            attendanceDao.insertRecord(
                AttendanceRecord(
                    subjectId = subjectId,
                    dateMillis = System.currentTimeMillis(),
                    status = status
                )
            )
        }
    }

    suspend fun updateRecord(record: AttendanceRecord) = attendanceDao.updateRecord(record)
    suspend fun deleteRecord(record: AttendanceRecord) = attendanceDao.deleteRecord(record)
    suspend fun deleteRecordById(id: Long) = attendanceDao.deleteRecordById(id)

    // Academic Events
    val allEvents: Flow<List<AcademicEvent>> = eventDao.getAllEvents()
    fun getUpcomingEvents(fromTime: Long = System.currentTimeMillis()): Flow<List<AcademicEvent>> =
        eventDao.getUpcomingEvents(fromTime)

    suspend fun insertEvent(event: AcademicEvent): Long = eventDao.insertEvent(event)
    suspend fun updateEvent(event: AcademicEvent) = eventDao.updateEvent(event)
    suspend fun deleteEvent(event: AcademicEvent) = eventDao.deleteEvent(event)

    // Exams
    val allExams: Flow<List<Exam>> = examDao.getAllExams()
    fun getUpcomingScheduledExams(fromTime: Long = System.currentTimeMillis()): Flow<List<Exam>> =
        examDao.getUpcomingScheduledExams(fromTime)
    val tbdExams: Flow<List<Exam>> = examDao.getTbdExams()

    suspend fun insertExam(exam: Exam): Long = examDao.insertExam(exam)
    suspend fun updateExam(exam: Exam) = examDao.updateExam(exam)
    suspend fun deleteExam(exam: Exam) = examDao.deleteExam(exam)
}
