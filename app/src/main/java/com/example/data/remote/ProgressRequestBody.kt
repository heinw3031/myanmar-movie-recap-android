package com.example.data.remote

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream

class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType?,
    private val onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var uploaded = 0L
        var lastEmitTime = 0L

        FileInputStream(file).use { inputStream ->
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                sink.write(buffer, 0, read)
                uploaded += read
                val now = System.currentTimeMillis()
                if (now - lastEmitTime >= 80L || uploaded >= totalBytes) {
                    lastEmitTime = now
                    onProgress(uploaded, totalBytes)
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 16 * 1024
    }
}
