package com.documentconverter.android.ui.state

data class ConversionState(
    val status: ConversionStatus = ConversionStatus.Idle,
    val message: String = "Pronto para converter.",
    val details: String = "",
    val elapsedMs: Long? = null,
    val progressText: String = "",
    val isRunning: Boolean = false
) {
    companion object {
        fun idle(message: String = "Pronto para converter.") = ConversionState(
            status = ConversionStatus.Idle,
            message = message
        )

        fun running(message: String, progressText: String = "") = ConversionState(
            status = ConversionStatus.Running,
            message = message,
            progressText = progressText,
            isRunning = true
        )

        fun success(message: String, elapsedMs: Long, details: String = "") = ConversionState(
            status = ConversionStatus.Success,
            message = message,
            details = details,
            elapsedMs = elapsedMs
        )

        fun error(message: String, details: String = "") = ConversionState(
            status = ConversionStatus.Error,
            message = message,
            details = details
        )
    }
}

enum class ConversionStatus {
    Idle,
    Running,
    Success,
    Error
}
