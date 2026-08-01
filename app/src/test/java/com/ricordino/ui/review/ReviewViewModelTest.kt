package com.ricordino.ui.review

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.ricordino.data.repository.CaptureResult
import com.ricordino.data.repository.NotesRepository
import com.ricordino.pipeline.Category
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<NotesRepository>()
    private val fakeBitmap = mockk<Bitmap>()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        mockkStatic(BitmapFactory::class)
        mockkStatic(Uri::class)
        every { BitmapFactory.decodeFile(any()) } returns fakeBitmap
        every { Uri.decode(any()) } returns "/tmp/photo.jpg"
    }

    @After
    fun tearDown() {
        unmockkStatic(BitmapFactory::class)
        unmockkStatic(Uri::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `processes the photo on init and exposes editable state`() = runTest(dispatcher) {
        coEvery { repository.processCapture(fakeBitmap) } returns CaptureResult(
            extractedText = "Milk, eggs, bread",
            category = Category.NOTE,
            detectedEntities = emptyList(),
        )

        val viewModel = ReviewViewModel(repository, savedStateHandle("/tmp/photo.jpg"))

        viewModel.uiState.test {
            assertEquals(ReviewUiState.Processing, awaitItem())
            val editing = awaitItem() as ReviewUiState.Editing
            assertEquals("Milk, eggs, bread", editing.extractedText)
            assertEquals(Category.NOTE, editing.category)
        }
    }

    @Test
    fun `updateText and updateCategory edit the in-progress state`() = runTest(dispatcher) {
        coEvery { repository.processCapture(fakeBitmap) } returns CaptureResult(
            extractedText = "original",
            category = Category.NOTE,
            detectedEntities = emptyList(),
        )

        val viewModel = ReviewViewModel(repository, savedStateHandle("/tmp/photo.jpg"))

        viewModel.uiState.test {
            awaitItem() // Processing
            awaitItem() // Editing (initial)

            viewModel.updateText("edited text")
            assertEquals("edited text", (awaitItem() as ReviewUiState.Editing).extractedText)

            viewModel.updateCategory(Category.RECEIPT)
            assertEquals(Category.RECEIPT, (awaitItem() as ReviewUiState.Editing).category)
        }
    }

    @Test
    fun `save persists the note and transitions to Saved`() = runTest(dispatcher) {
        coEvery { repository.processCapture(fakeBitmap) } returns CaptureResult(
            extractedText = "note text",
            category = Category.NOTE,
            detectedEntities = emptyList(),
        )
        coEvery {
            repository.saveNote(fakeBitmap, "note text", Category.NOTE, emptyList())
        } returns 1L

        val viewModel = ReviewViewModel(repository, savedStateHandle("/tmp/photo.jpg"))

        viewModel.uiState.test {
            awaitItem() // Processing
            awaitItem() // Editing

            viewModel.save()
            val saving = awaitItem() as ReviewUiState.Editing
            assertTrue(saving.isSaving)
            assertEquals(ReviewUiState.Saved, awaitItem())
        }
    }

    private fun savedStateHandle(photoPath: String) =
        SavedStateHandle(mapOf(ReviewViewModel.PHOTO_PATH_ARG to photoPath))
}
