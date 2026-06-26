  package com.documentconverter.android.ui.navigation

import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.documentconverter.android.ui.components.AppScaffold
import com.documentconverter.android.ui.screens.about.AboutScreen
import com.documentconverter.android.ui.screens.converter.DocxToPdfScreen
import com.documentconverter.android.ui.screens.converter.ImageToPdfScreen
import com.documentconverter.android.ui.screens.converter.PdfToDocxScreen
import com.documentconverter.android.ui.screens.converter.PdfToImageScreen
import com.documentconverter.android.ui.screens.home.HomeScreen
import com.documentconverter.android.ui.screens.settings.SettingsScreen
import com.documentconverter.android.viewmodel.DocxToPdfViewModel
import com.documentconverter.android.viewmodel.ImageToPdfViewModel
import com.documentconverter.android.viewmodel.PdfToDocxViewModel
import com.documentconverter.android.viewmodel.PdfToImageViewModel
import com.documentconverter.android.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(
    imageToPdfViewModel: ImageToPdfViewModel,
    pdfToImageViewModel: PdfToImageViewModel,
    pdfToDocxViewModel: PdfToDocxViewModel,
    docxToPdfViewModel: DocxToPdfViewModel,
    settingsViewModel: SettingsViewModel,
    onExitRequested: () -> Unit
) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }
    var lastHomeBackPress by remember { mutableStateOf(0L) }

    fun navigateHome() {
        currentScreen = AppScreen.Home
        lastHomeBackPress = 0L
    }

    BackHandler {
        if (currentScreen != AppScreen.Home) {
            navigateHome()
        } else {
            val now = SystemClock.elapsedRealtime()
            if (now - lastHomeBackPress <= BACK_EXIT_INTERVAL_MS) {
                onExitRequested()
            } else {
                lastHomeBackPress = now
                Toast.makeText(
                    context,
                    "Pressione voltar novamente para sair.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        AppScaffold(
            title = currentScreen.title,
            canGoBack = currentScreen != AppScreen.Home,
            onBack = ::navigateHome
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (currentScreen) {
                    AppScreen.Home -> HomeScreen(onNavigate = { currentScreen = it })
                    AppScreen.ImageToPdf -> ImageToPdfScreen(imageToPdfViewModel)
                    AppScreen.PdfToImage -> PdfToImageScreen(pdfToImageViewModel)
                    AppScreen.PdfToDocx -> PdfToDocxScreen(pdfToDocxViewModel)
                    AppScreen.DocxToPdf -> DocxToPdfScreen(docxToPdfViewModel)
                    AppScreen.Settings -> SettingsScreen(settingsViewModel)
                    AppScreen.About -> AboutScreen()
                }
            }
        }
    }
}

val AppScreen.title: String
    get() = when (this) {
        AppScreen.Home -> "Document Converter"
        AppScreen.ImageToPdf -> "Imagem para PDF"
        AppScreen.PdfToImage -> "PDF para Imagem"
        AppScreen.PdfToDocx -> "PDF para DOCX"
        AppScreen.DocxToPdf -> "DOCX para PDF"
        AppScreen.Settings -> "Configuracoes"
        AppScreen.About -> "Sobre"
    }

private const val BACK_EXIT_INTERVAL_MS = 2000L
