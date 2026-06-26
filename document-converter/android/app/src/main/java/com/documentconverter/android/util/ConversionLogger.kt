package com.documentconverter.android.util

import android.content.Context
import android.util.Log
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ConversionLogger(context: Context) {
    private val logDir = context.applicationContext.filesDir.resolve("logs")
    private val logFile = logDir.resolve("conversion-log.txt")

    fun conversionStarted(operation: String, sourceCount: Int) {
        write("START operation=$operation sources=$sourceCount")
    }

    fun conversionCompleted(operation: String, elapsedMs: Long, outputCount: Int) {
        write("SUCCESS operation=$operation elapsedMs=$elapsedMs outputs=$outputCount")
    }

    fun conversionFailed(operation: String, message: String, details: String = "") {
        write("ERROR operation=$operation message=\"$message\" details=\"$details\"")
    }

    private fun write(message: String) {
        try {
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            logFile.appendText("$timestamp $message\n")
        } catch (error: Throwable) {
            Log.w(TAG, "Nao foi possivel gravar log local.", error)
        }
    }

    private companion object {
        private const val TAG = "ConversionLogger"
    }
}
