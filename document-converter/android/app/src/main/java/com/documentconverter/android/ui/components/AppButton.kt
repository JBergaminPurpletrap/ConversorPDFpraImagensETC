package com.documentconverter.android.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun AppPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    ElevatedButton(
        modifier = modifier.heightIn(min = AppDimens.ButtonMinHeight),
        enabled = enabled,
        colors = ButtonDefaults.elevatedButtonColors(
            containerColor = containerColor,
            contentColor = AppColors.ButtonText
        ),
        elevation = ButtonDefaults.elevatedButtonElevation(
            defaultElevation = AppDimens.CardElevation
        ),
        onClick = onClick
    ) {
        Text(text = text, color = AppColors.ButtonText)
    }
}

@Composable
fun AppSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        modifier = modifier.heightIn(min = AppDimens.SmallButtonMinHeight),
        enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = AppColors.ButtonText
        ),
        onClick = onClick
    ) {
        Text(text = text, color = AppColors.ButtonText)
    }
}

@Composable
fun AppFullWidthSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    AppSecondaryButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    )
}
