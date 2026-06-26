package com.documentconverter.android.ui.screens.result

import com.documentconverter.android.ui.theme.AppColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.documentconverter.android.ui.components.AppFullWidthSecondaryButton
import com.documentconverter.android.ui.components.AppOutlinedCard
import com.documentconverter.android.ui.components.AppPrimaryButton
import com.documentconverter.android.ui.model.CompletedConversion
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun CompletionPanel(
    completedConversion: CompletedConversion?,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onBack: () -> Unit
) {
    if (completedConversion == null) {
        return
    }

    AppOutlinedCard {
        val hasMultipleOutputs = completedConversion.outputUris.size > 1
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            Text(
                text = "\u2713 Conversao concluida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.Success
            )
            Text("Arquivo salvo com sucesso.")
            Text(completedConversion.message, style = MaterialTheme.typography.bodySmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
            ) {
                AppPrimaryButton(
                    text = if (hasMultipleOutputs) "Abrir primeira" else "Abrir",
                    onClick = onOpen,
                    containerColor = AppColors.Moss,
                    modifier = Modifier.weight(1f)
                )
                AppPrimaryButton(
                    text = "Compartilhar",
                    onClick = onShare,
                    containerColor = AppColors.ForestSoft,
                    modifier = Modifier.weight(1f)
                )
            }
            AppFullWidthSecondaryButton(
                text = "Voltar",
                onClick = onBack
            )
        }
    }
}
