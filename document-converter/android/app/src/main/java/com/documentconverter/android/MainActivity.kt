package com.documentconverter.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.remember
import com.documentconverter.android.ui.navigation.AppNavigation
import com.documentconverter.android.ui.theme.DocumentConverterTheme
import com.documentconverter.android.viewmodel.DocxToPdfViewModel
import com.documentconverter.android.viewmodel.ImageToPdfViewModel
import com.documentconverter.android.viewmodel.PdfToDocxViewModel
import com.documentconverter.android.viewmodel.PdfToImageViewModel
import com.documentconverter.android.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var keepSplash = true
        installSplashScreen().setKeepOnScreenCondition { keepSplash }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplash = false }, 1200)
        super.onCreate(savedInstanceState)
        setContent {
            DocumentConverterTheme {
                val imageToPdfViewModel = remember { ImageToPdfViewModel(this) }
                val pdfToImageViewModel = remember { PdfToImageViewModel(this) }
                val pdfToDocxViewModel = remember { PdfToDocxViewModel(this) }
                val docxToPdfViewModel = remember { DocxToPdfViewModel(this) }
                val settingsViewModel = remember { SettingsViewModel(this) }
                AppNavigation(
                    imageToPdfViewModel = imageToPdfViewModel,
                    pdfToImageViewModel = pdfToImageViewModel,
                    pdfToDocxViewModel = pdfToDocxViewModel,
                    docxToPdfViewModel = docxToPdfViewModel,
                    settingsViewModel = settingsViewModel,
                    onExitRequested = ::finish
                )
            }
        }
    }
}
