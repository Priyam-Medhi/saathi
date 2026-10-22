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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicEvent
import com.example.data.model.EventCategory
import com.example.data.model.EventRecurrence
import com.example.data.model.Exam
import com.example.ui.CalendarViewMode
import com.example.ui.components.AcademicEventCard
import com.example.ui.components.AddEditEventDialog
import com.example.ui.components.ExamCard
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.BelowTerracottaDark
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CautionMustardDark
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.MutedSkyBlue
import com.example.ui.theme.MutedSkyBlueContainer
import com.example.ui.theme.MutedSkyBlueDark
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SageGreenDark
import com.example.ui.theme.SoftCharcoal
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftLavenderContainer
import com.example.ui.theme.SoftLavenderDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    allEvents: List<AcademicEvent>,
    allExams: List<Exam>,
    onAddEvent: (title: String, category: EventCategory, startMillis: Long, endMillis: Long, location: String, description: String, recurrence: EventRecurrence, reminderMinutes: Int) -> Unit,
    onUpdateEvent: (AcademicEvent) -> Unit,
    onDeleteEvent: (AcademicEvent) -> Unit,
    onToggleTopic: (Exam, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var displayedCalendarMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showAddEventDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<AcademicEvent?>(null) }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val selectedDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    // Filter events and exams for selected date
    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val selYear = selCal.get(Calendar.YEAR)
    val selDayOfYear = selCal.get(Calendar.DAY_OF_YEAR)

    val eventsForSelectedDay = allEvents.filter { event ->
        val eCal = Calendar.getInstance().apply { timeInMillis = event.startMillis }
        eCal.get(Calendar.YEAR) == selYear && eCal.get(Calendar.DAY_OF_YEAR) == selDayOfYear
    }

    val examsForSelectedDay = allExams.filter { exam ->
        if (exam.isDateTbd || exam.dateTimeMillis == null) false
        else {
            val exCal = Calendar.getInstance().apply { timeInMillis = exam.dateTimeMillis }
            exCal.get(Calendar.YEAR) == selYear && exCal.get(Calendar.DAY_OF_YEAR) == selDayOfYear
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("calendar_screen"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & View Mode Switcher
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Events & Calendar",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = "All hackathons, sports, meets & exams in one place",
                            style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                        )
                    }
                }
            }

            // View Mode Tabs
            item {
                TabRow(
                    selectedTabIndex = viewMode.ordinal,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[viewMode.ordinal])
                                .padding(horizontal = 24.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF81C784))
                        )
                    }
                ) {
                    CalendarViewMode.values().forEach { mode ->
                        val isSelected = viewMode == mode
                        Tab(
                            selected = isSelected,
                            onClick = { viewMode = mode },
                            text = {
                                Text(
                                    mode.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else Color(0xFF9CA3AF)
                                    )
                                )
                            }
                        )
                    }
                }
            }

            // Month View Calendar Card
            if (viewMode == CalendarViewMode.MONTH) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Month Navigation Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    val newCal = displayedCalendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, -1)
                                    displayedCalendarMonth = newCal
                                }) {
                                    Icon(Icons.Outlined.ChevronLeft, contentDescription = "Prev Month", tint = SoftCharcoal)
                                }

                                Text(
                                    text = monthYearFormat.format(displayedCalendarMonth.time),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SoftCharcoal
                                    )
                                )

                                IconButton(onClick = {
                                    val newCal = displayedCalendarMonth.clone() as Calendar
                                    newCal.add(Calendar.MONTH, 1)
                                    displayedCalendarMonth = newCal
                                }) {
                                    Icon(Icons.Outlined.ChevronRight, contentDescription = "Next Month", tint = SoftCharcoal)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Day of week labels (M T W T F S S)
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { dayLabel ->
                                    Text(
                                        text = dayLabel,
                                        modifier = Modifier.weight(1f),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CharcoalMuted,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Calendar Days Grid
                            MonthCalendarGrid(
                                currentMonthCalendar = displayedCalendarMonth,
                                selectedDateMillis = selectedDateMillis,
                                allEvents = allEvents,
                                allExams = allExams,
                                onSelectDate = { dateMillis ->
                                    selectedDateMillis = dateMillis
                                }
                            )
                        }
                    }
                }
            } else if (viewMode == CalendarViewMode.WEEK) {
                item {
                    WeekStripView(
                        selectedDateMillis = selectedDateMillis,
                        allEvents = allEvents,
                        allExams = allExams,
                        onSelectDate = { selectedDateMillis = it }
                    )
                }
            }

            // Selected Day Header & Add Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (viewMode == CalendarViewMode.AGENDA) "All Upcoming Agenda" else "Schedule for ${selectedDateFormat.format(Date(selectedDateMillis))}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftCharcoal
                            )
                        )
                        Text(
                            text = "${eventsForSelectedDay.size} events • ${examsForSelectedDay.size} exams",
                            style = MaterialTheme.typography.labelSmall.copy(color = CharcoalMuted)
                        )
                    }
                }
            }

            // Events & Exams for selected day (or Agenda mode)
            if (viewMode == CalendarViewMode.AGENDA) {
                // Show all upcoming events + exams sorted by date
                val combinedAgenda = (
                    allEvents.map { AgendaItem.Event(it) } +
                    allExams.filter { !it.isDateTbd && it.dateTimeMillis != null }.map { AgendaItem.ExamItem(it) }
                ).sortedBy { it.timeMillis }

                if (combinedAgenda.isEmpty()) {
                    item {
                        AgendaEmptyState()
                    }
                } else {
                    items(combinedAgenda) { item ->
                        when (item) {
                            is AgendaItem.Event -> AcademicEventCard(
                                event = item.event,
                                onClick = { editingEvent = item.event }
                            )
                            is AgendaItem.ExamItem -> ExamCard(
                                exam = item.exam,
                                onToggleTopic = { topicId -> onToggleTopic(item.exam, topicId) },
                                onSetDate = {},
                                onEditExam = {},
                                onDeleteExam = {}
                            )
                        }
                    }
                }
            } else {
                // Day mode list
                if (eventsForSelectedDay.isEmpty() && examsForSelectedDay.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = IvorySurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No events or exams on this date. Tap '+' to schedule one.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
                                )
                            }
                        }
                    }
                } else {
                    // Show Exams first
                    items(examsForSelectedDay, key = { "exam_${it.id}" }) { exam ->
                        ExamCard(
                            exam = exam,
                            onToggleTopic = { topicId -> onToggleTopic(exam, topicId) },
                            onSetDate = {},
                            onEditExam = {},
                            onDeleteExam = {}
                        )
                    }
                    // Show Events
                    items(eventsForSelectedDay, key = { "event_${it.id}" }) { event ->
                        AcademicEventCard(
                            event = event,
                            onClick = { editingEvent = event }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }

        // Floating Action Button to Add Event
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("fab_add_event"),
            containerColor = SageGreenDark,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Add Event")
        }
    }

    // Add Event Dialog
    if (showAddEventDialog) {
        AddEditEventDialog(
            initialEvent = null,
            defaultDateMillis = selectedDateMillis,
            onDismiss = { showAddEventDialog = false },
            onSave = { title, category, start, end, location, description, recurrence, reminder ->
                onAddEvent(title, category, start, end, location, description, recurrence, reminder)
                showAddEventDialog = false
            }
        )
    }

    // Edit Event Dialog
    editingEvent?.let { eventToEdit ->
        AddEditEventDialog(
            initialEvent = eventToEdit,
            defaultDateMillis = eventToEdit.startMillis,
            onDismiss = { editingEvent = null },
            onSave = { title, category, start, end, location, description, recurrence, reminder ->
                onUpdateEvent(
                    eventToEdit.copy(
                        title = title,
                        category = category,
                        startMillis = start,
                        endMillis = end,
                        location = location,
                        description = description,
                        recurrence = recurrence,
                        reminderMinutes = reminder
                    )
                )
                editingEvent = null
            }
        )
    }
}

