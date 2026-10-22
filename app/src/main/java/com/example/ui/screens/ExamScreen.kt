package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.ExamType
import com.example.data.model.Subject
import com.example.data.model.TopicItem
import com.example.ui.components.AddEditExamDialog
import com.example.ui.components.ExamCard
import com.example.ui.components.SetExamDateDialog
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamScreen(
    allExams: List<Exam>,
    allSubjects: List<Subject>,
    onAddExam: (subjectId: Long?, subjectName: String, examType: ExamType, isDateTbd: Boolean, dateTimeMillis: Long?, venue: String, topics: List<TopicItem>, notes: String) -> Unit,
    onUpdateExam: (Exam) -> Unit,
    onDeleteExam: (Exam) -> Unit,
    onToggleTopic: (Exam, String) -> Unit,
    onSetExamDate: (Exam, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTypeFilter by remember { mutableStateOf<ExamType?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var settingDateExam by remember { mutableStateOf<Exam?>(null) }

    val filteredExams = allExams.filter { exam ->
        selectedTypeFilter == null || exam.examType == selectedTypeFilter
    }

    val tbdExams = filteredExams.filter { it.isDateTbd || it.dateTimeMillis == null }
    val scheduledExams = filteredExams.filter { !it.isDateTbd && it.dateTimeMillis != null }
        .sortedBy { it.dateTimeMillis }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("exam_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            item {
                Column {
                    Text(
                        text = "Exam & Quiz Tracker",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    )
                    Text(
                        text = "Countdown timers, syllabus checklists & date TBD tracking",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                    )
                }
            }

            // Filter Chips (All, Midsem, Endsem, Quiz, Minor, Lab)
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("All Types (${allExams.size})", fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SageGreenContainer,
                            selectedLabelColor = SageGreenDark
                        )
                    )
                    ExamType.values().forEach { type ->
                        val count = allExams.count { it.examType == type }
                        val isSelected = selectedTypeFilter == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTypeFilter = if (isSelected) null else type },
                            label = { Text("${type.label} ($count)", fontSize = 11.5.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageGreenContainer,
                                selectedLabelColor = SageGreenDark
                            )
                        )
                    }
                }
            }

            // Unscheduled / Date TBD Section
            if (tbdExams.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date TBD / Unscheduled Quizzes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CautionMustardDark
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CautionMustardContainer
                            ) {
                                Text(
                                    text = "${tbdExams.size} Pending",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = CautionMustardDark
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            tbdExams.forEach { exam ->
                                ExamCard(
                                    exam = exam,
                                    onToggleTopic = { topicId -> onToggleTopic(exam, topicId) },
                                    onSetDate = { settingDateExam = exam },
                                    onEditExam = { editingExam = exam },
                                    onDeleteExam = { onDeleteExam(exam) }
                                )
                            }
                        }
                    }
                }
            }

            // Scheduled Exams Section
            item {
                Column {
                    Text(
                        text = "Scheduled Exams Timetable",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (scheduledExams.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = IvorySurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tbdExams.isNotEmpty()) "All remaining exams are in Date TBD above."
                                    else "No exams scheduled. Tap '+' to schedule an upcoming test.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            scheduledExams.forEach { exam ->
                                ExamCard(
                                    exam = exam,
                                    onToggleTopic = { topicId -> onToggleTopic(exam, topicId) },
                                    onSetDate = { settingDateExam = exam },
                                    onEditExam = { editingExam = exam },
                                    onDeleteExam = { onDeleteExam(exam) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Floating Action Button to Add Exam
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_exam"),
            containerColor = SageGreenDark,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Exam")
        }
    }

    // Add Exam Dialog
    if (showAddDialog) {
        AddEditExamDialog(
            initialExam = null,
            availableSubjects = allSubjects,
            onDismiss = { showAddDialog = false },
            onSave = { subjectId, subjectName, examType, isDateTbd, dateTimeMillis, venue, topics, notes ->
                onAddExam(subjectId, subjectName, examType, isDateTbd, dateTimeMillis, venue, topics, notes)
                showAddDialog = false
            }
        )
    }

    // Edit Exam Dialog
    editingExam?.let { examToEdit ->
        AddEditExamDialog(
            initialExam = examToEdit,
            availableSubjects = allSubjects,
            onDismiss = { editingExam = null },
            onSave = { subjectId, subjectName, examType, isDateTbd, dateTimeMillis, venue, topics, notes ->
                onUpdateExam(
                    examToEdit.copy(
                        subjectId = subjectId,
                        subjectName = subjectName,
                        examType = examType,
                        isDateTbd = isDateTbd,
                        dateTimeMillis = dateTimeMillis,
                        venue = venue,
                        topics = topics,
                        notes = notes
                    )
                )
                editingExam = null
            }
        )
    }

    // Set Date Dialog for TBD exams
    settingDateExam?.let { tbdExam ->
        SetExamDateDialog(
            exam = tbdExam,
            onDismiss = { settingDateExam = null },
            onConfirmDate = { chosenMillis ->
                onSetExamDate(tbdExam, chosenMillis)
                settingDateExam = null
            }
        )
    }
}
