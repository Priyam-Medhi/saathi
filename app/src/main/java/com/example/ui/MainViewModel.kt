package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.AcademicEvent
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.EventCategory
import com.example.data.model.Exam
import com.example.data.model.ExamType
import com.example.data.model.Subject
import com.example.data.model.TopicItem
import com.example.data.repository.SaathiRepository
import com.example.util.AttendanceCalc
import com.example.util.AttendanceSummary
import com.example.util.AttendanceTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SubjectWithStats(
    val subject: Subject,
    val stats: AttendanceSummary,
    val records: List<AttendanceRecord>
)

data class TodayClassItem(
    val subject: Subject,
    val todayRecord: AttendanceRecord?
)

sealed class UpcomingTimelineItem(val timeMillis: Long) {
    data class EventItem(val event: AcademicEvent) : UpcomingTimelineItem(event.startMillis)
    data class ScheduledExamItem(val exam: Exam) : UpcomingTimelineItem(exam.dateTimeMillis ?: 0L)
}

enum class CalendarViewMode(val label: String) {
    MONTH("Month"),
    WEEK("Week"),
    AGENDA("Agenda")
}

data class DashboardState(
    val overallStats: AttendanceSummary = AttendanceSummary(
        presentCount = 0,
        absentCount = 0,
        cancelledCount = 0,
        heldCount = 0,
        percentage = 100.0,
        targetPercentage = 75.0,
        statusTier = AttendanceTier.COMFORTABLE,
        statusMessage = "All caught up",
        classesSafeToMiss = 0,
        classesNeededToAttend = 0
    ),
    val subjectsWithStats: List<SubjectWithStats> = emptyList(),
    val todayClasses: List<TodayClassItem> = emptyList(),
    val upcomingTimeline: List<UpcomingTimelineItem> = emptyList(),
    val unreadAlertsCount: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SaathiRepository
    init {
        val db = AppDatabase.getInstance(application)
        repository = SaathiRepository(
            subjectDao = db.subjectDao(),
            attendanceDao = db.attendanceDao(),
            eventDao = db.eventDao(),
            examDao = db.examDao()
        )
    }

    val allSubjects: StateFlow<List<Subject>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeSubjects: StateFlow<List<Subject>> = repository.activeSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecord>> = repository.allAttendanceRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<AcademicEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExams: StateFlow<List<Exam>> = repository.allExams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar state
    val selectedCalendarDate = MutableStateFlow(System.currentTimeMillis())
    val calendarMode = MutableStateFlow(CalendarViewMode.MONTH)

    // Exam screen filter state
    val examTypeFilter = MutableStateFlow<ExamType?>(null)
    val examSubjectFilter = MutableStateFlow<Long?>(null)

    // Attendance screen filter
    val attendanceTabFilter = MutableStateFlow(0) // 0 = Active, 1 = Needs Attention, 2 = Archived

    // Unified Dashboard State
    val dashboardState: StateFlow<DashboardState> = combine(
        repository.allSubjects,
        repository.allAttendanceRecords,
        repository.allEvents,
        repository.allExams
    ) { subjects, records, events, exams ->
        val activeList = subjects.filter { !it.isArchived }

        // Compute stats for each subject
        val subjectsWithStats = activeList.map { subj ->
            val subjRecords = records.filter { it.subjectId == subj.id }
            val stats = AttendanceCalc.calculate(subjRecords, subj.targetPercentage)
            SubjectWithStats(subj, stats, subjRecords)
        }

        // Overall attendance combined (all active subjects)
        val activeRecords = records.filter { rec -> activeList.any { it.id == rec.subjectId } }
        val overallStats = AttendanceCalc.calculate(activeRecords, 75.0)

        // Today's classes
        val cal = Calendar.getInstance()
        val dayOfWeekInt = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        val endOfToday = startOfToday + 86400000L

        val todayRecords = records.filter { it.dateMillis in startOfToday until endOfToday }

        // Scheduled for today or subjects marked active
        val scheduledToday = activeList.filter { subj ->
            val daysList = subj.scheduledDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            daysList.isEmpty() || daysList.contains(dayOfWeekInt)
        }

        val todayClasses = scheduledToday.map { subj ->
            val recordForSubj = todayRecords.firstOrNull { it.subjectId == subj.id }
            TodayClassItem(subj, recordForSubj)
        }

        // Upcoming Timeline (next 3-5 items from events + scheduled exams)
        val now = System.currentTimeMillis()
        val upcomingEvents = events.filter { it.endMillis >= now }.map { UpcomingTimelineItem.EventItem(it) }
        val upcomingExams = exams.filter { !it.isDateTbd && (it.dateTimeMillis ?: 0L) >= (now - 3600000L * 4) }
            .map { UpcomingTimelineItem.ScheduledExamItem(it) }

        val combinedTimeline = (upcomingEvents + upcomingExams)
            .sortedBy { it.timeMillis }
            .take(5)

        DashboardState(
            overallStats = overallStats,
            subjectsWithStats = subjectsWithStats,
            todayClasses = todayClasses,
            upcomingTimeline = combinedTimeline
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    // -------------------------------------------------------------
    // SUBJECT ACTIONS
    // -------------------------------------------------------------
    fun addSubject(
        name: String,
        code: String,
        weeklyCount: Int,
        target: Double,
        colorHex: String,
        daysOfWeek: String,
        initialPresent: Int = 0,
        initialAbsent: Int = 0,
        initialCancelled: Int = 0
    ) {
        viewModelScope.launch {
            val subject = Subject(
                name = name.trim(),
                code = code.trim().uppercase(Locale.getDefault()),
                weeklyClassCount = weeklyCount,
                targetPercentage = target,
                colorHex = colorHex,
                scheduledDaysOfWeek = daysOfWeek
            )
            val newId = repository.insertSubject(subject)
            if (initialPresent > 0 || initialAbsent > 0 || initialCancelled > 0) {
                repository.setPastAttendance(
                    subjectId = newId,
                    attendedCount = initialPresent,
                    missedCount = initialAbsent,
                    cancelledCount = initialCancelled,
                    replaceExisting = false
                )
            }
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun toggleArchiveSubject(subjectId: Long, isArchived: Boolean) {
        viewModelScope.launch {
            repository.setArchived(subjectId, isArchived)
        }
    }

    // -------------------------------------------------------------
    // ATTENDANCE ACTIONS
    // -------------------------------------------------------------
    fun setPastAttendance(
        subjectId: Long,
        attendedCount: Int,
        missedCount: Int,
        cancelledCount: Int = 0,
        replaceExisting: Boolean = true
    ) {
        viewModelScope.launch {
            repository.setPastAttendance(
                subjectId = subjectId,
                attendedCount = attendedCount,
                missedCount = missedCount,
                cancelledCount = cancelledCount,
                replaceExisting = replaceExisting
            )
        }
    }

    fun quickMarkToday(subjectId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            repository.markTodayAttendance(subjectId, status)
        }
    }

    fun logAttendanceCustom(subjectId: Long, status: AttendanceStatus, dateMillis: Long, notes: String = "") {
        viewModelScope.launch {
            repository.logAttendance(subjectId, status, dateMillis, notes)
        }
    }

    fun updateAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.updateRecord(record)
        }
    }

    fun deleteAttendanceRecord(record: AttendanceRecord) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun deleteAttendanceRecordById(id: Long) {
        viewModelScope.launch {
            repository.deleteRecordById(id)
        }
    }

    // -------------------------------------------------------------
    // EVENT ACTIONS
    // -------------------------------------------------------------
    fun addEvent(
        title: String,
        category: EventCategory,
        startMillis: Long,
        endMillis: Long,
        location: String,
        description: String,
        recurrence: com.example.data.model.EventRecurrence = com.example.data.model.EventRecurrence.NONE,
        reminderMinutes: Int = 60
    ) {
        viewModelScope.launch {
            val event = AcademicEvent(
                title = title.trim(),
                category = category,
                startMillis = startMillis,
                endMillis = endMillis,
                location = location.trim(),
                description = description.trim(),
                recurrence = recurrence,
                reminderMinutes = reminderMinutes
            )
            repository.insertEvent(event)
        }
    }

    fun updateEvent(event: AcademicEvent) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }

    fun deleteEvent(event: AcademicEvent) {
        viewModelScope.launch {
            repository.deleteEvent(event)
        }
    }

    // -------------------------------------------------------------
    // EXAM ACTIONS
    // -------------------------------------------------------------
    fun addExam(
        subjectId: Long?,
        subjectName: String,
        examType: ExamType,
        isDateTbd: Boolean,
        dateTimeMillis: Long?,
        venue: String,
        topics: List<TopicItem>,
        notes: String
    ) {
        viewModelScope.launch {
            val exam = Exam(
                subjectId = subjectId,
                subjectName = subjectName.trim(),
                examType = examType,
                isDateTbd = isDateTbd,
                dateTimeMillis = if (isDateTbd) null else dateTimeMillis,
                venue = venue.trim(),
                topics = topics,
                notes = notes.trim()
            )
            repository.insertExam(exam)
        }
    }

    fun updateExam(exam: Exam) {
        viewModelScope.launch {
            repository.updateExam(exam)
        }
    }

    fun deleteExam(exam: Exam) {
        viewModelScope.launch {
            repository.deleteExam(exam)
        }
    }

    fun toggleTopicCompleted(exam: Exam, topicId: String) {
        viewModelScope.launch {
            val updatedTopics = exam.topics.map {
                if (it.id == topicId) it.copy(isCompleted = !it.isCompleted) else it
            }
            repository.updateExam(exam.copy(topics = updatedTopics))
        }
    }

    fun setExamDateForTbd(exam: Exam, dateTimeMillis: Long) {
        viewModelScope.launch {
            repository.updateExam(exam.copy(isDateTbd = false, dateTimeMillis = dateTimeMillis))
        }
    }

    // -------------------------------------------------------------
    // CSV EXPORT GENERATOR
    // -------------------------------------------------------------
    fun generateAttendanceCsv(subjectsWithStats: List<SubjectWithStats>): String {
        val sb = StringBuilder()
        sb.append("Subject Code,Subject Name,Present,Absent,Cancelled,Held Classes,Attendance %,Target %,Status,Action Guidance\n")
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        for (item in subjectsWithStats) {
            val s = item.subject
            val st = item.stats
            val pctStr = String.format(Locale.US, "%.1f%%", st.percentage)
            val tgtStr = String.format(Locale.US, "%.0f%%", st.targetPercentage)
            sb.append("\"${s.code}\",\"${s.name}\",${st.presentCount},${st.absentCount},${st.cancelledCount},${st.heldCount},\"$pctStr\",\"$tgtStr\",\"${st.statusTier.label}\",\"${st.statusMessage.replace("\"", "'")}\"\n")
        }

        sb.append("\n\nDetailed Attendance Logs\n")
        sb.append("Subject,Date,Status,Notes\n")
        for (item in subjectsWithStats) {
            for (rec in item.records) {
                val dateStr = df.format(Date(rec.dateMillis))
                sb.append("\"${item.subject.name}\",\"$dateStr\",\"${rec.status.name}\",\"${rec.notes.replace("\"", "'")}\"\n")
            }
        }
        return sb.toString()
    }

    // -------------------------------------------------------------
    // BACKUP & RESTORE
    // -------------------------------------------------------------
    fun exportBackupJson(): String {
        return com.example.util.BackupRestoreUtil.exportToJson(
            subjects = allSubjects.value,
            attendanceRecords = allRecords.value,
            events = allEvents.value,
            exams = allExams.value
        )
    }

    suspend fun restoreBackupJson(jsonString: String): Result<String> {
        return runCatching {
            val parseResult = com.example.util.BackupRestoreUtil.parseJson(jsonString)
            if (parseResult.isFailure) {
                throw parseResult.exceptionOrNull() ?: Exception("Failed to parse backup JSON")
            }
            val backupData = parseResult.getOrThrow()

            // Safe restoration with ID mapping
            val db = AppDatabase.getInstance(getApplication())
            val idMapping = mutableMapOf<Long, Long>()

            for (subj in backupData.subjects) {
                val existing = db.subjectDao().getSubjectById(subj.id)
                val assignedId = if (existing == null) {
                    db.subjectDao().insertSubject(subj)
                } else {
                    // Update existing
                    db.subjectDao().updateSubject(subj)
                    subj.id
                }
                idMapping[subj.id] = assignedId
            }

            // Attendance records
            val recordsToInsert = backupData.attendanceRecords.map { rec ->
                val mappedSubjId = idMapping[rec.subjectId] ?: rec.subjectId
                rec.copy(subjectId = mappedSubjId)
            }
            if (recordsToInsert.isNotEmpty()) {
                db.attendanceDao().insertRecords(recordsToInsert)
            }

            // Events
            if (backupData.events.isNotEmpty()) {
                db.eventDao().insertEvents(backupData.events)
            }

            // Exams
            val examsToInsert = backupData.exams.map { exam ->
                val mappedSubjId = exam.subjectId?.let { idMapping[it] ?: it }
                exam.copy(subjectId = mappedSubjId)
            }
            if (examsToInsert.isNotEmpty()) {
                db.examDao().insertExams(examsToInsert)
            }

            "Successfully restored ${backupData.subjects.size} subjects, ${backupData.attendanceRecords.size} attendance records, ${backupData.events.size} events, and ${backupData.exams.size} exams."
        }
    }
}
