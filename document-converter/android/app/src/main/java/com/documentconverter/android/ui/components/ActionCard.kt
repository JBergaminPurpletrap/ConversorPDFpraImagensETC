package com.documentconverter.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.PaddingValues
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun ActionCard(
    title: String,
    description: String,
    buttonText: String,
    buttonContainerColor: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    AppElevatedCard(
        contentPadding = PaddingValues(AppDimens.ActionCardPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
            AppPrimaryButton(
                text = buttonText,
                onClick = onClick,
                containerColor = buttonContainerColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
