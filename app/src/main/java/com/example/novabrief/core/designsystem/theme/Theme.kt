package com.example.novabrief.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlueDark,
    secondary = AccentOrange,
    tertiary = AccentTeal,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceContainer = DarkSurfaceContainer,
    onSurface = DarkOnSurface,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outlineVariant = DarkOutlineVariant
)


private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    secondary = AccentOrange,
    tertiary = AccentTeal,
    background = LightBackground,
    surface = LightSurface,
    surfaceContainer = LightSurfaceContainer,
    onSurface = LightOnSurface,
    onSurfaceVariant = LightOnSurfaceVariant,
    outlineVariant = LightOutlineVariant
)

@Composable
fun NovaBriefTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    MaterialTheme(
        colorScheme = if(darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
