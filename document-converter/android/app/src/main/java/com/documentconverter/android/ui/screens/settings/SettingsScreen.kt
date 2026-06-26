package com.documentconverter.android.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.documentconverter.android.ui.components.AppOutlinedCard
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    ScreenBody {
        AppOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
            ) {
                Text(
                    text = "PDF para DOCX",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Controle o OCR usado quando o PDF nao possui texto selecionavel.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        AppOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("OCR experimental", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Reconhece texto em PDFs escaneados. Pode demorar mais em arquivos grandes.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = viewModel.ocrEnabled,
                        onCheckedChange = viewModel::updateOcrEnabled
                    )
                }

                Text(
                    text = "Qualidade do OCR: ${viewModel.ocrDpi} DPI",
                    fontWeight = FontWeight.SemiBold
                )
                Slider(
                    value = viewModel.ocrDpi.toFloat(),
                    onValueChange = { value -> viewModel.updateOcrDpi(value.roundToInt()) },
                    enabled = viewModel.ocrEnabled,
                    valueRange = SettingsViewModel.MIN_OCR_DPI.toFloat()..SettingsViewModel.MAX_OCR_DPI.toFloat(),
                    steps = 2
                )
                Text(
                    text = "150 DPI e mais rapido. 200 DPI e o padrao. 300 DPI pode melhorar scans pequenos, mas consome mais memoria.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
