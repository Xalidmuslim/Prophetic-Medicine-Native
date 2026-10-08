package com.xalid.meditsinaproroka.nativeapp

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF86AB94),
    onPrimary = Color(0xFF152019),
    primaryContainer = Color(0xFF29362E),
    onPrimaryContainer = Color(0xFFEDE7DC),
    background = Color(0xFF191D1A),
    onBackground = Color(0xFFEDE7DC),
    surface = Color(0xFF202622),
    onSurface = Color(0xFFEDE7DC),
    surfaceVariant = Color(0xFF292F2A),
    onSurfaceVariant = Color(0xFFF3F3F3),
    outline = Color(0xFF343B35),
    outlineVariant = Color(0xFF343B35),
    secondary = Color(0xFFBDA276),
    onSecondary = Color(0xFF191D1A),
    error = Color(0xFFD08377),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2F6A52),
    onPrimary = Color(0xFFFBF8F2),
    primaryContainer = Color(0xFFE6EDE6),
    onPrimaryContainer = Color(0xFF214B3D),
    background = Color(0xFFF6F1E8),
    onBackground = Color(0xFF1F1C19),
    surface = Color(0xFFFCF9F3),
    onSurface = Color(0xFF1F1C19),
    surfaceVariant = Color(0xFFF2EAE0),
    onSurfaceVariant = Color(0xFF6E6A63),
    outline = Color(0xFFE3D7C7),
    outlineVariant = Color(0xFFE3D7C7),
    secondary = Color(0xFFC8A46A),
    onSecondary = Color(0xFF1F1C19),
    error = Color(0xFF9B4940),
)

private val WebTypography = Typography(
    displayLarge = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 44.sp),
    displayMedium = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 34.sp, lineHeight = 38.sp),
    displaySmall = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 29.sp, lineHeight = 33.sp),
    headlineLarge = TextStyle(fontFamily = WebLiterataFont, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 32.sp),
    headlineMedium = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 24.sp),
    titleLarge = TextStyle(fontFamily = WebModernFont, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, lineHeight = 23.sp),
    titleMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
    bodyLarge = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 25.sp),
    bodyMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = WebSansFont, fontWeight = FontWeight.SemiBold, fontSize = 10.5.sp, lineHeight = 13.sp),
)

private val WebShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(19.dp),
    extraLarge = RoundedCornerShape(22.dp),
)

@Composable
fun MedicinaTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
    val activity = LocalContext.current as? Activity
    SideEffect {
        activity?.let { host ->
            // System icons must contrast with the actual Compose theme, including
            // after toggling dark mode from the reader settings.
            val controls = WindowCompat.getInsetsController(host.window, host.window.decorView)
            controls.isAppearanceLightStatusBars = !dark
            controls.isAppearanceLightNavigationBars = !dark
            @Suppress("DEPRECATION")
            host.window.statusBarColor = if (dark) AndroidColor.rgb(25, 29, 26)
                else AndroidColor.rgb(247, 243, 235)
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkScheme else LightScheme,
        typography = WebTypography,
        shapes = WebShapes,
        content = content,
    )
}
