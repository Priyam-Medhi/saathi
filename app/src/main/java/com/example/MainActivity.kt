package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.ui.MainViewModel
import com.example.ui.components.AddEditEventDialog
import com.example.ui.components.AddEditExamDialog
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.MainTab
import com.example.ui.components.SaathiBottomNavBar
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.SaathiTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SaathiTheme {
                SaathiApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SaathiApp(viewModel: MainViewModel) {
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    // Observers
    val dashboardState by viewModel.dashboardState.collectAsState()
    val allSubjects by viewModel.allSubjects.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val allExams by viewModel.allExams.collectAsState()

    // Global Add Dialogs (opened from Home shortcuts)
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var showAddExamDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SaathiBottomNavBar(
                selectedTab = currentTab,
                onTabSelected = { currentTab = it }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(
                targetState = currentTab,
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            dashboardState = dashboardState,
                            onNavigateTab = { currentTab = it },
                            onQuickMarkToday = { subjectId, status ->
                                viewModel.quickMarkToday(subjectId, status)
                            },
                            onOpenAddSubject = { showAddSubjectDialog = true },
                            onOpenAddEvent = { showAddEventDialog = true },
                            onOpenAddExam = { showAddExamDialog = true },
                            onToggleTopic = { exam, topicId ->
                                viewModel.toggleTopicCompleted(exam, topicId)
                            }
                        )
                    }

                    MainTab.ATTENDANCE -> {
                        AttendanceScreen(
                            subjectsWithStats = dashboardState.subjectsWithStats,
                            allSubjects = allSubjects,
                            allRecords = allRecords,
                            onQuickMark = { subjectId, status ->
                                viewModel.logAttendanceCustom(subjectId, status, System.currentTimeMillis())
                            },
                            onAddSubject = { name, code, weekly, target, color, days, initP, initA, initC ->
                                viewModel.addSubject(name, code, weekly, target, color, days, initP, initA, initC)
                            },
                            onUpdateSubject = { viewModel.updateSubject(it) },
                            onDeleteSubject = { viewModel.deleteSubject(it) },
                            onToggleArchive = { subjectId, isArchived ->
                                viewModel.toggleArchiveSubject(subjectId, isArchived)
                            },
                            onUpdateRecord = { viewModel.updateAttendanceRecord(it) },
                            onDeleteRecord = { viewModel.deleteAttendanceRecord(it) },
                            onAddManualRecord = { subjectId, status, dateMillis, notes ->
                                viewModel.logAttendanceCustom(subjectId, status, dateMillis, notes)
                            },
                            onSetPastAttendance = { subjectId, attended, missed, cancelled, replaceExisting ->
                                viewModel.setPastAttendance(subjectId, attended, missed, cancelled, replaceExisting)
                            },
                            onExportCsv = { list ->
                                viewModel.generateAttendanceCsv(list)
                            },
                            onExportBackupJson = {
                                viewModel.exportBackupJson()
                            },
                            onRestoreBackupJson = { jsonStr ->
                                viewModel.restoreBackupJson(jsonStr)
                            }
                        )
                    }

                    MainTab.CALENDAR -> {
                        CalendarScreen(
                            allEvents = allEvents,
                            allExams = allExams,
                            onAddEvent = { title, cat, start, end, loc, desc, rec, rem ->
                                viewModel.addEvent(title, cat, start, end, loc, desc, rec, rem)
                            },
                            onUpdateEvent = { viewModel.updateEvent(it) },
                            onDeleteEvent = { viewModel.deleteEvent(it) },
                            onToggleTopic = { exam, topicId ->
                                viewModel.toggleTopicCompleted(exam, topicId)
                            }
                        )
                    }

                    MainTab.EXAMS -> {
                        ExamScreen(
                            allExams = allExams,
                            allSubjects = allSubjects,
                            onAddExam = { subId, subName, type, isTbd, dateMillis, venue, topics, notes ->
                                viewModel.addExam(subId, subName, type, isTbd, dateMillis, venue, topics, notes)
                            },
                            onUpdateExam = { viewModel.updateExam(it) },
                            onDeleteExam = { viewModel.deleteExam(it) },
                            onToggleTopic = { exam, topicId ->
                                viewModel.toggleTopicCompleted(exam, topicId)
                            },
                            onSetExamDate = { exam, dateMillis ->
                                viewModel.setExamDateForTbd(exam, dateMillis)
                            }
                        )
                    }
                }
            }
        }

        // Global Shortcut Add Dialogs
        if (showAddSubjectDialog) {
            AddEditSubjectDialog(
                initialSubject = null,
                onDismiss = { showAddSubjectDialog = false },
                onSave = { name, code, weekly, target, color, days, initP, initA, initC ->
                    viewModel.addSubject(name, code, weekly, target, color, days, initP, initA, initC)
                    showAddSubjectDialog = false
                }
            )
        }

        if (showAddEventDialog) {
            AddEditEventDialog(
                initialEvent = null,
                onDismiss = { showAddEventDialog = false },
                onSave = { title, cat, start, end, loc, desc, rec, rem ->
                    viewModel.addEvent(title, cat, start, end, loc, desc, rec, rem)
                    showAddEventDialog = false
                }
            )
        }

        if (showAddExamDialog) {
            AddEditExamDialog(
                initialExam = null,
                availableSubjects = allSubjects,
                onDismiss = { showAddExamDialog = false },
                onSave = { subId, subName, type, isTbd, dateMillis, venue, topics, notes ->
                    viewModel.addExam(subId, subName, type, isTbd, dateMillis, venue, topics, notes)
                    showAddExamDialog = false
                }
            )
        }
    }
}
