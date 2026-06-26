package com.documentconverter.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun LoadingState(message: String) {
    AppOutlinedCard {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
        ) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
