package com.example.gymtracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppTheme(val label: String) {
    SYSTEM("System"),

    ENERGY_ORANGE_DARK("Energy Orange (Dark)"),
    ENERGY_ORANGE_LIGHT("Energy Orange (Light)"),

    ELECTRIC_BLUE_DARK("Electric Blue (Dark)"),
    ELECTRIC_BLUE_LIGHT("Electric Blue (Light)"),

    NEON_LIME_DARK("Neon Lime (Dark)"),
    NEON_LIME_LIGHT("Neon Lime (Light)")
}


// --- Clean Light ---
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary,

    background = Bg,
    surface = Surface0,
    surfaceVariant = Surface1,

    onPrimary = Color(0xFF0B0F14),
    onSecondary = Color(0xFF0B0F14),
    onTertiary = Color(0xFF0B0F14),

    onBackground = OnBg,
    onSurface = OnSurface,
    onSurfaceVariant = OnSurfaceVariant,

    outline = Outline,
    error = Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    // Simple Light fallback (optional) – du kannst später ein richtiges Light Theme bauen
    primary = Primary,
    secondary = Secondary,
    tertiary = Tertiary
)

private val EnergyOrangeDark = DarkColorScheme // dein aktuelles DarkColorScheme

private val ElectricBlueDark = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFFF97316),
    tertiary = Color(0xFF7C5CFF),

    background = Color(0xFF060B12),
    surface = Color(0xFF0E1624),
    surfaceVariant = Color(0xFF142033),

    onPrimary = Color(0xFF060B12),
    onSecondary = Color(0xFF060B12),
    onTertiary = Color(0xFF060B12),

    onBackground = Color(0xFFE6F0FF),
    onSurface = Color(0xFFE6F0FF),
    onSurfaceVariant = Color(0xFFA5B4CC),

    outline = Color(0xFF23324A),
    error = Error,
    onError = Color.White
)

private val NeonLimeDark = darkColorScheme(
    primary = Color(0xFFA3FF12),
    secondary = Color(0xFF7C5CFF),
    tertiary = Color(0xFF2DD4FF),

    background = Color(0xFF070A0F),
    surface = Color(0xFF101827),
    surfaceVariant = Color(0xFF162236),

    onPrimary = Color(0xFF070A0F),
    onSecondary = Color(0xFF070A0F),
    onTertiary = Color(0xFF070A0F),

    onBackground = Color(0xFFEAF2FF),
    onSurface = Color(0xFFEAF2FF),
    onSurfaceVariant = Color(0xFFA9B6C8),

    outline = Color(0xFF24324A),
    error = Error,
    onError = Color.White
)
private val EnergyOrangeLight = lightColorScheme(
    primary = Color(0xFFFF6B2D),
    secondary = Color(0xFF2DD4FF),
    tertiary = Color(0xFF7C5CFF),

    background = Color(0xFFFFF7F2),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFFFE6DA),

    onPrimary = Color.White,
    onSecondary = Color(0xFF001018),
    onTertiary = Color.White,

    onBackground = Color(0xFF121212),
    onSurface = Color(0xFF121212),
    onSurfaceVariant = Color(0xFF3A2A22),

    outline = Color(0xFFE6C7B6),
    error = Error,
    onError = Color.White
)
private val ElectricBlueLight = lightColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFFF97316),
    tertiary = Color(0xFF7C5CFF),

    background = Color(0xFFF4F8FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6F0FF),

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color(0xFF0B1220),
    onSurface = Color(0xFF0B1220),
    onSurfaceVariant = Color(0xFF1F2A44),

    outline = Color(0xFFC9D9F5),
    error = Error,
    onError = Color.White
)
private val NeonLimeLight = lightColorScheme(
    primary = Color(0xFFA3FF12),
    secondary = Color(0xFF7C5CFF),
    tertiary = Color(0xFF2DD4FF),

    background = Color(0xFFF7FFEF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8FFD0),

    onPrimary = Color(0xFF0B0F14),
    onSecondary = Color.White,
    onTertiary = Color(0xFF001018),

    onBackground = Color(0xFF0B0F14),
    onSurface = Color(0xFF0B0F14),
    onSurfaceVariant = Color(0xFF203010),

    outline = Color(0xFFCCE6A6),
    error = Error,
    onError = Color.White
)

@Composable
fun GymTrackerTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()

    val colorScheme = when (appTheme) {
        AppTheme.SYSTEM -> if (systemDark) EnergyOrangeDark else EnergyOrangeLight

        AppTheme.ENERGY_ORANGE_DARK -> EnergyOrangeDark
        AppTheme.ENERGY_ORANGE_LIGHT -> EnergyOrangeLight

        AppTheme.ELECTRIC_BLUE_DARK -> ElectricBlueDark
        AppTheme.ELECTRIC_BLUE_LIGHT -> ElectricBlueLight

        AppTheme.NEON_LIME_DARK -> NeonLimeDark
        AppTheme.NEON_LIME_LIGHT -> NeonLimeLight
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

