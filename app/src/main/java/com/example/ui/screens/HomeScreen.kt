package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.DashboardState
import com.example.ui.UpcomingTimelineItem
import com.example.ui.components.AcademicEventCard
import com.example.ui.components.CalmRingProgress
import com.example.ui.components.ExamCard
import com.example.ui.components.MainTab
import com.example.ui.components.TodayClassCard
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.BelowTerracottaDark
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.MutedSkyBlueContainer
import com.example.ui.theme.MutedSkyBlueDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SoftLavenderContainer
import com.example.ui.theme.SoftLavenderDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    dashboardState: DashboardState,
    onNavigateTab: (MainTab) -> Unit,
    onQuickMarkToday: (subjectId: Long, status: AttendanceStatus) -> Unit,
    onOpenAddSubject: () -> Unit,
    onOpenAddEvent: () -> Unit,
    onOpenAddExam: () -> Unit,
    onToggleTopic: (com.example.data.model.Exam, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val overallStats = dashboardState.overallStats
    val todayClasses = dashboardState.todayClasses
    val timeline = dashboardState.upcomingTimeline

    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val currentDateStr = dateFormat.format(Date())

    val overallTierBg = when {
        overallStats.percentage >= 78.0 -> SageGreenContainer
        overallStats.percentage >= 75.0 -> CautionMustardContainer
        else -> BelowTerracottaContainer
    }

    val overallTierFg = when {
        overallStats.percentage >= 78.0 -> SageGreenDark
        overallStats.percentage >= 75.0 -> CautionMustardDark
        else -> BelowTerracottaDark
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // Calm Greeting & Date Header
        item {
            Column {
                Text(
                    text = currentDateStr,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = CharcoalMuted,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Welcome to Saathi",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = "Your calm college companion",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = CharcoalMuted
                            )
                        )
                    }

                    // Mascot / Calm Icon Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(SageGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = "Academic Companion",
                            tint = SageGreenDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Overall Attendance Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("overall_attendance_card")
                    .clickable { onNavigateTab(MainTab.ATTENDANCE) },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Attendance",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = overallTierBg
                        ) {
                            Text(
                                text = overallStats.statusTier.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = overallTierFg
                                ),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CalmRingProgress(
                            percentage = overallStats.percentage,
                            strokeWidth = 11.dp,
                            size = 100.dp,
                            sublabel = "${overallStats.presentCount}/${overallStats.heldCount}"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = String.format(Locale.US, "%.1f%% Total Standing", overallStats.percentage),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SoftCharcoal
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${overallStats.presentCount} attended • ${overallStats.absentCount} missed • ${overallStats.cancelledCount} cancelled",
                                style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = overallTierBg
                            ) {
                                Text(
                                    text = overallStats.statusMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = overallTierFg,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Shortcuts Chips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionChip(
                    label = "+ Subject",
                    bgColor = SageGreenContainer,
                    fgColor = SageGreenDark,
                    onClick = onOpenAddSubject,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    label = "+ Event",
                    bgColor = MutedSkyBlueContainer,
                    fgColor = MutedSkyBlueDark,
                    onClick = onOpenAddEvent,
                    modifier = Modifier.weight(1f)
                )
                QuickActionChip(
                    label = "+ Exam",
                    bgColor = SoftLavenderContainer,
                    fgColor = SoftLavenderDark,
                    onClick = onOpenAddExam,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Today's Classes & Quick Mark
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Classes",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    )
                    Text(
                        text = "One-tap log",
                        style = MaterialTheme.typography.labelSmall.copy(color = CharcoalMuted)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (todayClasses.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IvorySurfaceCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No classes scheduled for today! Enjoy your free time.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        todayClasses.forEach { item ->
                            TodayClassCard(
                                subject = item.subject,
                                currentStatus = item.todayRecord?.status,
                                onMarkStatus = { status ->
                                    onQuickMarkToday(item.subject.id, status)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Upcoming Deadlines & Academic Events
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Upcoming Schedule & Deadlines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    )
                    Text(
                        text = "View Calendar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = SageGreenDark,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.clickable { onNavigateTab(MainTab.CALENDAR) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (timeline.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = IvorySurfaceCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No upcoming events or exams scheduled.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        timeline.forEach { item ->
                            when (item) {
                                is UpcomingTimelineItem.EventItem -> {
                                    AcademicEventCard(
                                        event = item.event,
                                        onClick = { onNavigateTab(MainTab.CALENDAR) }
                                    )
                                }
                                is UpcomingTimelineItem.ScheduledExamItem -> {
                                    ExamCard(
                                        exam = item.exam,
                                        onToggleTopic = { topicId ->
                                            onToggleTopic(item.exam, topicId)
                                        },
                                        onSetDate = {},
                                        onEditExam = { onNavigateTab(MainTab.EXAMS) },
                                        onDeleteExam = {},
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    bgColor: Color,
    fgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 11.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = fgColor
                )
            )
        }
    }
}
