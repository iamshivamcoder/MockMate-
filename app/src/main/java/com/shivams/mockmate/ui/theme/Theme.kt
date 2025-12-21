package com.shivams.mockmate.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDarkMode,
    secondary = SecondaryDarkMode,
    tertiary = PrimaryDarkDarkMode,
    background = BackgroundDarkMode,
    surface = SurfaceDarkMode,
    error = ErrorDarkMode,
    onPrimary = OnPrimaryDarkMode,
    onSecondary = OnSecondaryDarkMode,
    onBackground = OnBackgroundDarkMode,
    onSurface = OnSurfaceDarkMode,
    onError = OnErrorDarkMode
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = PrimaryDark,
    background = Background,
    surface = Surface,
    error = Error,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onError = OnError
    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Immutable
data class ExtendedColorScheme(
    val mockTestColor: Color,
    val onMockTestColor: Color,
    val paragraphAnalysisColor: Color,
    val onParagraphAnalysisColor: Color,
    val difficultyEasyColor: Color,
    val onDifficultyEasyColor: Color,
    val difficultyMediumColor: Color,
    val onDifficultyMediumColor: Color,
    val difficultyHardColor: Color,
    val onDifficultyHardColor: Color
)

val LocalExtendedColorScheme = staticCompositionLocalOf {
    ExtendedColorScheme(
        mockTestColor = Color.Unspecified,
        onMockTestColor = Color.Unspecified,
        paragraphAnalysisColor = Color.Unspecified,
        onParagraphAnalysisColor = Color.Unspecified,
        difficultyEasyColor = Color.Unspecified,
        onDifficultyEasyColor = Color.Unspecified,
        difficultyMediumColor = Color.Unspecified,
        onDifficultyMediumColor = Color.Unspecified,
        difficultyHardColor = Color.Unspecified,
        onDifficultyHardColor = Color.Unspecified
    )
}

@Composable
fun MockMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Dynamic color is available on Android 12+
    content: @Composable () -> Unit
) {
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val extendedColorScheme = if (darkTheme) {
        ExtendedColorScheme(
            mockTestColor = MockTestDarkColor,
            onMockTestColor = OnPrimaryDarkMode, // Assuming black text on this color for dark theme
            paragraphAnalysisColor = ParagraphAnalysisDarkColor,
            onParagraphAnalysisColor = OnPrimaryDarkMode, // Assuming black text
            difficultyEasyColor = DifficultyEasyDarkColor,
            onDifficultyEasyColor = OnSecondaryDarkMode, // Assuming black text
            difficultyMediumColor = DifficultyMediumDarkColor,
            onDifficultyMediumColor = OnBackgroundDarkMode, // Assuming white text
            difficultyHardColor = DifficultyHardDarkColor,
            onDifficultyHardColor = OnBackgroundDarkMode // Assuming white text
        )
    } else {
        ExtendedColorScheme(
            mockTestColor = MockTestColor,
            onMockTestColor = OnPrimary, // Assuming white text on this color for light theme
            paragraphAnalysisColor = ParagraphAnalysisColor,
            onParagraphAnalysisColor = OnPrimary, // Assuming white text
            difficultyEasyColor = DifficultyEasyColor,
            onDifficultyEasyColor = OnSecondary, // Assuming black text
            difficultyMediumColor = DifficultyMediumColor,
            onDifficultyMediumColor = OnBackground, // Assuming black text
            difficultyHardColor = DifficultyHardColor,
            onDifficultyHardColor = OnBackground // Assuming black text
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = baseColorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = baseColorScheme,
        typography = Typography,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            LocalExtendedColorScheme provides extendedColorScheme
        ) {
            content()
        }
    }
}

// Extension property to easily access the extended colors
val MaterialTheme.extendedColorScheme: ExtendedColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColorScheme.current
