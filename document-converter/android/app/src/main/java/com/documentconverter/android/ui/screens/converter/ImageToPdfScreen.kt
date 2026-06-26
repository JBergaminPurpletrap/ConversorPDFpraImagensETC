package com.documentconverter.android.ui.screens.converter

import com.documentconverter.android.ui.components.DocumentList
import com.documentconverter.android.ui.screens.result.CompletionPanel
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.components.StatusPanel
import com.documentconverter.android.ui.state.ConversionStatus
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.util.ShareHelper
import com.documentconverter.android.viewmodel.ImageToPdfViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.SnackbarHostState
import com.documentconverter.android.ui.components.AppOutlinedCard
import com.documentconverter.android.ui.components.AppPrimaryButton

@Composable
fun ImageToPdfScreen(viewModel: ImageToPdfViewModel) {
    val scope = rememberCoroutineScope()
    val shareHelper = ShareHelper(LocalContext.current)
    val snackbarHostState = remember { SnackbarHostState() }
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        viewModel.selectImages(uris)
    }
    val pdfCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.convert(uri, scope)
        }
    }

    LaunchedEffect(viewModel.state.status, viewModel.state.message) {
        if (viewModel.state.status == ConversionStatus.Success ||
            viewModel.state.status == ConversionStatus.Error
        ) {
            snackbarHostState.showSnackbar(viewModel.state.message)
        }
    }

    ScreenBody(snackbarHostState = snackbarHostState) {
        AppOutlinedCard {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.TinySpacing)
            ) {
                Text(
                    text = "Imagem para PDF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Selecione JPG, JPEG, PNG ou WEBP. Cada imagem vira uma pagina do PDF.")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            AppPrimaryButton(
                text = "Selecionar imagens",
                modifier = Modifier
                    .weight(1f),
                enabled = !viewModel.state.isRunning,
                containerColor = AppColors.Moss,
                onClick = {
                    imagePicker.launch(arrayOf("image/jpeg", "image/png", "image/webp"))
                }
            )
            AppPrimaryButton(
                text = "Salvar PDF",
                modifier = Modifier
                    .weight(1f),
                enabled = viewModel.selectedImages.isNotEmpty() && !viewModel.state.isRunning,
                containerColor = AppColors.ForestSoft,
                onClick = { pdfCreator.launch("document_converter_imagens.pdf") }
            )
        }
        Text(
            text = "${viewModel.selectedImages.size} imagem(ns) selecionada(s).",
            style = MaterialTheme.typography.titleSmall
        )
        DocumentList(
            title = "Imagens selecionadas",
            documents = viewModel.selectedImages
        )
        StatusPanel(state = viewModel.state)
        CompletionPanel(
            completedConversion = viewModel.completedConversion,
            onOpen = {
                viewModel.completedConversion?.outputUris?.firstOrNull()?.let { uri ->
                    shareHelper.open(uri, "application/pdf")
                }
            },
            onShare = {
                viewModel.completedConversion?.let { conversion ->
                    shareHelper.share(conversion.outputUris, conversion.mimeType)
                }
            },
            onBack = { viewModel.clear() }
        )
    }
}
