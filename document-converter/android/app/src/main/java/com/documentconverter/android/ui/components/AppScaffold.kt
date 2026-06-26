package com.documentconverter.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun AppScaffold(
    title: String,
    canGoBack: Boolean,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            AppTopBar(title = title, canGoBack = canGoBack, onBack = onBack)
        },
        content = content
    )
}

@Composable
fun ScreenBody(
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = AppDimens.ScreenHorizontalPadding,
                    vertical = AppDimens.ScreenVerticalPadding
                ),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SectionSpacing)
        ) {
            content()
        }
        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppDimens.SnackbarPadding)
            )
        }
    }
}
