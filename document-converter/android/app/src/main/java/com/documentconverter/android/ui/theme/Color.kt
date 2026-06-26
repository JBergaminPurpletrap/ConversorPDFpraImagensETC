package com.documentconverter.android.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object AppColors {
    val DeepForest = Color(0xFF123528)
    val ForestPanel = Color(0xFF1D4636)
    val ForestPanelSoft = Color(0xFF2A5745)
    val Moss = Color(0xFFDCE8C2)
    val Olive = Color(0xFFC5D7A1)
    val ForestSoft = Color(0xFFB7CFA4)
    val NaturalBackground = DeepForest
    val NaturalSurface = ForestPanel
    val WarmWhite = Color(0xFFF7F4E8)
    val TextPrimary = Color(0xFFF7F4E8)
    val TextSecondary = Color(0xFFC9D8C2)
    val ButtonText = Color.Black
    val Success = Color(0xFFBDE5A8)
    val Warning = Color(0xFFE5C06B)
    val Error = Color(0xFFE3A096)
}

val AppColorScheme = lightColorScheme(
    primary = AppColors.Moss,
    onPrimary = AppColors.ButtonText,
    primaryContainer = AppColors.Olive,
    onPrimaryContainer = AppColors.ButtonText,
    secondary = AppColors.ForestSoft,
    onSecondary = AppColors.ButtonText,
    secondaryContainer = Color(0xFFE4EED4),
    onSecondaryContainer = AppColors.ButtonText,
    tertiary = AppColors.Warning,
    onTertiary = AppColors.ButtonText,
    background = AppColors.NaturalBackground,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.NaturalSurface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.ForestPanelSoft,
    onSurfaceVariant = AppColors.TextSecondary,
    error = AppColors.Error,
    onError = AppColors.ButtonText,
    outline = Color(0xFF6E8B78)
)
