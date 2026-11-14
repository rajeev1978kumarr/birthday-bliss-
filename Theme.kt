package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BerryRose,
    onPrimary = Color.White,
    primaryContainer = SoftBlush,
    onPrimaryContainer = DeepBerry,
    secondary = MarigoldGold,
    onSecondary = Color.White,
    secondaryContainer = WarmAmberContainer,
    onSecondaryContainer = DeepCocoa,
    tertiary = TealSpark,
    onTertiary = Color.White,
    tertiaryContainer = SoftMintContainer,
    onTertiaryContainer = Color(0xFF064E47),
    background = WarmIvory,
    onBackground = DeepCocoa,
    surface = WarmIvory,
    onSurface = DeepCocoa,
    surfaceVariant = WarmCreamSurface,
    onSurfaceVariant = MutedPlum,
    surfaceContainerLow = WarmCreamSurface,
    surfaceContainer = ElevatedPeach,
    outline = WarmOutline,
)

private val DarkColorScheme = darkColorScheme(
    primary = LuminousRose,
    onPrimary = Color(0xFF4F0021),
    primaryContainer = DarkBerryContainer,
    onPrimaryContainer = SoftBlush,
    secondary = ChampagneGold,
    onSecondary = Color(0xFF3E2900),
    secondaryContainer = DarkAmberContainer,
    onSecondaryContainer = Color(0xFFFFEAB3),
    tertiary = MintSpark,
    onTertiary = Color(0xFF003832),
    tertiaryContainer = DarkTealContainer,
    onTertiaryContainer = SoftMintContainer,
    background = VelvetPlum,
    onBackground = SoftIvoryText,
    surface = VelvetPlum,
    onSurface = SoftIvoryText,
    surfaceVariant = DarkPlumSurface,
    onSurfaceVariant = MutedRoseText,
    surfaceContainerLow = DarkPlumSurface,
    surfaceContainer = DarkPlumElevated,
    outline = DarkOutline,
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun BirthdayBlissTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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
        typography = AppTypography,
        shapes = AppShapes,
        content = content,
    )
}
