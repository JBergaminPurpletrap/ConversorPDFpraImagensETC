package com.documentconverter.android.ui.screens.converter

import com.documentconverter.android.converter.PdfToImageConverter.ImageOutputFormat
import com.documentconverter.android.ui.state.ConversionStatus
import com.documentconverter.android.ui.model.SelectedDocument
import com.documentconverter.android.ui.screens.result.CompletionPanel
import com.documentconverter.android.ui.components.DocumentList
import com.documentconverter.android.ui.components.ScreenBody
import com.documentconverter.android.ui.components.StatusPanel
import com.documentconverter.android.ui.theme.AppColors
import com.documentconverter.android.ui.theme.AppDimens
import com.documentconverter.android.util.ShareHelper
import com.documentconverter.android.viewmodel.PdfToImageViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.documentconverter.android.ui.components.AppSecondaryButton
import com.documentconverter.android.ui.components.PasswordDialog

@Composable
fun PdfToImageScreen(viewModel: PdfToImageViewModel) {
    val scope = rememberCoroutineScope()
    val shareHelper = ShareHelper(LocalContext.current)
    val snackbarHostState = remember { SnackbarHostState() }
    val pdfPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        viewModel.setSelectedPdf(uri, scope)
    }
    val folderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
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
                    text = "PDF para Imagem",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Selecione o PDF e uma pasta para salvar pagina_1.jpg, pagina_2.jpg... E selecione como deseja o tipo de imagem, mais comum JPEG")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
        ) {
            AppPrimaryButton(
                text = "Selecionar PDF",
                modifier = Modifier
                    .weight(1f),
                enabled = !viewModel.state.isRunning,
                containerColor = AppColors.ForestSoft,
                onClick = { pdfPicker.launch(arrayOf("application/pdf")) }
            )
            AppPrimaryButton(
                text = "Salvar ${viewModel.selectedOutputFormat.label}",
                modifier = Modifier
                    .weight(1f),
                enabled = viewModel.selectedPdf != null &&
                    (!viewModel.isProtected || viewModel.pdfPassword.isNotBlank()) &&
                    !viewModel.state.isRunning,
                containerColor = AppColors.Moss,
                onClick = { folderPicker.launch(null) }
            )
        }
        AppOutlinedCard {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.ContentSpacing)
            ) {
                Text(
                    text = "Formato de saida",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text("Escolha como sera a imagem. DICA: JPEG e o mais comum")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)
                ) {
                    ImageFormatButton(
                        text = "JPEG (.jpg)",
                        selected = viewModel.selectedOutputFormat == ImageOutputFormat.JPEG,
                        enabled = !viewModel.state.isRunning,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectOutputFormat(ImageOutputFormat.JPEG) }
                    )
                    ImageFormatButton(
                        text = "PNG (.png)",
                        selected = viewModel.selectedOutputFormat == ImageOutputFormat.PNG,
                        enabled = !viewModel.state.isRunning,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectOutputFormat(ImageOutputFormat.PNG) }
                    )
                }
            }
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
                    shareHelper.open(uri, viewModel.selectedOutputFormat.mimeType)
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

@Composable
private fun ImageFormatButton(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    if (selected) {
        AppPrimaryButton(
            text = text,
            modifier = modifier.heightIn(min = AppDimens.SmallButtonMinHeight),
            enabled = enabled,
            containerColor = AppColors.Moss,
            onClick = onClick
        )
    } else {
        AppSecondaryButton(
            text = text,
            modifier = modifier.heightIn(min = AppDimens.SmallButtonMinHeight),
            enabled = enabled,
            onClick = onClick
        )
    }
}
