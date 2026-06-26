package com.documentconverter.android.ui.screens.home

import com.documentconverter.android.ui.navigation.AppScreen
import com.documentconverter.android.ui.components.ActionCard
import com.documentconverter.android.ui.components.BrandMark
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.theme.AppColors
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
fun HomeScreen(onNavigate: (AppScreen) -> Unit) {
    ScreenBody {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
        ) {
            BrandMark()
            Text(
                text = "Conversor de Documentos Bergamin Company LTDA",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Converta imagens e PDFs localmente, sem internet.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        ActionCard(
            title = "Imagem para PDF",
            description = "Selecione uma ou varias imagens e gere um PDF multipagina.",
            buttonText = "Imagem/s para PDF",
            buttonContainerColor = AppColors.Moss,
            onClick = { onNavigate(AppScreen.ImageToPdf) }
        )
        ActionCard(
            title = "PDF para Imagem",
            description = "Converta todas as paginas de um PDF como arquivos de imagem, com senha quando necessario.",
            buttonText = "PDF para Imagem/s",
            buttonContainerColor = AppColors.ForestSoft,
            onClick = { onNavigate(AppScreen.PdfToImage) }
        )
        ActionCard(
            title = "PDF para DOCX",
            description = "Extraia texto de PDFs e gere um documento Word simples.",
            buttonText = "PDF para DOCX",
            buttonContainerColor = AppColors.Olive,
            onClick = { onNavigate(AppScreen.PdfToDocx) }
        )
        ActionCard(
            title = "DOCX para PDF",
            description = "Converta o texto de documentos Word em um PDF simples.",
            buttonText = "DOCX para PDF",
            buttonContainerColor = AppColors.Moss,
            onClick = { onNavigate(AppScreen.DocxToPdf) }
        )
        ActionCard(
            title = "Configuracoes",
            description = "Ajuste OCR, qualidade e comportamento das conversoes experimentais.",
            buttonText = "Abrir Configuracoes",
            buttonContainerColor = AppColors.Olive,
            onClick = { onNavigate(AppScreen.Settings) }
        )
        ActionCard(
            title = "Sobre",
            description = "Veja a versao, recursos suportados e limitacoes conhecidas.",
            buttonText = "Abrir Sobre",
            buttonContainerColor = AppColors.TextSecondary,
            onClick = { onNavigate(AppScreen.About) }
        )
    }
}
