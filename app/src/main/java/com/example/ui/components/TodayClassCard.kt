package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.BelowTerracottaContainer
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.CautionMustardContainer
import com.example.ui.theme.CharcoalMuted
import com.example.ui.theme.IvorySurfaceCard
import com.example.ui.theme.SageGreen
import com.example.ui.theme.SageGreenContainer
import com.example.ui.theme.SoftCharcoal

@Composable
fun TodayClassCard(
    subject: Subject,
    currentStatus: AttendanceStatus?,
    onMarkStatus: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val subjectColor = runCatching { Color(android.graphics.Color.parseColor(subject.colorHex)) }
        .getOrDefault(SageGreen)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("today_class_card_${subject.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = IvorySurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Subject Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SoftCharcoal
                        ),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subject.code,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CharcoalMuted
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quick Mark Action Pills: Present, Absent, Cancelled
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Present Button
                StatusPillButton(
                    label = "Present",
                    icon = Icons.Outlined.Check,
                    isActive = currentStatus == AttendanceStatus.PRESENT,
                    activeBg = SageGreenContainer,
                    activeFg = SageGreen,
                    onClick = { onMarkStatus(AttendanceStatus.PRESENT) },
                    testTag = "btn_present_${subject.id}"
                )

                // Absent Button
                StatusPillButton(
                    label = "Absent",
                    icon = Icons.Outlined.Close,
                    isActive = currentStatus == AttendanceStatus.ABSENT,
                    activeBg = BelowTerracottaContainer,
                    activeFg = BelowTerracotta,
                    onClick = { onMarkStatus(AttendanceStatus.ABSENT) },
                    testTag = "btn_absent_${subject.id}"
                )

                // Cancelled Button
                StatusPillButton(
                    label = "Off",
                    icon = Icons.Outlined.Block,
                    isActive = currentStatus == AttendanceStatus.CANCELLED,
                    activeBg = CautionMustardContainer,
                    activeFg = CautionMustard,
                    onClick = { onMarkStatus(AttendanceStatus.CANCELLED) },
                    testTag = "btn_cancel_${subject.id}"
                )
            }
        }
    }
}

@Composable
private fun StatusPillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeBg: Color,
    activeFg: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val bgColor by animateColorAsState(
        targetValue = if (isActive) activeBg else Color.Transparent,
        label = "pillBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isActive) activeFg else CharcoalMuted,
        label = "pillFg"
    )

    Surface(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .then(
                if (isActive) Modifier.border(1.dp, activeFg.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, CharcoalMuted.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            ),
        shape = RoundedCornerShape(20.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 11.sp,
                    color = contentColor
                )
            )
        }
    }
}
