package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimary,
    secondary = EmeraldSecondary,
    tertiary = EmeraldTertiary,
    background = EmeraldBackground,
    surface = EmeraldSurface,
    surfaceVariant = EmeraldSurfaceVariant,
    onPrimary = OnEmeraldPrimary,
    onBackground = OnEmeraldBackground,
    onSurface = OnEmeraldSurface,
    onSurfaceVariant = OnEmeraldSurfaceVariant
  )

private val LightColorScheme = DarkColorScheme // Keep it consistent as dark mode only

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true for dark emerald theme
  dynamicColor: Boolean = false, // Disable dynamic colors to keep modern emerald look
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
