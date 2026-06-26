package com.documentconverter.android.ui.components

import com.documentconverter.android.ui.model.SelectedDocument
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextOverflow
import com.documentconverter.android.ui.theme.AppDimens

@Composable
fun DocumentList(
    title: String,
    documents: List<SelectedDocument>,
    limit: Int = 12
) {
    if (documents.isEmpty()) {
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(AppDimens.CompactSpacing)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        AppOutlinedCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(AppDimens.TinySpacing)
            ) {
                documents.take(limit).forEachIndexed { index, document ->
                    Text(
                        text = "${index + 1}. ${document.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (documents.size > limit) {
                    Text(
                        text = "... e mais ${documents.size - limit}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
