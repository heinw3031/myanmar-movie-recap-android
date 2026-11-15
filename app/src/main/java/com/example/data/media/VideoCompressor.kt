package com.example.data.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

data class PickedVideoInfo(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val mimeType: String
)

data class PreparedVideoFile(
    val file: File,
    val mimeType: String,
    val wasCompressed: Boolean
)

class VideoCompressor(private val context: Context) {

    fun inspectPickedVideo(uri: Uri): PickedVideoInfo {
        var name = "selected_video.mp4"
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIdx >= 0) {
                    name = cursor.getString(nameIdx) ?: name
                }
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIdx >= 0) {
                    size = cursor.getLong(sizeIdx)
                }
            }
        }
        val mime = context.contentResolver.getType(uri) ?: "video/mp4"
        return PickedVideoInfo(
            uri = uri,
            displayName = name,
            sizeBytes = size,
            mimeType = mime
        )
    }

    suspend fun prepareForUpload(
        picked: PickedVideoInfo,
        compress: Boolean,
        onCompressionProgress: (Int) -> Unit
    ): PreparedVideoFile {
        val workDir = File(context.cacheDir, "upload_staging").apply { mkdirs() }
        workDir.listFiles()?.forEach { old ->
            if (System.currentTimeMillis() - old.lastModified() > 2 * 60 * 60 * 1000L) {
                old.delete()
            }
        }

        val sourceFile = File(workDir, "src_${System.currentTimeMillis()}.mp4")
        withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(picked.uri)?.use { input ->
                FileOutputStream(sourceFile).use { output ->
                    input.copyTo(output)
                }
            } ?: throw IllegalStateException("ရွေးချယ်ထားသော ဗီဒီယိုဖိုင်ကို ဖွင့်၍မရပါ")
        }

        val shouldCompress = compress && sourceFile.length() > COMPRESS_THRESHOLD_BYTES
        if (!shouldCompress) {
            return PreparedVideoFile(
                file = sourceFile,
                mimeType = picked.mimeType,
                wasCompressed = false
            )
        }

        val compressedFile = File(workDir, "cmp_${System.currentTimeMillis()}.mp4")
        val compressedOk = tryTranscodeTo720p(
            inputUri = Uri.fromFile(sourceFile),
            outputFile = compressedFile,
            onProgress = onCompressionProgress
        )

        return if (compressedOk && compressedFile.exists() && compressedFile.length() in 1024L until sourceFile.length()) {
            sourceFile.delete()
            PreparedVideoFile(
                file = compressedFile,
                mimeType = MimeTypes.VIDEO_MP4,
                wasCompressed = true
            )
        } else {
            compressedFile.delete()
            PreparedVideoFile(
                file = sourceFile,
                mimeType = picked.mimeType,
                wasCompressed = false
            )
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun tryTranscodeTo720p(
        inputUri: Uri,
        outputFile: File,
        onProgress: (Int) -> Unit
    ): Boolean = withContext(Dispatchers.Main) {
        var finished = false
        val result = suspendCancellableCoroutine<Boolean> { cont ->
            val mediaItem = MediaItem.fromUri(inputUri)
            val videoEffects = listOf(Presentation.createForHeight(720))
            val editedMediaItem = EditedMediaItem.Builder(mediaItem)
                .setEffects(Effects(emptyList(), videoEffects))
                .build()

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        finished = true
                        onProgress(100)
                        if (cont.isActive) cont.resume(true)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        finished = true
                        if (cont.isActive) cont.resume(false)
                    }
                })
                .build()

            try {
                transformer.start(editedMediaItem, outputFile.absolutePath)
            } catch (_: Exception) {
                finished = true
                if (cont.isActive) cont.resume(false)
                return@suspendCancellableCoroutine
            }

            cont.invokeOnCancellation {
                try {
                    transformer.cancel()
                } catch (_: Exception) {
                }
            }

            // Poll Transformer progress on Main looper cleanly
            val progressHolder = ProgressHolder()
            val mainHandler = android.os.Handler(context.mainLooper)
            val progressRunnable = object : Runnable {
                override fun run() {
                    if (!finished && cont.isActive) {
                        val state = transformer.getProgress(progressHolder)
                        if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                            onProgress(progressHolder.progress.coerceIn(0, 99))
                        }
                        mainHandler.postDelayed(this, 300L)
                    }
                }
            }
            mainHandler.postDelayed(progressRunnable, 300L)
        }
        delay(50)
        result
    }

    companion object {
        // Compress videos larger than 15 MB before upload
        private const val COMPRESS_THRESHOLD_BYTES = 15L * 1024L * 1024L
    }
}