sealed class AgendaItem(val timeMillis: Long) {
    data class Event(val event: AcademicEvent) : AgendaItem(event.startMillis)
    data class ExamItem(val exam: Exam) : AgendaItem(exam.dateTimeMillis ?: 0L)
}

@Composable
private fun MonthCalendarGrid(
    currentMonthCalendar: Calendar,
    selectedDateMillis: Long,
    allEvents: List<AcademicEvent>,
    allExams: List<Exam>,
    onSelectDate: (Long) -> Unit
) {
    val cal = currentMonthCalendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    // 1=Sun, 2=Mon... Map to Mon=0 .. Sun=6
    val leadingEmptyCells = (firstDayOfWeek + 5) % 7
    val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selCal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val selYear = selCal.get(Calendar.YEAR)
    val selMonth = selCal.get(Calendar.MONTH)
    val selDay = selCal.get(Calendar.DAY_OF_MONTH)

    val currentYear = currentMonthCalendar.get(Calendar.YEAR)
    val currentMonth = currentMonthCalendar.get(Calendar.MONTH)

    val totalCells = leadingEmptyCells + maxDaysInMonth
    val rows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (c in 0..6) {
                    val cellIndex = r * 7 + c
                    val dayNumber = cellIndex - leadingEmptyCells + 1

                    if (dayNumber in 1..maxDaysInMonth) {
                        val isSelected = currentYear == selYear && currentMonth == selMonth && dayNumber == selDay

                        val cellCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                            set(Calendar.DAY_OF_MONTH, dayNumber)
                            set(Calendar.HOUR_OF_DAY, 12)
                        }
                        val cellMillis = cellCal.timeInMillis
                        val cellDayOfYear = cellCal.get(Calendar.DAY_OF_YEAR)

                        val hasEvents = allEvents.any {
                            val eCal = Calendar.getInstance().apply { timeInMillis = it.startMillis }
                            eCal.get(Calendar.YEAR) == currentYear && eCal.get(Calendar.DAY_OF_YEAR) == cellDayOfYear
                        }
                        val hasExams = allExams.any {
                            if (it.isDateTbd || it.dateTimeMillis == null) false
                            else {
                                val exCal = Calendar.getInstance().apply { timeInMillis = it.dateTimeMillis }
                                exCal.get(Calendar.YEAR) == currentYear && exCal.get(Calendar.DAY_OF_YEAR) == cellDayOfYear
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SageGreenContainer else Color.Transparent)
                                .clickable { onSelectDate(cellMillis) }
                                .then(
                                    if (isSelected) Modifier.border(1.5.dp, SageGreenDark, RoundedCornerShape(10.dp))
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) SageGreenDark else SoftCharcoal
                                    )
                                )
                                // Indicator Dots
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasEvents) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(MutedSkyBlue)
                                        )
                                    }
                                    if (hasExams) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(CautionMustard)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Empty spacer cell
                        Spacer(modifier = Modifier.weight(1f).height(38.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekStripView(
    selectedDateMillis: Long,
    allEvents: List<AcademicEvent>,
    allExams: List<Exam>,
    onSelectDate: (Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val startOffset = (dayOfWeek + 5) % 7 // Monday offset

    val weekCal = cal.clone() as Calendar
    weekCal.add(Calendar.DAY_OF_YEAR, -startOffset)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0..6) {
                val dayCal = weekCal.clone() as Calendar
                dayCal.add(Calendar.DAY_OF_YEAR, i)
                val dayMillis = dayCal.timeInMillis
                val isSelected = dayCal.get(Calendar.DAY_OF_YEAR) == cal.get(Calendar.DAY_OF_YEAR) &&
                                 dayCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
                val dayLabel = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
                val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SageGreenContainer else Color.Transparent)
                        .clickable { onSelectDate(dayMillis) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = dayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isSelected) SageGreenDark else CharcoalMuted,
                                fontSize = 10.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayNum.toString(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SageGreenDark else SoftCharcoal
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgendaEmptyState() {
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
                text = "No upcoming items in your academic schedule.",
                style = MaterialTheme.typography.bodyMedium.copy(color = CharcoalMuted)
            )
        }
    }
}
