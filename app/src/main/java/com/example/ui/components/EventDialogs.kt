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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.data.model.AcademicEvent
import com.example.data.model.EventCategory
import com.example.data.model.EventRecurrence
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditEventDialog(
    initialEvent: AcademicEvent? = null,
    defaultDateMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: EventCategory,
        startMillis: Long,
        endMillis: Long,
        location: String,
        description: String,
        recurrence: EventRecurrence,
        reminderMinutes: Int
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialEvent?.title ?: "") }
    var category by remember { mutableStateOf(initialEvent?.category ?: EventCategory.ACADEMIC) }
    var location by remember { mutableStateOf(initialEvent?.location ?: "") }
    var description by remember { mutableStateOf(initialEvent?.description ?: "") }
    var recurrence by remember { mutableStateOf(initialEvent?.recurrence ?: EventRecurrence.NONE) }
    var reminderMinutes by remember { mutableIntStateOf(initialEvent?.reminderMinutes ?: 60) }

    // Start & End time setup
    var startMillis by remember {
        mutableLongStateOf(initialEvent?.startMillis ?: defaultDateMillis)
    }
    var endMillis by remember {
        mutableLongStateOf(initialEvent?.endMillis ?: (defaultDateMillis + 3600000L * 2))
    }

    var hasError by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialEvent == null) "Add College Event" else "Edit Event",
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
                        value = title,
                        onValueChange = {
                            title = it
                            hasError = false
                        },
                        label = { Text("Event Title (e.g. Hackathon, Football Trial)") },
                        singleLine = true,
                        isError = hasError && title.isBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_event_title")
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EventCategory.values().forEach { cat ->
                                val isSelected = category == cat
                                val catColor = Color(android.graphics.Color.parseColor(cat.colorHex))
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { category = cat },
                                    label = { Text(cat.label, fontSize = 11.5.sp) },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = catColor.copy(alpha = 0.2f),
                                        selectedLabelColor = SoftCharcoal
                                    )
                                )
                            }
                        }
                    }
                }

                // Date Time adjustments (Quick +/- 1 day or hour shifts)
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SageGreenContainer.copy(alpha = 0.4f),
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
                                    text = dateFormat.format(Date(startMillis)),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuickTimeShiftChip(label = "Today", onClick = {
                                    val now = System.currentTimeMillis()
                                    startMillis = now
                                    endMillis = now + 3600000L * 2
                                })
                                QuickTimeShiftChip(label = "+1 Day", onClick = {
                                    startMillis += 86400000L
                                    endMillis += 86400000L
                                })
                                QuickTimeShiftChip(label = "+1 Week", onClick = {
                                    startMillis += 86400000L * 7
                                    endMillis += 86400000L * 7
                                })
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Outlined.AccessTime,
                                    contentDescription = "Time",
                                    modifier = Modifier.size(15.dp),
                                    tint = SageGreenDark
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${timeFormat.format(Date(startMillis))} – ${timeFormat.format(Date(endMillis))}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = SoftCharcoal)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. Auditorium, Lab 2)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_event_location")
                    )
                }

                item {
                    Column {
                        Text(
                            text = "Recurrence",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            EventRecurrence.values().forEach { rec ->
                                val isSelected = recurrence == rec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { recurrence = rec },
                                    label = { Text(rec.label, fontSize = 11.sp) },
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
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Notes (optional)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        hasError = true
                    } else {
                        onSave(title, category, startMillis, endMillis, location, description, recurrence, reminderMinutes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_save_event")
            ) {
                Text("Save Event")
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
private fun QuickTimeShiftChip(
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
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
