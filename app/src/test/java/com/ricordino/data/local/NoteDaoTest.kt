package com.ricordino.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Robolectric 4.13's newest supported framework jar is API 34, behind our targetSdk 36 —
// pinned here since Room DAO behavior doesn't depend on the exact framework level anyway.
@Config(sdk = [34])
@RunWith(RobolectricTestRunner::class)
class NoteDaoTest {
    private lateinit var database: RicordinoDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RicordinoDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `search matches notes containing the query in extractedText`() = runTest {
        dao.insert(note(text = "Grocery receipt total \$42.10"))
        dao.insert(note(text = "Call mom about dinner"))

        val results = dao.search("receipt").first()

        assertEquals(1, results.size)
        assertTrue(results.first().extractedText.contains("receipt"))
    }

    @Test
    fun `getAll returns notes ordered by timestamp descending`() = runTest {
        dao.insert(note(text = "first", timestamp = 100))
        dao.insert(note(text = "second", timestamp = 200))

        val results = dao.getAll().first()

        assertEquals(listOf("second", "first"), results.map { it.extractedText })
    }

    @Test
    fun `deleteById removes the note`() = runTest {
        val id = dao.insert(note(text = "temp"))

        dao.deleteById(id)

        assertTrue(dao.getAll().first().isEmpty())
    }

    private fun note(text: String, timestamp: Long = 0L) = NoteEntity(
        extractedText = text,
        category = "NOTE",
        timestamp = timestamp,
        imageFilePath = "/tmp/fake.jpg",
    )
}
