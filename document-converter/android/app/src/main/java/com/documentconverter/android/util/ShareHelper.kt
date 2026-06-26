package com.documentconverter.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

class ShareHelper(private val context: Context) {
    fun open(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startSafely(Intent.createChooser(intent, "Abrir arquivo"))
    }

    fun share(uris: List<Uri>, mimeType: String) {
        if (uris.isEmpty()) {
            Toast.makeText(context, "Nenhum arquivo para compartilhar.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uris.first())
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startSafely(Intent.createChooser(intent, "Compartilhar arquivo"))
    }

    private fun startSafely(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (_: Throwable) {
            Toast.makeText(context, "Nenhum aplicativo disponivel para esta acao.", Toast.LENGTH_SHORT).show()
        }
    }
}
