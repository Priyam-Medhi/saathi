package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Repeat
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
import com.example.data.model.AcademicEvent
import com.example.data.model.EventRecurrence
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.MutedSkyBlue
import com.example.ui.theme.SoftCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AcademicEventCard(
    event: AcademicEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = runCatching { Color(android.graphics.Color.parseColor(event.category.colorHex)) }
        .getOrDefault(MutedSkyBlue)

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val dateStr = dateFormat.format(Date(event.startMillis))
    val timeStr = "${timeFormat.format(Date(event.startMillis))} - ${timeFormat.format(Date(event.endMillis))}"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category Tag + Recurrence Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.category.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (event.recurrence != EventRecurrence.NONE) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Repeat,
                                contentDescription = "Repeats",
                                modifier = Modifier.size(11.dp),
                                tint = SoftCharcoal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.recurrence.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.5.sp,
                                    color = SoftCharcoal
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Title
            Text(
                text = event.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftCharcoal
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date and Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.AccessTime,
                    contentDescription = "Time",
                    tint = CharcoalMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$dateStr • $timeStr",
                    style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                )
            }

            // Location if present
            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = CharcoalMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodySmall.copy(color = CharcoalMuted)
                    )
                }
            }

            // Description preview if present
            if (event.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = SoftCharcoal.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    ),
                    maxLines = 2
                )
            }
        }
    }
}
