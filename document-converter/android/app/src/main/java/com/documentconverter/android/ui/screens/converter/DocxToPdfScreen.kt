package com.documentconverter.android.ui.screens.converter

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.documentconverter.android.ui.components.AppOutlinedCard
import com.documentconverter.android.ui.components.AppPrimaryButton
import com.documentconverter.android.ui.components.DocumentList
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.components.StatusPanel
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.ui.screens.result.CompletionPanel
import com.documentconverter.android.ui.state.ConversionStatus
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.util.ShareHelper
import com.documentconverter.android.viewmodel.DocxToPdfViewModel

@Composable
fun DocxToPdfScreen(viewModel: DocxToPdfViewModel) {
    val scope = rememberCoroutineScope()
    val shareHelper = ShareHelper(LocalContext.current)
    val snackbarHostState = remember { SnackbarHostState() }
    val docxPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.setSelectedDocx(uri)
    }
    val pdfCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(DocxToPdfViewModel.PDF_MIME_TYPE)
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppDimens.TinySpacing)
            ) {
                Text(
                    text = "DOCX para PDF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Cria um PDF simples com o texto do DOCX. Imagens, tabelas e layouts complexos podem nao ser preservados.")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            AppPrimaryButton(
                text = "Selecionar DOCX",
                modifier = Modifier.weight(1f),
                enabled = !viewModel.state.isRunning,
                containerColor = AppColors.ForestSoft,
                onClick = { docxPicker.launch(arrayOf(DocxToPdfViewModel.DOCX_MIME_TYPE)) }
            )
            AppPrimaryButton(
                text = "Salvar PDF",
                modifier = Modifier.weight(1f),
                enabled = viewModel.selectedDocx != null && !viewModel.state.isRunning,
                containerColor = AppColors.Moss,
                onClick = {
                    val defaultName = viewModel.selectedDocx?.displayName
                        ?.substringBeforeLast('.', "documento")
                        ?.plus(".pdf")
                        ?: "documento_convertido.pdf"
                    pdfCreator.launch(defaultName)
                }
            )
        }

        viewModel.selectedDocx?.let { docx ->
            DocumentList(
                title = "DOCX selecionado",
                documents = listOf(SelectedDocument(docx.uri, docx.displayName)),
                limit = 1
            )
        }

        StatusPanel(state = viewModel.state)
        CompletionPanel(
            completedConversion = viewModel.completedConversion,
            onOpen = {
                viewModel.completedConversion?.outputUris?.firstOrNull()?.let { uri ->
                    shareHelper.open(uri, DocxToPdfViewModel.PDF_MIME_TYPE)
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
