package com.ricordino.data.repository

import android.graphics.Bitmap
import com.ricordino.data.files.PhotoStorage
import com.ricordino.data.local.DetectedEntity
import com.ricordino.data.local.NoteDao
import com.ricordino.data.local.NoteEntity
import com.ricordino.pipeline.Category
import com.ricordino.pipeline.CategoryClassifier
import com.ricordino.pipeline.EntityDetectionService
import com.ricordino.pipeline.OcrService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

data class CaptureResult(
    val extractedText: String,
    val category: Category,
    val detectedEntities: List<DetectedEntity>,
)

@Singleton
class NotesRepository @Inject constructor(
    private val ocrService: OcrService,
    private val categoryClassifier: CategoryClassifier,
    private val entityDetectionService: EntityDetectionService,
    private val noteDao: NoteDao,
    private val photoStorage: PhotoStorage,
) {
    suspend fun processCapture(bitmap: Bitmap): CaptureResult {
        val text = ocrService.recognize(bitmap)
        val category = categoryClassifier.classify(text)
        val entities = entityDetectionService.detect(text)
        return CaptureResult(text, category, entities)
    }

    suspend fun saveNote(
        bitmap: Bitmap,
        extractedText: String,
        category: Category,
        detectedEntities: List<DetectedEntity>,
    ): Long {
        val path = photoStorage.savePhoto(bitmap)
        return noteDao.insert(
            NoteEntity(
                extractedText = extractedText,
                category = category.name,
                timestamp = System.currentTimeMillis(),
                imageFilePath = path,
                detectedEntities = detectedEntities,
            ),
        )
    }

    suspend fun updateNote(note: NoteEntity, extractedText: String, category: Category) {
        noteDao.update(note.copy(extractedText = extractedText, category = category.name))
    }

    suspend fun deleteNote(note: NoteEntity) {
        photoStorage.deletePhoto(note.imageFilePath)
        noteDao.deleteById(note.id)
    }

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getById(id)

    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAll()

    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.search(query)
}
