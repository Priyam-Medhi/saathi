package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditExamDialog(
    initialExam: Exam? = null,
    availableSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onSave: (
        subjectId: Long?,
        subjectName: String,
        examType: ExamType,
        isDateTbd: Boolean,
        dateTimeMillis: Long?,
        venue: String,
        topics: List<TopicItem>,
        notes: String
    ) -> Unit
) {
    var subjectName by remember { mutableStateOf(initialExam?.subjectName ?: (availableSubjects.firstOrNull()?.name ?: "")) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(initialExam?.subjectId ?: availableSubjects.firstOrNull()?.id) }
    var examType by remember { mutableStateOf(initialExam?.examType ?: ExamType.MIDSEM) }
    var isDateTbd by remember { mutableStateOf(initialExam?.isDateTbd ?: false) }
    var dateTimeMillis by remember {
        mutableLongStateOf(initialExam?.dateTimeMillis ?: (System.currentTimeMillis() + 86400000L * 7))
    }
    var venue by remember { mutableStateOf(initialExam?.venue ?: "") }
    var notes by remember { mutableStateOf(initialExam?.notes ?: "") }

    val topicsList = remember {
        mutableStateListOf<TopicItem>().apply {
            if (initialExam != null) addAll(initialExam.topics)
        }
    }
    var newTopicTitle by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialExam == null) "Schedule / Add Exam" else "Edit Exam",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftCharcoal
                )
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Subject Quick Picker
                if (availableSubjects.isNotEmpty()) {
                    item {
                        Column {
                            Text(
                                text = "Select Subject",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                availableSubjects.forEach { sub ->
                                    val isSelected = selectedSubjectId == sub.id || subjectName == sub.name
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedSubjectId = sub.id
                                            subjectName = sub.name
                                        },
                                        label = { Text(sub.code.ifBlank { sub.name }, fontSize = 11.5.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = SageGreenContainer,
                                            selectedLabelColor = SageGreenDark
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = subjectName,
                        onValueChange = {
                            subjectName = it
                            hasError = false
                        },
                        label = { Text("Subject or Exam Name") },
                        singleLine = true,
                        isError = hasError && subjectName.isBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_subject")
                    )
                }

                // Exam Type
                item {
                    Column {
                        Text(
                            text = "Exam Type",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ExamType.values().forEach { type ->
                                val isSelected = examType == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { examType = type },
                                    label = { Text(type.label, fontSize = 11.5.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenContainer,
                                        selectedLabelColor = SageGreenDark
                                    )
                                )
                            }
                        }
                    }
                }

                // Date TBD Toggle Switch
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isDateTbd) CautionMustardContainer else SageGreenContainer.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Date TBD (Unscheduled)",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isDateTbd) CautionMustardDark else SoftCharcoal
                                    )
                                )
                                Text(
                                    text = "For quizzes or minors whose dates are not fixed yet",
                                    style = MaterialTheme.typography.labelSmall.copy(color = CharcoalMuted)
                                )
                            }
                            Switch(
                                checked = isDateTbd,
                                onCheckedChange = { isDateTbd = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CautionMustardDark,
                                    checkedTrackColor = CautionMustard
                                )
                            )
                        }
                    }
                }

                // Scheduled Date & Time if not TBD
                if (!isDateTbd) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = SageGreenContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = "Date",
                                        modifier = Modifier.size(15.dp),
                                        tint = SageGreenDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dateFormat.format(Date(dateTimeMillis)),
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    QuickDateShiftChip(label = "+3 Days", onClick = {
                                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 3
                                    })
                                    QuickDateShiftChip(label = "+1 Week", onClick = {
                                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 7
                                    })
                                    QuickDateShiftChip(label = "+2 Weeks", onClick = {
                                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 14
                                    })
                                    QuickDateShiftChip(label = "+1 Month", onClick = {
                                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 30
                                    })
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Exam Venue / Hall (e.g. LH-101, Lab 3)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_exam_venue")
                    )
                }

                // Topics Checklist Builder
                item {
                    Column {
                        Text(
                            text = "Syllabus / Topics Checklist (${topicsList.size} topics)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newTopicTitle,
                                onValueChange = { newTopicTitle = it },
                                label = { Text("Topic name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = {
                                    if (newTopicTitle.isNotBlank()) {
                                        topicsList.add(
                                            TopicItem(
                                                id = UUID.randomUUID().toString(),
                                                title = newTopicTitle.trim(),
                                                isCompleted = false
                                            )
                                        )
                                        newTopicTitle = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SageGreenContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Add Topic",
                                    tint = SageGreenDark
                                )
                            }
                        }

                        if (topicsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                topicsList.forEachIndexed { index, topic ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.dp, CharcoalMuted.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${index + 1}. ${topic.title}",
                                            style = MaterialTheme.typography.bodySmall.copy(color = SoftCharcoal),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { topicsList.remove(topic) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                contentDescription = "Remove",
                                                tint = CharcoalMuted,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Preparation Notes / Reference Rules") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subjectName.isBlank()) {
                        hasError = true
                    } else {
                        onSave(
                            selectedSubjectId,
                            subjectName,
                            examType,
                            isDateTbd,
                            if (isDateTbd) null else dateTimeMillis,
                            venue,
                            topicsList.toList(),
                            notes
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_exam")
            ) {
                Text("Save Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}

@Composable
fun SetExamDateDialog(
    exam: Exam,
    onDismiss: () -> Unit,
    onConfirmDate: (dateTimeMillis: Long) -> Unit
) {
    var dateTimeMillis by remember {
        mutableLongStateOf(System.currentTimeMillis() + 86400000L * 3)
    }
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Set Date for ${exam.subjectName}",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftCharcoal
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Announced date for ${exam.examType.label}:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SageGreenContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dateFormat.format(Date(dateTimeMillis)),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = SageGreenDark
                        ),
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickDateShiftChip(label = "+1 Day", onClick = {
                        dateTimeMillis = System.currentTimeMillis() + 86400000L
                    })
                    QuickDateShiftChip(label = "+3 Days", onClick = {
                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 3
                    })
                    QuickDateShiftChip(label = "+1 Week", onClick = {
                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 7
                    })
                    QuickDateShiftChip(label = "+2 Weeks", onClick = {
                        dateTimeMillis = System.currentTimeMillis() + 86400000L * 14
                    })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmDate(dateTimeMillis) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm Date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}

@Composable
private fun QuickDateShiftChip(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .border(1.dp, SageGreenDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = SageGreenDark,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
        )
    }
}
