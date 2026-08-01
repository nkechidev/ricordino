package com.ricordino.data.files

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val photosDir: File by lazy {
        File(context.filesDir, "photos").apply { mkdirs() }
    }

    suspend fun savePhoto(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
        file.absolutePath
    }

    suspend fun deletePhoto(path: String) = withContext(Dispatchers.IO) {
        File(path).takeIf { it.exists() }?.delete()
    }

    fun resolveFile(path: String): File = File(path)
}
