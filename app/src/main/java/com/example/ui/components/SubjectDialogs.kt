package com.example.ui.components

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Subject
import com.example.ui.SubjectWithStats
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.BelowTerracottaDark
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvoryBackground
import com.example.ui.theme.IvorySurface
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

private val CALM_PALETTE = listOf(
    "#8FAF8B", // Sage Green
    "#A8C5DA", // Sky Blue
    "#C9BFE0", // Soft Lavender
    "#E0B872", // Caution Mustard
    "#D08770", // Terracotta
    "#9DBBB5"  // Soft Teal
)

private val DAYS_OF_WEEK = listOf(
    1 to "Mon",
    2 to "Tue",
    3 to "Wed",
    4 to "Thu",
    5 to "Fri",
    6 to "Sat",
    7 to "Sun"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditSubjectDialog(
    initialSubject: Subject? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        code: String,
        weeklyCount: Int,
        target: Double,
        colorHex: String,
        daysOfWeek: String,
        initialPresent: Int,
        initialAbsent: Int,
        initialCancelled: Int
    ) -> Unit
) {
    var name by remember { mutableStateOf(initialSubject?.name ?: "") }
    var code by remember { mutableStateOf(initialSubject?.code ?: "") }
    var weeklyCount by remember { mutableIntStateOf(initialSubject?.weeklyClassCount ?: 4) }
    var targetPercentage by remember { mutableDoubleStateOf(initialSubject?.targetPercentage ?: 75.0) }
    var selectedColor by remember { mutableStateOf(initialSubject?.colorHex ?: CALM_PALETTE[0]) }

    // Old / initial attendance fields (for new subjects or adjustments)
    var initialPresentStr by remember { mutableStateOf("") }
    var initialAbsentStr by remember { mutableStateOf("") }
    var initialCancelledStr by remember { mutableStateOf("") }

    val initialDays = initialSubject?.scheduledDaysOfWeek?.split(",")?.mapNotNull { it.trim().toIntOrNull() }
        ?: listOf(1, 2, 3, 4, 5)
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(initialDays) } }

    var hasError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSubject == null) "Add New Subject" else "Edit Subject",
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
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            hasError = false
                        },
                        label = { Text("Subject Name (e.g. Data Structures)") },
                        singleLine = true,
                        isError = hasError && name.isBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_subject_name")
                    )
                }

                item {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it },
                        label = { Text("Course Code (e.g. CS201)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_subject_code")
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Weekly Classes: $weeklyCount",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Slider(
                            value = weeklyCount.toFloat(),
                            onValueChange = { weeklyCount = it.toInt() },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = SageGreenDark,
                                activeTrackColor = SageGreen
                            )
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Target Attendance: ${targetPercentage.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Slider(
                            value = targetPercentage.toFloat(),
                            onValueChange = { targetPercentage = it.toDouble() },
                            valueRange = 50f..95f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = SageGreenDark,
                                activeTrackColor = SageGreen
                            )
                        )
                    }
                }

                // Initial / Past Semester Attendance Section (only shown when creating new subject)
                if (initialSubject == null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = IvorySurfaceCard,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, SageGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Past Semester Attendance (Optional)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SoftCharcoal
                                    )
                                )
                                Text(
                                    text = "Starting midway in the semester? Enter already completed classes.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = CharcoalMuted,
                                        fontSize = 11.5.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = initialPresentStr,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) initialPresentStr = it },
                                        label = { Text("Attended") },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = initialAbsentStr,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) initialAbsentStr = it },
                                        label = { Text("Missed") },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = initialCancelledStr,
                                        onValueChange = { if (it.all { char -> char.isDigit() }) initialCancelledStr = it },
                                        label = { Text("Off/Cancel") },
                                        placeholder = { Text("0") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                val p = initialPresentStr.toIntOrNull() ?: 0
                                val a = initialAbsentStr.toIntOrNull() ?: 0
                                val held = p + a
                                if (held > 0) {
                                    val pct = (p.toDouble() / held.toDouble()) * 100.0
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Initial Standing: $p/$held classes (${String.format(Locale.US, "%.1f", pct)}%)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SageGreenDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Class Schedule Days",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DAYS_OF_WEEK.forEach { (dayInt, dayLabel) ->
                                val isSelected = selectedDays.contains(dayInt)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedDays.remove(dayInt)
                                        else selectedDays.add(dayInt)
                                    },
                                    label = { Text(dayLabel, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = SageGreenContainer,
                                        selectedLabelColor = SageGreenDark
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Column {
                        Text(
                            text = "Color Tag",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CALM_PALETTE.forEach { hex ->
                                val color = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedColor = hex }
                                        .then(
                                            if (isSelected) Modifier.border(2.5.dp, SoftCharcoal, CircleShape)
                                            else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Outlined.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        hasError = true
                    } else {
                        val daysStr = selectedDays.sorted().joinToString(",")
                        val initP = initialPresentStr.toIntOrNull() ?: 0
                        val initA = initialAbsentStr.toIntOrNull() ?: 0
                        val initC = initialCancelledStr.toIntOrNull() ?: 0
                        onSave(name, code, weeklyCount, targetPercentage, selectedColor, daysStr, initP, initA, initC)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_subject")
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CharcoalMuted)
            }
        }
    )
}

/**
 * Dedicated Dialog allowing users to input/adjust their semester attendance baseline in bulk.
 */
@Composable
fun SetPastAttendanceDialog(
    subjectWithStats: SubjectWithStats,
    onDismiss: () -> Unit,
    onSave: (attendedCount: Int, missedCount: Int, cancelledCount: Int, replaceExisting: Boolean) -> Unit
) {
    val subject = subjectWithStats.subject
    val stats = subjectWithStats.stats

    var attendedStr by remember { mutableStateOf(stats.presentCount.toString()) }
    var missedStr by remember { mutableStateOf(stats.absentCount.toString()) }
    var cancelledStr by remember { mutableStateOf(stats.cancelledCount.toString()) }
    var replaceExisting by remember { mutableStateOf(true) }

    val attended = attendedStr.toIntOrNull() ?: 0
    val missed = missedStr.toIntOrNull() ?: 0
    val cancelled = cancelledStr.toIntOrNull() ?: 0
    val totalHeld = attended + missed

    val previewPercentage = if (totalHeld > 0) (attended.toDouble() / totalHeld.toDouble()) * 100.0 else 100.0
    val previewTierBg = when {
        previewPercentage >= 78.0 -> SageGreenContainer
        previewPercentage >= 75.0 -> CautionMustardContainer
        else -> BelowTerracottaContainer
    }
    val previewTierFg = when {
        previewPercentage >= 78.0 -> SageGreenDark
        previewPercentage >= 75.0 -> CautionMustardDark
        else -> BelowTerracottaDark
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IvoryBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set Past Attendance",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted),
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = CharcoalMuted)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Enter your current semester figures from your college portal / attendance register:",
                    style = MaterialTheme.typography.bodySmall.copy(color = SoftCharcoal, fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stepper + Inputs
                AttendanceCounterRow(
                    label = "Classes Attended (Present)",
                    value = attended,
                    color = SageGreenDark,
                    onValueChange = { attendedStr = it.toString() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AttendanceCounterRow(
                    label = "Classes Missed (Absent)",
                    value = missed,
                    color = BelowTerracottaDark,
                    onValueChange = { missedStr = it.toString() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                AttendanceCounterRow(
                    label = "Cancelled / Off Classes",
                    value = cancelled,
                    color = CautionMustardDark,
                    onValueChange = { cancelledStr = it.toString() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = previewTierBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Resulting Attendance",
                                style = MaterialTheme.typography.labelSmall.copy(color = previewTierFg)
                            )
                            Text(
                                text = "$attended/$totalHeld classes held",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = SoftCharcoal
                                )
                            )
                        }
                        Text(
                            text = String.format(Locale.US, "%.1f%%", previewPercentage),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = previewTierFg
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Replace vs Append toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { replaceExisting = !replaceExisting }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it },
                        colors = CheckboxDefaults.colors(checkedColor = SageGreenDark)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Replace existing logs with this baseline",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = SoftCharcoal,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = CharcoalMuted)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(attended, missed, cancelled, replaceExisting)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update Attendance")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceCounterRow(
    label: String,
    value: Int,
    color: Color,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IvorySurface)
            .border(1.dp, CharcoalMuted.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = SoftCharcoal
                )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onValueChange(max(0, value - 1)) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IvorySurfaceCard)
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease", tint = SoftCharcoal, modifier = Modifier.size(16.dp))
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                ),
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IvorySurfaceCard)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase", tint = SoftCharcoal, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SubjectHistoryDialog(
    subjectWithStats: SubjectWithStats,
    onUpdateRecord: (AttendanceRecord) -> Unit,
    onDeleteRecord: (AttendanceRecord) -> Unit,
    onAddManualRecord: (AttendanceStatus, Long, String) -> Unit,
    onOpenSetPastAttendance: () -> Unit,
    onDismiss: () -> Unit
) {
    val subject = subjectWithStats.subject
    val stats = subjectWithStats.stats
    val records = subjectWithStats.records

    var showAddManualEntry by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IvoryBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = "Attendance History (${stats.presentCount}/${stats.heldCount} • ${String.format(Locale.US, "%.1f", stats.percentage)}%)",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = CharcoalMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons: Log Single Class + Bulk Set Past Attendance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddManualEntry = !showAddManualEntry },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Log Class",
                            modifier = Modifier.size(16.dp),
                            tint = SageGreenDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showAddManualEntry) "Hide" else "Log Class",
                            color = SageGreenDark,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = onOpenSetPastAttendance,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenContainer),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = "Set Past Attendance",
                            modifier = Modifier.size(16.dp),
                            tint = SageGreenDark
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Set Past Attendance",
                            color = SageGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (showAddManualEntry) {
                    Spacer(modifier = Modifier.height(10.dp))
                    ManualLogSection(
                        onLog = { status, notes ->
                            onAddManualRecord(status, System.currentTimeMillis(), notes)
                            showAddManualEntry = false
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Past Recorded Classes (${records.size})",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = SoftCharcoal
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (records.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No classes logged yet. Use 'Set Past Attendance' to import past semester data.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(records, key = { it.id }) { record ->
                            RecordLogItem(
                                record = record,
                                formattedDate = dateFormat.format(Date(record.dateMillis)),
                                onToggleStatus = { nextStatus ->
                                    onUpdateRecord(record.copy(status = nextStatus))
                                },
                                onDelete = { onDeleteRecord(record) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManualLogSection(
    onLog: (AttendanceStatus, String) -> Unit
) {
    var status by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var notes by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = IvorySurfaceCard,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CharcoalMuted.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Select Status:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    AttendanceStatus.PRESENT to ("Present" to SageGreenContainer),
                    AttendanceStatus.ABSENT to ("Absent" to BelowTerracottaContainer),
                    AttendanceStatus.CANCELLED to ("Cancelled" to CautionMustardContainer)
                ).forEach { (st, pair) ->
                    val isSelected = status == st
                    FilterChip(
                        selected = isSelected,
                        onClick = { status = st },
                        label = { Text(pair.first, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = pair.second,
                            selectedLabelColor = SoftCharcoal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Note (optional, e.g. Lab 3)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onLog(status, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Log Entry")
            }
        }
    }
}

@Composable
private fun RecordLogItem(
    record: AttendanceRecord,
    formattedDate: String,
    onToggleStatus: (AttendanceStatus) -> Unit,
    onDelete: () -> Unit
) {
    val statusBg = when (record.status) {
        AttendanceStatus.PRESENT -> SageGreenContainer
        AttendanceStatus.ABSENT -> BelowTerracottaContainer
        AttendanceStatus.CANCELLED -> CautionMustardContainer
    }

    val statusFg = when (record.status) {
        AttendanceStatus.PRESENT -> SageGreenDark
        AttendanceStatus.ABSENT -> BelowTerracottaDark
        AttendanceStatus.CANCELLED -> CautionMustardDark
    }

    val nextStatus = when (record.status) {
        AttendanceStatus.PRESENT -> AttendanceStatus.ABSENT
        AttendanceStatus.ABSENT -> AttendanceStatus.CANCELLED
        AttendanceStatus.CANCELLED -> AttendanceStatus.PRESENT
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = IvorySurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CharcoalMuted.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = SoftCharcoal
                    )
                )
                if (record.notes.isNotBlank()) {
                    Text(
                        text = record.notes,
                        style = MaterialTheme.typography.labelSmall.copy(color = CharcoalMuted)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusBg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToggleStatus(nextStatus) }
                ) {
                    Text(
                        text = record.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete Record",
                        tint = CharcoalMuted.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Dialog for Exporting full data backup (JSON) and Restoring after reinstall or phone change.
 */
@Composable
fun BackupRestoreDialog(
    onExportJson: () -> String,
    onRestoreJson: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Export/Backup, 1=Restore/Import
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var restoreInputText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = IvoryBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Backup & Restore Data",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftCharcoal
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = CharcoalMuted)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .padding(horizontal = 24.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF81C784))
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { 
                            Text(
                                "Backup Data", 
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Color.White else Color(0xFF9CA3AF)
                            ) 
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { 
                            Text(
                                "Restore / Import", 
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) Color.White else Color(0xFF9CA3AF)
                            ) 
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    // Export Section
                    Text(
                        text = "Export all your subjects, attendance history, events, and exams into a secure backup JSON file. You can restore this anytime if you reinstall the app or switch devices.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftCharcoal, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val json = onExportJson()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Backup", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val json = onExportJson()
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_SUBJECT, "Saathi Planner Backup")
                                    putExtra(Intent.EXTRA_TEXT, json)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Backup JSON"))
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Outlined.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Restore Section
                    Text(
                        text = "Paste your backup JSON below to restore your subjects and attendance history:",
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftCharcoal, fontSize = 12.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = restoreInputText,
                        onValueChange = { restoreInputText = it },
                        label = { Text("Paste JSON backup here") },
                        placeholder = { Text("{\"version\": 1, \"subjects\": [...]}") },
                        maxLines = 5,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (restoreInputText.isNotBlank()) {
                                onRestoreJson(restoreInputText)
                            } else {
                                Toast.makeText(context, "Please paste JSON backup content", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Data")
                    }
                }
            }
        }
    }
}
