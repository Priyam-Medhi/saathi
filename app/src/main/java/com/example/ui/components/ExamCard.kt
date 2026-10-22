package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exam
import com.example.data.model.ExamType
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftLavenderContainer
import com.example.ui.theme.SoftLavenderDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ExamCard(
    exam: Exam,
    onToggleTopic: (String) -> Unit,
    onSetDate: () -> Unit,
    onEditExam: () -> Unit,
    onDeleteExam: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedTopics by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val completedTopicsCount = exam.topics.count { it.isCompleted }
    val totalTopicsCount = exam.topics.size
    val topicProgress = if (totalTopicsCount > 0) completedTopicsCount.toFloat() / totalTopicsCount.toFloat() else 0f

    // Calculate countdown text & styling
    val now = System.currentTimeMillis()
    val countdownText: String
    val badgeBg: Color
    val badgeFg: Color

    if (exam.isDateTbd || exam.dateTimeMillis == null) {
        countdownText = "Date TBD"
        badgeBg = CautionMustardContainer
        badgeFg = CautionMustardDark
    } else {
        val diffMillis = exam.dateTimeMillis - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)

        when {
            diffMillis < 0 -> {
                countdownText = "Completed"
                badgeBg = CharcoalMuted.copy(alpha = 0.15f)
                badgeFg = CharcoalMuted
            }
            days == 0L && hours <= 0 -> {
                countdownText = "Happening Now"
                badgeBg = BelowTerracottaContainer
                badgeFg = BelowTerracotta
            }
            days == 0L -> {
                countdownText = "Today (${hours}h left)"
                badgeBg = BelowTerracottaContainer
                badgeFg = BelowTerracotta
            }
            days == 1L -> {
                countdownText = "Tomorrow"
                badgeBg = CautionMustardContainer
                badgeFg = CautionMustardDark
            }
            else -> {
                countdownText = "$days days left"
                badgeBg = if (days <= 3) CautionMustardContainer else SageGreenContainer
                badgeFg = if (days <= 3) CautionMustardDark else SageGreenDark
            }
        }
    }

    val typeColor = when (exam.examType) {
        ExamType.MIDSEM -> SoftLavenderDark
        ExamType.ENDSEM -> BelowTerracotta
        ExamType.QUIZ -> CautionMustardDark
        ExamType.MINOR -> SageGreenDark
        ExamType.LAB_EXAM -> SoftCharcoal
        ExamType.OTHER -> CharcoalMuted
    }

    val typeBg = when (exam.examType) {
        ExamType.MIDSEM -> SoftLavenderContainer
        ExamType.ENDSEM -> BelowTerracottaContainer
        ExamType.QUIZ -> CautionMustardContainer
        ExamType.MINOR -> SageGreenContainer
        ExamType.LAB_EXAM -> IvorySurfaceCard
        ExamType.OTHER -> IvorySurfaceCard
    }

    val dateTimeFormatted = exam.dateTimeMillis?.let {
        val df = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())
        df.format(Date(it))
    } ?: "Date not announced yet"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Type Pill + Countdown Badge + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exam Type Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = typeBg
                    ) {
                        Text(
                            text = exam.examType.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = typeColor,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Countdown Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = countdownText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = badgeFg,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Exam Options",
                            tint = CharcoalMuted
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Exam Details") },
                            onClick = {
                                menuExpanded = false
                                onEditExam()
                            }
                        )
                        if (exam.isDateTbd) {
                            DropdownMenuItem(
                                text = { Text("Set Exam Date") },
                                onClick = {
                                    menuExpanded = false
                                    onSetDate()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Delete Exam", color = BelowTerracotta) },
                            onClick = {
                                menuExpanded = false
                                onDeleteExam()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subject / Title
            Text(
                text = exam.subjectName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftCharcoal
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Date & Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = "Date and Time",
                    tint = CharcoalMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = dateTimeFormatted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (exam.isDateTbd) CautionMustardDark else CharcoalMuted,
                        fontWeight = if (exam.isDateTbd) FontWeight.Medium else FontWeight.Normal
                    )
                )
            }

            // Venue / Room
            if (exam.venue.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Venue",
                        tint = CharcoalMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Venue: ${exam.venue}",
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                    )
                }
            }

            // Notes
            if (exam.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = exam.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftCharcoal.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                )
            }

            // Date TBD Action Button
            if (exam.isDateTbd) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = CautionMustardContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSetDate() }
                        .border(1.dp, CautionMustard.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditCalendar,
                            contentDescription = "Set Date",
                            tint = CautionMustardDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Set Announced Exam Date",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = CautionMustardDark
                            )
                        )
                    }
                }
            }

            // Syllabus / Topic Checklist Section
            if (exam.topics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { expandedTopics = !expandedTopics },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Syllabus Checklist ($completedTopicsCount/$totalTopicsCount done)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = SoftCharcoal
                            )
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${(topicProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = SageGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = if (expandedTopics) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                            contentDescription = "Toggle Syllabus",
                            tint = CharcoalMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { topicProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (topicProgress >= 1f) SageGreen else SageGreenDark,
                    trackColor = SageGreenContainer
                )

                // Expandable Topic Checkboxes
                AnimatedVisibility(
                    visible = expandedTopics,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        exam.topics.forEach { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onToggleTopic(topic.id) }
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = topic.isCompleted,
                                    onCheckedChange = { onToggleTopic(topic.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = SageGreen,
                                        uncheckedColor = CharcoalMuted
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = topic.title,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if (topic.isCompleted) CharcoalMuted else SoftCharcoal,
                                        textDecoration = if (topic.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
