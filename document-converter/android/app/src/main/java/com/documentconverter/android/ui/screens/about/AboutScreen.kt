package com.documentconverter.android.ui.screens.about

import com.documentconverter.android.ui.components.BrandMark
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.components.AppOutlinedCard
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun AboutScreen() {
    ScreenBody {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
        ) {
            BrandMark()
            Text(
                text = "Document Converter",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text("Versao: 1.0.0")
        }
        AppOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
            ) {
                Text("Funcionalidades", fontWeight = FontWeight.SemiBold)
                Text("\u2713 Imagem -> PDF")
                Text("\u2713 PDF -> Imagem")
                Text("\u2713 PDF protegido com senha")
                Text("\u2713 PDF -> DOCX experimental")
                Text("\u2713 OCR experimental em PDF -> DOCX")
            }
        }
        AppOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
            ) {
                Text("Limitacoes atuais", fontWeight = FontWeight.SemiBold)
                Text("\u2022 PDF -> DOCX extrai texto, mas nao recria layouts complexos")
                Text("\u2022 OCR depende da qualidade da imagem e pode reconhecer texto parcialmente")
                Text("\u2713 DOCX -> PDF simples")
                Text("\u2022 DOCX -> PDF preserva texto, mas nao recria layouts complexos")
            }
        }
    }
}
