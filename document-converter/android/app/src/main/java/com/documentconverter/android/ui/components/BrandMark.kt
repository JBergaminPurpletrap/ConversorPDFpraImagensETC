package com.documentconverter.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun BrandMark(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier.size(AppDimens.BrandMarkSize)) {
        drawRoundRect(
            color = primary,
            topLeft = Offset(size.width * 0.18f, size.height * 0.08f),
            size = Size(size.width * 0.58f, size.height * 0.76f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )
        drawRoundRect(
            color = surface,
            topLeft = Offset(size.width * 0.26f, size.height * 0.22f),
            size = Size(size.width * 0.42f, size.height * 0.08f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = surface,
            topLeft = Offset(size.width * 0.26f, size.height * 0.38f),
            size = Size(size.width * 0.34f, size.height * 0.07f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawOval(
            color = secondary,
            topLeft = Offset(size.width * 0.52f, size.height * 0.48f),
            size = Size(size.width * 0.36f, size.height * 0.28f)
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.58f, size.height * 0.66f),
            end = Offset(size.width * 0.78f, size.height * 0.54f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.78f),
            topLeft = Offset(size.width * 0.17f, size.height * 0.08f),
            size = Size(size.width * 0.58f, size.height * 0.76f),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
