package com.documentconverter.androidpoc.poc

data class PocResult(
    val status: String,
    val message: String,
    val elapsedMs: Long? = null,
    val details: String = ""
) {
    companion object {
        fun success(message: String, elapsedMs: Long, details: String = "") =
            PocResult("OK", message, elapsedMs, details)

        fun failure(message: String, elapsedMs: Long? = null, details: String = "") =
            PocResult("FALHA", message, elapsedMs, details)

        fun info(message: String) =
            PocResult("INFO", message)
    }
}
