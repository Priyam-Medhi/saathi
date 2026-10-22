package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.EventDao
import com.example.data.dao.ExamDao
import com.example.data.dao.SubjectDao
import com.example.data.model.AcademicEvent
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.EventCategory
import com.example.data.model.EventRecurrence
import com.example.data.model.Exam
import com.example.data.model.ExamType
import com.example.data.model.Subject
import com.example.data.model.TopicItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        Subject::class,
        AttendanceRecord::class,
        AcademicEvent::class,
        Exam::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun eventDao(): EventDao
    abstract fun examDao(): ExamDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "saathi_college_tracker.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Pre-populate database with starter sample data for first launch
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        seedStarterData(database)
                    }
                }
            }
        }

        suspend fun seedStarterData(db: AppDatabase) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Seed 4 Core Subjects
            val s1 = Subject(
                name = "Data Structures & Algorithms",
                code = "CS201",
                weeklyClassCount = 4,
                targetPercentage = 75.0,
                colorHex = "#8FAF8B", // Sage Green
                scheduledDaysOfWeek = "1,2,3,4,5"
            )
            val s2 = Subject(
                name = "Operating Systems",
                code = "CS302",
                weeklyClassCount = 3,
                targetPercentage = 75.0,
                colorHex = "#A8C5DA", // Muted Sky Blue
                scheduledDaysOfWeek = "1,3,5"
            )
            val s3 = Subject(
                name = "Machine Learning",
                code = "CS415",
                weeklyClassCount = 4,
                targetPercentage = 80.0,
                colorHex = "#C9BFE0", // Soft Lavender
                scheduledDaysOfWeek = "2,4,5"
            )
            val s4 = Subject(
                name = "Computer Networks",
                code = "CS304",
                weeklyClassCount = 3,
                targetPercentage = 75.0,
                colorHex = "#E0B872", // Caution Mustard
                scheduledDaysOfWeek = "1,2,4"
            )

            val s1Id = db.subjectDao().insertSubject(s1)
            val s2Id = db.subjectDao().insertSubject(s2)
            val s3Id = db.subjectDao().insertSubject(s3)
            val s4Id = db.subjectDao().insertSubject(s4)

            // Seed realistic attendance records over the last 3 weeks
            val records = mutableListOf<AttendanceRecord>()

            // s1: 16 classes held: 14 present, 2 absent -> 87.5% (Sage Green comfortable buffer)
            for (i in 1..14) {
                val pastMillis = now - (i * 86400000L * 1.5).toLong()
                records.add(AttendanceRecord(subjectId = s1Id, dateMillis = pastMillis, status = AttendanceStatus.PRESENT))
            }
            records.add(AttendanceRecord(subjectId = s1Id, dateMillis = now - (4 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s1Id, dateMillis = now - (9 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s1Id, dateMillis = now - (12 * 86400000L), status = AttendanceStatus.CANCELLED))

            // s2: 12 classes held: 9 present, 3 absent -> 75% (Caution Mustard close to line)
            for (i in 1..9) {
                val pastMillis = now - (i * 86400000L * 2).toLong()
                records.add(AttendanceRecord(subjectId = s2Id, dateMillis = pastMillis, status = AttendanceStatus.PRESENT))
            }
            records.add(AttendanceRecord(subjectId = s2Id, dateMillis = now - (2 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s2Id, dateMillis = now - (7 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s2Id, dateMillis = now - (11 * 86400000L), status = AttendanceStatus.ABSENT))

            // s3: 10 classes held: 7 present, 3 absent -> 70% (Below Terracotta needs attention)
            for (i in 1..7) {
                val pastMillis = now - (i * 86400000L * 2.2).toLong()
                records.add(AttendanceRecord(subjectId = s3Id, dateMillis = pastMillis, status = AttendanceStatus.PRESENT))
            }
            records.add(AttendanceRecord(subjectId = s3Id, dateMillis = now - (3 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s3Id, dateMillis = now - (6 * 86400000L), status = AttendanceStatus.ABSENT))
            records.add(AttendanceRecord(subjectId = s3Id, dateMillis = now - (10 * 86400000L), status = AttendanceStatus.ABSENT))

            // s4: 8 classes held: 7 present, 1 absent -> 87.5%
            for (i in 1..7) {
                val pastMillis = now - (i * 86400000L * 2.5).toLong()
                records.add(AttendanceRecord(subjectId = s4Id, dateMillis = pastMillis, status = AttendanceStatus.PRESENT))
            }
            records.add(AttendanceRecord(subjectId = s4Id, dateMillis = now - (5 * 86400000L), status = AttendanceStatus.ABSENT))

            db.attendanceDao().insertRecords(records)

            // Seed Upcoming Events
            val event1 = AcademicEvent(
                title = "Inter-College Hackathon 2026",
                category = EventCategory.HACKATHON,
                startMillis = now + (3 * 86400000L),
                endMillis = now + (3 * 86400000L) + (3600000L * 8),
                location = "Auditorium Main Hall",
                description = "48-hr software engineering competition. Team build submissions.",
                recurrence = EventRecurrence.NONE,
                reminderMinutes = 1440
            )
            val event2 = AcademicEvent(
                title = "Robotics Club Weekly Meet",
                category = EventCategory.CLUB_MEET,
                startMillis = now + (1 * 86400000L) + (3600000L * 4),
                endMillis = now + (1 * 86400000L) + (3600000L * 6),
                location = "Lab 304, R&D Block",
                description = "Sprint demo and micro-controller circuit testing.",
                recurrence = EventRecurrence.WEEKLY,
                reminderMinutes = 60
            )
            val event3 = AcademicEvent(
                title = "Annual Sports Meet Registration",
                category = EventCategory.SPORTS,
                startMillis = now + (6 * 86400000L),
                endMillis = now + (6 * 86400000L) + (3600000L * 3),
                location = "Sports Complex Ground",
                description = "Badminton, Athletics and Football team trials.",
                recurrence = EventRecurrence.NONE,
                reminderMinutes = 1440
            )
            db.eventDao().insertEvents(listOf(event1, event2, event3))

            // Seed Exams (Scheduled & Date TBD)
            val exam1 = Exam(
                subjectId = s1Id,
                subjectName = "Data Structures & Algorithms",
                examType = ExamType.MIDSEM,
                isDateTbd = false,
                dateTimeMillis = now + (5 * 86400000L) + (3600000L * 10),
                venue = "LH-101",
                topics = listOf(
                    TopicItem("t1", "Binary Trees & BST Traversals", true),
                    TopicItem("t2", "AVL & Red-Black Trees", true),
                    TopicItem("t3", "Graph Algorithms (Dijkstra, Prim)", false),
                    TopicItem("t4", "Dynamic Programming memoization", false)
                ),
                notes = "Allowed: Non-programmable calculator and 1 page cheat sheet."
            )
            val exam2 = Exam(
                subjectId = s2Id,
                subjectName = "Operating Systems",
                examType = ExamType.MIDSEM,
                isDateTbd = false,
                dateTimeMillis = now + (8 * 86400000L) + (3600000L * 14),
                venue = "LH-204",
                topics = listOf(
                    TopicItem("t1", "Process Synchronization & Semaphores", true),
                    TopicItem("t2", "CPU Scheduling Algorithms", true),
                    TopicItem("t3", "Deadlock Detection & Banker's Algorithm", false),
                    TopicItem("t4", "Paging & Virtual Memory", false)
                ),
                notes = "Focus on numerical problems from Chapter 5."
            )
            val exam3 = Exam(
                subjectId = s3Id,
                subjectName = "Machine Learning",
                examType = ExamType.QUIZ,
                isDateTbd = true, // Date TBD!
                dateTimeMillis = null,
                venue = "Lab 4, AI Dept",
                topics = listOf(
                    TopicItem("t1", "Linear Regression & Gradient Descent", true),
                    TopicItem("t2", "Logistic Regression & Regularization", false),
                    TopicItem("t3", "Support Vector Machines", false)
                ),
                notes = "Surprise quiz announced by Dr. Sharma. Will happen next week."
            )
            db.examDao().insertExams(listOf(exam1, exam2, exam3))
        }
    }
}
