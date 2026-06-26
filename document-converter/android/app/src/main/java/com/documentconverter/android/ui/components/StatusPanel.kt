package com.documentconverter.android.ui.components

import com.documentconverter.android.ui.state.ConversionState
import com.documentconverter.android.ui.state.ConversionStatus
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.ui.theme.AppColors

@Composable
fun StatusPanel(state: ConversionState) {
    val accent = when (state.status) {
        ConversionStatus.Idle -> MaterialTheme.colorScheme.secondary
        ConversionStatus.Running -> MaterialTheme.colorScheme.primary
        ConversionStatus.Success -> AppColors.Success
        ConversionStatus.Error -> MaterialTheme.colorScheme.error
    }
    AppOutlinedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
        ) {
            Text(
                text = when (state.status) {
                    ConversionStatus.Idle -> "Status"
                    ConversionStatus.Running -> "Convertendo"
                    ConversionStatus.Success -> "Concluido"
                    ConversionStatus.Error -> "Atencao"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Text(text = state.message, style = MaterialTheme.typography.bodyMedium)
            if (state.progressText.isNotBlank()) {
                Text(text = state.progressText, style = MaterialTheme.typography.bodySmall)
            }
            if (state.isRunning) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            if (state.elapsedMs != null) {
                Text(text = "Tempo: ${state.elapsedMs} ms", style = MaterialTheme.typography.bodySmall)
            }
            if (state.details.isNotBlank()) {
                Text(text = state.details, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
