package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.ui.theme.BelowTerracotta
import com.example.ui.theme.CautionMustard
import com.example.ui.theme.SageGreen
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

data class AttendanceSummary(
    val presentCount: Int,
    val absentCount: Int,
    val cancelledCount: Int,
    val heldCount: Int,
    val percentage: Double,
    val targetPercentage: Double,
    val statusTier: AttendanceTier,
    val statusMessage: String,
    val classesSafeToMiss: Int,
    val classesNeededToAttend: Int
)

enum class AttendanceTier(val label: String, val color: Color) {
    COMFORTABLE("Comfortable Buffer", SageGreen),
    SAFE_LINE("On The Line", CautionMustard),
    BELOW_TARGET("Needs Attention", BelowTerracotta)
}

object AttendanceCalc {

    fun calculate(
        records: List<AttendanceRecord>,
        targetPercentage: Double = 75.0
    ): AttendanceSummary {
        var present = 0
        var absent = 0
        var cancelled = 0

        for (record in records) {
            when (record.status) {
                AttendanceStatus.PRESENT -> present++
                AttendanceStatus.ABSENT -> absent++
                AttendanceStatus.CANCELLED -> cancelled++ // excluded from held classes
            }
        }

        val held = present + absent
        val percentage = if (held > 0) {
            (present.toDouble() / held.toDouble()) * 100.0
        } else {
            100.0 // Default full score when no classes have been held yet
        }

        val tier = when {
            percentage >= 78.0 -> AttendanceTier.COMFORTABLE
            percentage >= 75.0 -> AttendanceTier.SAFE_LINE
            else -> AttendanceTier.BELOW_TARGET
        }

        var safeToMiss = 0
        var neededToAttend = 0
        val message: String

        if (held == 0) {
            message = "No classes held yet. 100% standing."
            safeToMiss = 0
            neededToAttend = 0
        } else if (percentage >= targetPercentage) {
            // Formula: (P * 100 - target * H) / target
            val numerator = (100.0 * present) - (targetPercentage * held)
            val missCount = floor(numerator / targetPercentage).toInt()
            safeToMiss = max(0, missCount)

            message = if (safeToMiss > 0) {
                "You can miss next $safeToMiss class${if (safeToMiss > 1) "es" else ""} and stay safe."
            } else {
                "On track! Attend the next class to maintain target."
            }
        } else {
            // Formula: (target * H - 100 * P) / (100 - target)
            val denominator = 100.0 - targetPercentage
            neededToAttend = if (denominator > 0) {
                val num = (targetPercentage * held) - (100.0 * present)
                max(1, ceil(num / denominator).toInt())
            } else {
                1
            }

            message = "Attend next $neededToAttend class${if (neededToAttend > 1) "es" else ""} to get back on track (${targetPercentage.toInt()}%)."
        }

        return AttendanceSummary(
            presentCount = present,
            absentCount = absent,
            cancelledCount = cancelled,
            heldCount = held,
            percentage = percentage,
            targetPercentage = targetPercentage,
            statusTier = tier,
            statusMessage = message,
            classesSafeToMiss = safeToMiss,
            classesNeededToAttend = neededToAttend
        )
    }
}
