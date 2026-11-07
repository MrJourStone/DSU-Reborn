package vegabobo.dsusideloader.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// DSU-Reborn uses a single typographic family through the whole application.
// Keep FontFamily.SansSerif on every Material 3 text style so that headings,
// labels, cards, dialogs and the log console are never mixed with another font.
private val AppFontFamily = FontFamily.SansSerif

private fun TextStyle.withAppFont(): TextStyle = copy(fontFamily = AppFontFamily)

private val DefaultTypography = Typography()

val Typography = Typography(
    displayLarge = DefaultTypography.displayLarge.withAppFont(),
    displayMedium = DefaultTypography.displayMedium.withAppFont(),
    displaySmall = DefaultTypography.displaySmall.withAppFont(),
    headlineLarge = DefaultTypography.headlineLarge.withAppFont(),
    headlineMedium = DefaultTypography.headlineMedium.withAppFont(),
    headlineSmall = DefaultTypography.headlineSmall.withAppFont(),
    titleLarge = DefaultTypography.titleLarge.withAppFont(),
    titleMedium = DefaultTypography.titleMedium.withAppFont(),
    titleSmall = DefaultTypography.titleSmall.withAppFont(),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = DefaultTypography.bodyMedium.withAppFont(),
    bodySmall = DefaultTypography.bodySmall.withAppFont(),
    labelLarge = DefaultTypography.labelLarge.withAppFont(),
    labelMedium = DefaultTypography.labelMedium.withAppFont(),
    labelSmall = DefaultTypography.labelSmall.withAppFont(),
)
