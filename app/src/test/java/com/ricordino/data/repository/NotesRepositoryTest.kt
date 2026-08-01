package com.ricordino.data.repository

import android.graphics.Bitmap
import com.ricordino.data.files.PhotoStorage
import com.ricordino.data.local.DetectedEntity
import com.ricordino.data.local.EntityType
import com.ricordino.data.local.NoteDao
import com.ricordino.pipeline.Category
import com.ricordino.pipeline.CategoryClassifier
import com.ricordino.pipeline.EntityDetectionService
import com.ricordino.pipeline.OcrService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class NotesRepositoryTest {

    private val ocrService = mockk<OcrService>()
    private val categoryClassifier = mockk<CategoryClassifier>()
    private val entityDetectionService = mockk<EntityDetectionService>()
    private val noteDao = mockk<NoteDao>(relaxed = true)
    private val photoStorage = mockk<PhotoStorage>()
    private val bitmap = mockk<Bitmap>()

    private val repository = NotesRepository(
        ocrService = ocrService,
        categoryClassifier = categoryClassifier,
        entityDetectionService = entityDetectionService,
        noteDao = noteDao,
        photoStorage = photoStorage,
    )

    @Test
    fun `processCapture runs OCR, classification, and entity detection in order`() = runTest {
        coEvery { ocrService.recognize(bitmap) } returns "Total: \$12.00"
        coEvery { categoryClassifier.classify("Total: \$12.00") } returns Category.RECEIPT
        coEvery { entityDetectionService.detect("Total: \$12.00") } returns
            listOf(DetectedEntity(EntityType.DATE, "2024-01-01"))

        val result = repository.processCapture(bitmap)

        assertEquals("Total: \$12.00", result.extractedText)
        assertEquals(Category.RECEIPT, result.category)
        assertEquals(listOf(DetectedEntity(EntityType.DATE, "2024-01-01")), result.detectedEntities)
    }

    @Test
    fun `saveNote stores the photo then inserts a note referencing its path`() = runTest {
        coEvery { photoStorage.savePhoto(bitmap) } returns "/data/photos/abc.jpg"
        coEvery { noteDao.insert(any()) } returns 7L

        val id = repository.saveNote(
            bitmap = bitmap,
            extractedText = "hello",
            category = Category.NOTE,
            detectedEntities = emptyList(),
        )

        assertEquals(7L, id)
        coVerify {
            noteDao.insert(
                match { it.imageFilePath == "/data/photos/abc.jpg" && it.extractedText == "hello" && it.category == "NOTE" },
            )
        }
    }
}
