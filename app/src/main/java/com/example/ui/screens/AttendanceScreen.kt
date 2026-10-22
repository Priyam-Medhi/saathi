package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Subject
import com.example.ui.SubjectWithStats
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.CalmRingProgress
import com.example.ui.components.SetPastAttendanceDialog
import com.example.ui.components.SubjectAttendanceCard
import com.example.ui.components.SubjectHistoryDialog
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
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AttendanceScreen(
    subjectsWithStats: List<SubjectWithStats>,
    allSubjects: List<Subject>,
    allRecords: List<AttendanceRecord>,
    onQuickMark: (subjectId: Long, status: AttendanceStatus) -> Unit,
    onAddSubject: (name: String, code: String, weeklyCount: Int, target: Double, colorHex: String, daysOfWeek: String, initialPresent: Int, initialAbsent: Int, initialCancelled: Int) -> Unit,
    onUpdateSubject: (Subject) -> Unit,
    onDeleteSubject: (Subject) -> Unit,
    onToggleArchive: (subjectId: Long, isArchived: Boolean) -> Unit,
    onUpdateRecord: (AttendanceRecord) -> Unit,
    onDeleteRecord: (AttendanceRecord) -> Unit,
    onAddManualRecord: (subjectId: Long, status: AttendanceStatus, dateMillis: Long, notes: String) -> Unit,
    onSetPastAttendance: (subjectId: Long, attendedCount: Int, missedCount: Int, cancelledCount: Int, replaceExisting: Boolean) -> Unit,
    onExportCsv: (List<SubjectWithStats>) -> String,
    onExportBackupJson: () -> String,
    onRestoreBackupJson: suspend (String) -> Result<String>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0=All, 1=Needs Attention, 2=Archived

    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var inspectingSubjectWithStats by remember { mutableStateOf<SubjectWithStats?>(null) }
    var pastAttendanceSubjectWithStats by remember { mutableStateOf<SubjectWithStats?>(null) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    // Filter list
    val displayedSubjects = when (selectedFilterTab) {
        0 -> subjectsWithStats.filter { !it.subject.isArchived }
        1 -> subjectsWithStats.filter { !it.subject.isArchived && it.stats.percentage < it.subject.targetPercentage }
        2 -> {
            allSubjects.filter { it.isArchived }.map { subj ->
                val records = allRecords.filter { it.subjectId == subj.id }
                val stats = com.example.util.AttendanceCalc.calculate(records, subj.targetPercentage)
                SubjectWithStats(subj, stats, records)
            }
        }
        else -> subjectsWithStats
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("attendance_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header & Action Buttons (CSV Export + Backup & Restore)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Attendance Tracker",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = "Low-stress tracking with smart safe-to-miss guidance",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Backup & Restore Button
                        IconButton(
                            onClick = { showBackupRestoreDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SageGreenContainer)
                                .testTag("btn_backup_restore")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Backup,
                                contentDescription = "Backup & Restore Data",
                                tint = SageGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // CSV Export Button
                        IconButton(
                            onClick = {
                                val csvContent = onExportCsv(subjectsWithStats)
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_SUBJECT, "Saathi Attendance Summary")
                                    putExtra(Intent.EXTRA_TEXT, csvContent)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export Attendance CSV"))
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SageGreenContainer)
                                .testTag("btn_export_csv")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Export CSV",
                                tint = SageGreenDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Information & Past Semester Notice Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SageGreenContainer.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(SageGreenDark)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Joined midway? Tap any subject's menu (⋮) → 'Set Past Attendance' to import classes held so far.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftCharcoal,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            // Filter Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedFilterTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedFilterTab])
                                .padding(horizontal = 24.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF81C784))
                        )
                    }
                ) {
                    Tab(
                        selected = selectedFilterTab == 0,
                        onClick = { selectedFilterTab = 0 },
                        text = {
                            Text(
                                "All Active (${subjectsWithStats.size})",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilterTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilterTab == 0) Color.White else Color(0xFF9CA3AF)
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedFilterTab == 1,
                        onClick = { selectedFilterTab = 1 },
                        text = {
                            val count = subjectsWithStats.count { it.stats.percentage < it.subject.targetPercentage }
                            Text(
                                "Needs Attention ($count)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilterTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilterTab == 1) Color.White else Color(0xFF9CA3AF)
                                )
                            )
                        }
                    )
                    Tab(
                        selected = selectedFilterTab == 2,
                        onClick = { selectedFilterTab = 2 },
                        text = {
                            val count = allSubjects.count { it.isArchived }
                            Text(
                                "Archived ($count)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedFilterTab == 2) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedFilterTab == 2) Color.White else Color(0xFF9CA3AF)
                                )
                            )
                        }
                    )
                }
            }

            // Subject Cards List
            if (displayedSubjects.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = IvorySurfaceCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedFilterTab == 1) "Great job! All your subjects are above target."
                                else if (selectedFilterTab == 2) "No archived subjects."
                                else "No subjects added yet. Tap '+' to create your first subject.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
                            )
                        }
                    }
                }
            } else {
                items(displayedSubjects, key = { it.subject.id }) { item ->
                    SubjectAttendanceCard(
                        subjectWithStats = item,
                        onQuickMark = { status -> onQuickMark(item.subject.id, status) },
                        onOpenHistory = { inspectingSubjectWithStats = item },
                        onSetPastAttendance = { pastAttendanceSubjectWithStats = item },
                        onEditSubject = { editingSubject = item.subject },
                        onToggleArchive = { onToggleArchive(item.subject.id, !item.subject.isArchived) },
                        onDeleteSubject = { onDeleteSubject(item.subject) }
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Floating Action Button to Add Subject
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_subject"),
            containerColor = SageGreenDark,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Subject")
        }
    }

    // Add Subject Dialog
    if (showAddDialog) {
        AddEditSubjectDialog(
            initialSubject = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, code, weeklyCount, target, colorHex, daysOfWeek, initP, initA, initC ->
                onAddSubject(name, code, weeklyCount, target, colorHex, daysOfWeek, initP, initA, initC)
                showAddDialog = false
                Toast.makeText(context, "Added $name", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Edit Subject Dialog
    editingSubject?.let { subjectToEdit ->
        AddEditSubjectDialog(
            initialSubject = subjectToEdit,
            onDismiss = { editingSubject = null },
            onSave = { name, code, weeklyCount, target, colorHex, daysOfWeek, _, _, _ ->
                onUpdateSubject(
                    subjectToEdit.copy(
                        name = name,
                        code = code,
                        weeklyClassCount = weeklyCount,
                        targetPercentage = target,
                        colorHex = colorHex,
                        scheduledDaysOfWeek = daysOfWeek
                    )
                )
                editingSubject = null
                Toast.makeText(context, "Updated $name", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Subject History Dialog Sheet
    inspectingSubjectWithStats?.let { subjectWithStats ->
        val currentItem = subjectsWithStats.firstOrNull { it.subject.id == subjectWithStats.subject.id }
            ?: subjectWithStats
        SubjectHistoryDialog(
            subjectWithStats = currentItem,
            onUpdateRecord = onUpdateRecord,
            onDeleteRecord = onDeleteRecord,
            onAddManualRecord = { status, dateMillis, notes ->
                onAddManualRecord(subjectWithStats.subject.id, status, dateMillis, notes)
            },
            onOpenSetPastAttendance = {
                pastAttendanceSubjectWithStats = currentItem
            },
            onDismiss = { inspectingSubjectWithStats = null }
        )
    }

    // Set Past Attendance Dialog
    pastAttendanceSubjectWithStats?.let { subjectWithStats ->
        val currentItem = subjectsWithStats.firstOrNull { it.subject.id == subjectWithStats.subject.id }
            ?: subjectWithStats
        SetPastAttendanceDialog(
            subjectWithStats = currentItem,
            onDismiss = { pastAttendanceSubjectWithStats = null },
            onSave = { attended, missed, cancelled, replaceExisting ->
                onSetPastAttendance(currentItem.subject.id, attended, missed, cancelled, replaceExisting)
                pastAttendanceSubjectWithStats = null
                Toast.makeText(context, "Updated past attendance for ${currentItem.subject.name}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            onExportJson = onExportBackupJson,
            onRestoreJson = { jsonStr ->
                coroutineScope.launch {
                    val result = onRestoreBackupJson(jsonStr)
                    if (result.isSuccess) {
                        Toast.makeText(context, result.getOrNull() ?: "Data restored successfully!", Toast.LENGTH_LONG).show()
                        showBackupRestoreDialog = false
                    } else {
                        Toast.makeText(context, "Restore failed: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}
