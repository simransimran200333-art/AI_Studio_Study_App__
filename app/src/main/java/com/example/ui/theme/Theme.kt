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
    primary = PrimaryMinimalDark,
    secondary = SecondaryMinimalDark,
    tertiary = TertiaryMinimalDark,
    error = ErrorMinimalDark,
    errorContainer = ErrorContainerMinimalDark,
    primaryContainer = PrimaryContainerMinimalDark,
    onPrimaryContainer = OnPrimaryContainerMinimalDark,
    secondaryContainer = SecondaryContainerMinimalDark,
    tertiaryContainer = TertiaryContainerMinimalDark,
    background = MinimalDarkBg,
    surface = MinimalDarkSurface,
    onPrimary = MinimalDarkBg,
    onSecondary = MinimalDarkBg,
    onTertiary = MinimalDarkBg,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = MinimalDarkSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryMinimal,
    secondary = SecondaryMinimal,
    tertiary = TertiaryMinimal,
    error = ErrorMinimal,
    errorContainer = ErrorContainerMinimal,
    primaryContainer = PrimaryContainerMinimal,
    onPrimaryContainer = OnPrimaryContainerMinimal,
    secondaryContainer = SecondaryContainerMinimal,
    tertiaryContainer = TertiaryContainerMinimal,
    background = MinimalLightBg,
    surface = MinimalLightSurface,
    onPrimary = MinimalLightSurface,
    onSecondary = MinimalLightSurface,
    onTertiary = MinimalLightSurface,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = MinimalLightSurface,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Force customized color scheme to preserve premium visual brand in screenshot testing and live emulator
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
