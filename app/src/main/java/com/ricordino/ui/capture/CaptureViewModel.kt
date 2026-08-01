package com.ricordino.ui.capture

import android.content.Context
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class CaptureViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    // CameraX's takePicture is callback-based, not Task-based like ML Kit — bridge it
    // ourselves so the rest of the app (Repository, ReviewViewModel) stays suspend-based.
    // Written to cacheDir since it's a temporary file; the real photo file is created by
    // PhotoStorage once the user confirms save on the Review screen.
    suspend fun capturePhoto(imageCapture: ImageCapture): String =
        suspendCancellableCoroutine { continuation ->
            val photoFile = File(context.cacheDir, "capture_${UUID.randomUUID()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        continuation.resume(photoFile.absolutePath)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                },
            )
        }
}
