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
import com.documentconverter.android.ui.components.PasswordDialog
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.components.StatusPanel
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.ui.screens.result.CompletionPanel
import com.documentconverter.android.ui.state.ConversionStatus
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.util.ShareHelper
import com.documentconverter.android.viewmodel.PdfToDocxViewModel

@Composable
fun PdfToDocxScreen(viewModel: PdfToDocxViewModel) {
    val scope = rememberCoroutineScope()
    val shareHelper = ShareHelper(LocalContext.current)
    val snackbarHostState = remember { SnackbarHostState() }
    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.setSelectedPdf(uri, scope)
    }
    val docxCreator = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(PdfToDocxViewModel.DOCX_MIME_TYPE)
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
                    text = "PDF para DOCX",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Extrai texto do PDF e cria um documento Word simples. Layouts complexos podem perder formatacao.")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            AppPrimaryButton(
                text = "Selecionar PDF",
                modifier = Modifier.weight(1f),
                enabled = !viewModel.state.isRunning,
                containerColor = AppColors.ForestSoft,
                onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
            )
            AppPrimaryButton(
                text = "Salvar DOCX",
                modifier = Modifier.weight(1f),
                enabled = viewModel.selectedPdf != null &&
                    (!viewModel.isProtected || viewModel.pdfPassword.isNotBlank()) &&
                    !viewModel.state.isRunning,
                containerColor = AppColors.Moss,
                onClick = {
                    val defaultName = viewModel.selectedPdf?.displayName
                        ?.substringBeforeLast('.', "documento")
                        ?.plus(".docx")
                        ?: "documento_convertido.docx"
                    docxCreator.launch(defaultName)
                }
            )
        }

        viewModel.selectedPdf?.let { pdf ->
            DocumentList(
                title = "PDF selecionado",
                documents = listOf(SelectedDocument(pdf.uri, pdf.displayName)),
                limit = 1
            )
        }

        viewModel.pageCount?.let { count ->
            Text("$count pagina(s) detectada(s).")
        }

        StatusPanel(state = viewModel.state)
        CompletionPanel(
            completedConversion = viewModel.completedConversion,
            onOpen = {
                viewModel.completedConversion?.outputUris?.firstOrNull()?.let { uri ->
                    shareHelper.open(uri, PdfToDocxViewModel.DOCX_MIME_TYPE)
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

    if (viewModel.showPasswordDialog) {
        PasswordDialog(
            password = viewModel.pdfPassword,
            onPasswordChange = viewModel::updatePassword,
            onConfirm = viewModel::dismissPasswordDialog,
            onDismiss = viewModel::dismissPasswordDialog
        )
    }
}
