package com.documentconverter.android.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

class DocumentNameResolver(private val context: Context) {
    fun displayName(uri: Uri): String {
        val resolver = context.contentResolver
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return cursor.getString(index)
                }
            }
        }
        return uri.lastPathSegment ?: uri.toString()
    }
}
