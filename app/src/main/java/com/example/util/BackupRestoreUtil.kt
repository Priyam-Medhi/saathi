package com.example.util

import com.example.data.model.AcademicEvent
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.EventCategory
import com.example.data.model.EventRecurrence
import com.example.data.model.Exam
import com.example.data.model.ExamType
import com.example.data.model.Subject
import com.example.data.model.TopicItem
import org.json.JSONArray
import org.json.JSONObject

data class AppBackupData(
    val subjects: List<Subject>,
    val attendanceRecords: List<AttendanceRecord>,
    val events: List<AcademicEvent>,
    val exams: List<Exam>
)

object BackupRestoreUtil {

    fun exportToJson(
        subjects: List<Subject>,
        attendanceRecords: List<AttendanceRecord>,
        events: List<AcademicEvent>,
        exams: List<Exam>
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())
        root.put("app", "Saathi College Companion")

        // Subjects
        val subjectsArray = JSONArray()
        for (s in subjects) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("code", s.code)
                put("weeklyClassCount", s.weeklyClassCount)
                put("targetPercentage", s.targetPercentage)
                put("colorHex", s.colorHex)
                put("scheduledDaysOfWeek", s.scheduledDaysOfWeek)
                put("isArchived", s.isArchived)
                put("createdAt", s.createdAt)
            }
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        // Attendance Records
        val attendanceArray = JSONArray()
        for (r in attendanceRecords) {
            val obj = JSONObject().apply {
                put("id", r.id)
                put("subjectId", r.subjectId)
                put("dateMillis", r.dateMillis)
                put("status", r.status.name)
                put("slotIndex", r.slotIndex)
                put("notes", r.notes)
            }
            attendanceArray.put(obj)
        }
        root.put("attendanceRecords", attendanceArray)

        // Events
        val eventsArray = JSONArray()
        for (e in events) {
            val obj = JSONObject().apply {
                put("id", e.id)
                put("title", e.title)
                put("category", e.category.name)
                put("startMillis", e.startMillis)
                put("endMillis", e.endMillis)
                put("location", e.location)
                put("description", e.description)
                put("recurrence", e.recurrence.name)
                put("reminderMinutes", e.reminderMinutes)
            }
            eventsArray.put(obj)
        }
        root.put("events", eventsArray)

        // Exams
        val examsArray = JSONArray()
        for (ex in exams) {
            val obj = JSONObject().apply {
                put("id", ex.id)
                if (ex.subjectId != null) put("subjectId", ex.subjectId)
                put("subjectName", ex.subjectName)
                put("examType", ex.examType.name)
                put("isDateTbd", ex.isDateTbd)
                if (ex.dateTimeMillis != null) put("dateTimeMillis", ex.dateTimeMillis)
                put("venue", ex.venue)
                put("notes", ex.notes)
                put("reminderMinutes", ex.reminderMinutes)

                val topicsArr = JSONArray()
                for (t in ex.topics) {
                    val tObj = JSONObject().apply {
                        put("id", t.id)
                        put("title", t.title)
                        put("isCompleted", t.isCompleted)
                    }
                    topicsArr.put(tObj)
                }
                put("topics", topicsArr)
            }
            examsArray.put(obj)
        }
        root.put("exams", examsArray)

        return root.toString(2)
    }

    fun parseJson(jsonString: String): Result<AppBackupData> {
        return runCatching {
            val root = JSONObject(jsonString)

            val subjectsList = mutableListOf<Subject>()
            val subjectsArr = root.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjectsArr.length()) {
                val obj = subjectsArr.getJSONObject(i)
                subjectsList.add(
                    Subject(
                        id = obj.optLong("id", 0L),
                        name = obj.getString("name"),
                        code = obj.optString("code", ""),
                        weeklyClassCount = obj.optInt("weeklyClassCount", 4),
                        targetPercentage = obj.optDouble("targetPercentage", 75.0),
                        colorHex = obj.optString("colorHex", "#8FAF8B"),
                        scheduledDaysOfWeek = obj.optString("scheduledDaysOfWeek", "1,2,3,4,5"),
                        isArchived = obj.optBoolean("isArchived", false),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }

            val recordsList = mutableListOf<AttendanceRecord>()
            val recordsArr = root.optJSONArray("attendanceRecords") ?: JSONArray()
            for (i in 0 until recordsArr.length()) {
                val obj = recordsArr.getJSONObject(i)
                val statusStr = obj.optString("status", "PRESENT")
                val status = runCatching { AttendanceStatus.valueOf(statusStr) }.getOrDefault(AttendanceStatus.PRESENT)
                recordsList.add(
                    AttendanceRecord(
                        id = obj.optLong("id", 0L),
                        subjectId = obj.getLong("subjectId"),
                        dateMillis = obj.getLong("dateMillis"),
                        status = status,
                        slotIndex = obj.optInt("slotIndex", 0),
                        notes = obj.optString("notes", "")
                    )
                )
            }

            val eventsList = mutableListOf<AcademicEvent>()
            val eventsArr = root.optJSONArray("events") ?: JSONArray()
            for (i in 0 until eventsArr.length()) {
                val obj = eventsArr.getJSONObject(i)
                val catStr = obj.optString("category", "OTHER")
                val cat = runCatching { EventCategory.valueOf(catStr) }.getOrDefault(EventCategory.OTHER)
                val recStr = obj.optString("recurrence", "NONE")
                val rec = runCatching { EventRecurrence.valueOf(recStr) }.getOrDefault(EventRecurrence.NONE)

                eventsList.add(
                    AcademicEvent(
                        id = obj.optLong("id", 0L),
                        title = obj.getString("title"),
                        category = cat,
                        startMillis = obj.getLong("startMillis"),
                        endMillis = obj.getLong("endMillis"),
                        location = obj.optString("location", ""),
                        description = obj.optString("description", ""),
                        recurrence = rec,
                        reminderMinutes = obj.optInt("reminderMinutes", 60)
                    )
                )
            }

            val examsList = mutableListOf<Exam>()
            val examsArr = root.optJSONArray("exams") ?: JSONArray()
            for (i in 0 until examsArr.length()) {
                val obj = examsArr.getJSONObject(i)
                val examTypeStr = obj.optString("examType", "MIDSEM")
                val examType = runCatching { ExamType.valueOf(examTypeStr) }.getOrDefault(ExamType.MIDSEM)
                val subId = if (obj.has("subjectId") && !obj.isNull("subjectId")) obj.getLong("subjectId") else null
                val dateTime = if (obj.has("dateTimeMillis") && !obj.isNull("dateTimeMillis")) obj.getLong("dateTimeMillis") else null

                val topicsList = mutableListOf<TopicItem>()
                val topicsArr = obj.optJSONArray("topics") ?: JSONArray()
                for (j in 0 until topicsArr.length()) {
                    val tObj = topicsArr.getJSONObject(j)
                    topicsList.add(
                        TopicItem(
                            id = tObj.optString("id", "t_$j"),
                            title = tObj.getString("title"),
                            isCompleted = tObj.optBoolean("isCompleted", false)
                        )
                    )
                }

                examsList.add(
                    Exam(
                        id = obj.optLong("id", 0L),
                        subjectId = subId,
                        subjectName = obj.optString("subjectName", ""),
                        examType = examType,
                        isDateTbd = obj.optBoolean("isDateTbd", false),
                        dateTimeMillis = dateTime,
                        venue = obj.optString("venue", ""),
                        topics = topicsList,
                        notes = obj.optString("notes", ""),
                        reminderMinutes = obj.optInt("reminderMinutes", 1440)
                    )
                )
            }

            AppBackupData(
                subjects = subjectsList,
                attendanceRecords = recordsList,
                events = eventsList,
                exams = examsList
            )
        }
    }
}
