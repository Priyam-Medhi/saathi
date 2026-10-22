package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.Subject
import com.example.ui.SubjectWithStats
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.BelowTerracottaDark
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import java.util.Locale

@Composable
fun SubjectAttendanceCard(
    subjectWithStats: SubjectWithStats,
    onQuickMark: (AttendanceStatus) -> Unit,
    onOpenHistory: () -> Unit,
    onSetPastAttendance: () -> Unit,
    onEditSubject: () -> Unit,
    onToggleArchive: () -> Unit,
    onDeleteSubject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = subjectWithStats.subject
    val stats = subjectWithStats.stats
    var menuExpanded by remember { mutableStateOf(false) }

    val subjectTagColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }
        .getOrDefault(SageGreen)

    val statusTierBg = when {
        stats.percentage >= 78.0 -> SageGreenContainer
        stats.percentage >= 75.0 -> CautionMustardContainer
        else -> BelowTerracottaContainer
    }

    val statusTierFg = when {
        stats.percentage >= 78.0 -> SageGreenDark
        stats.percentage >= 75.0 -> CautionMustardDark
        else -> BelowTerracottaDark
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_card_${subject.id}")
            .clickable { onOpenHistory() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Subject Name, Tag, Code, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(subjectTagColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            ),
                            maxLines = 1
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = subject.code,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CharcoalMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = " • ${subject.weeklyClassCount} classes/wk • Target ${subject.targetPercentage.toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = CharcoalMuted
                                )
                            )
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "Options",
                            tint = CharcoalMuted
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View History & Logs") },
                            onClick = {
                                menuExpanded = false
                                onOpenHistory()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Set Past Attendance") },
                            onClick = {
                                menuExpanded = false
                                onSetPastAttendance()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Subject") },
                            onClick = {
                                menuExpanded = false
                                onEditSubject()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (subject.isArchived) "Unarchive" else "Archive") },
                            onClick = {
                                menuExpanded = false
                                onToggleArchive()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Subject", color = BelowTerracotta) },
                            onClick = {
                                menuExpanded = false
                                onDeleteSubject()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body: Ring Progress + Stats Grid + Calculation Guidance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Circular Ring
                CalmRingProgress(
                    percentage = stats.percentage,
                    strokeWidth = 9.dp,
                    size = 92.dp,
                    sublabel = "${stats.presentCount}/${stats.heldCount}"
                )

                Spacer(modifier = Modifier.width(14.dp))

                // Stats breakdown & Smart Guidance
                Column(modifier = Modifier.weight(1f)) {
                    // Breakdown Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatSmallItem(label = "Attended", value = stats.presentCount.toString(), color = SageGreenDark)
                        StatSmallItem(label = "Missed", value = stats.absentCount.toString(), color = BelowTerracottaDark)
                        StatSmallItem(label = "Cancelled", value = stats.cancelledCount.toString(), color = CharcoalMuted)
                        StatSmallItem(label = "Total Held", value = stats.heldCount.toString(), color = SoftCharcoal)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Calculation message box
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusTierBg,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stats.statusMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = statusTierFg,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions: Quick Log Buttons + History Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuickActionButton(
                        label = "+ Present",
                        bgColor = SageGreenContainer,
                        fgColor = SageGreenDark,
                        onClick = { onQuickMark(AttendanceStatus.PRESENT) }
                    )
                    QuickActionButton(
                        label = "+ Absent",
                        bgColor = BelowTerracottaContainer,
                        fgColor = BelowTerracottaDark,
                        onClick = { onQuickMark(AttendanceStatus.ABSENT) }
                    )
                    QuickActionButton(
                        label = "+ Off",
                        bgColor = CautionMustardContainer,
                        fgColor = CautionMustardDark,
                        onClick = { onQuickMark(AttendanceStatus.CANCELLED) }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onOpenHistory() }
                        .border(1.dp, CharcoalMuted.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "Logs",
                            modifier = Modifier.size(13.dp),
                            tint = CharcoalMuted
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Logs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = CharcoalMuted,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatSmallItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = CharcoalMuted
            )
        )
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    bgColor: Color,
    fgColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = fgColor,
                fontSize = 11.5.sp
            ),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}
