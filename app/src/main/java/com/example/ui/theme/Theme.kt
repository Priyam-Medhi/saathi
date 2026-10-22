package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SageGreenDark,
    onPrimary = Color.White,
    primaryContainer = SageGreenContainer,
    onPrimaryContainer = SageGreenDark,

    secondary = MutedSkyBlueDark,
    onSecondary = Color.White,
    secondaryContainer = MutedSkyBlueContainer,
    onSecondaryContainer = MutedSkyBlueDark,

    tertiary = SoftLavenderDark,
    onTertiary = Color.White,
    tertiaryContainer = SoftLavenderContainer,
    onTertiaryContainer = SoftLavenderDark,

    background = IvoryBackground,
    onBackground = SoftCharcoal,

    surface = IvorySurface,
    onSurface = SoftCharcoal,
    surfaceVariant = IvorySurfaceVariant,
    onSurfaceVariant = CharcoalMuted,

    error = BelowTerracottaDark,
    onError = Color.White,
    errorContainer = BelowTerracottaContainer,
    onErrorContainer = BelowTerracottaDark,

    outline = CharcoalSubtle.copy(alpha = 0.5f),
    outlineVariant = CharcoalSubtle.copy(alpha = 0.25f)
)

private val DarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = DarkSlateBackground,
    primaryContainer = DarkSlateSurfaceVariant,
    onPrimaryContainer = SageGreen,

    secondary = MutedSkyBlue,
    onSecondary = DarkSlateBackground,
    secondaryContainer = DarkSlateSurfaceVariant,
    onSecondaryContainer = MutedSkyBlue,

    tertiary = SoftLavender,
    onTertiary = DarkSlateBackground,
    tertiaryContainer = DarkSlateSurfaceVariant,
    onTertiaryContainer = SoftLavender,

    background = DarkSlateBackground,
    onBackground = DarkTextPrimary,

    surface = DarkSlateSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,

    error = BelowTerracotta,
    onError = DarkSlateBackground,
    errorContainer = DarkSlateSurfaceVariant,
    onErrorContainer = BelowTerracotta,

    outline = DarkTextSubtle.copy(alpha = 0.5f),
    outlineVariant = DarkTextSubtle.copy(alpha = 0.25f)
)

@Composable
fun SaathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Default false to prioritize research-backed calm palette
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
