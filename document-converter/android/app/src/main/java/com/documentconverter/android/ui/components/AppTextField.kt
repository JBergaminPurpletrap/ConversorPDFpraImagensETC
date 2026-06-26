package com.documentconverter.android.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppTypography

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        label = { Text(text = label) },
        textStyle = AppTypography.bodyMedium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            focusedLabelColor = AppColors.Moss,
            unfocusedLabelColor = AppColors.TextSecondary,
            focusedBorderColor = AppColors.Moss,
            unfocusedBorderColor = AppColors.ForestSoft,
            cursorColor = AppColors.Moss
        )
    )
}
