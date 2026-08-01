package com.ricordino.pipeline

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class KeywordCategoryClassifierTest {
    private val classifier = KeywordCategoryClassifier()

    @Test
    fun `classifies receipt text with currency and total keyword`() = runTest {
        val result = classifier.classify("Grocery Store\nSubtotal: \$38.50\nTax: \$3.10\nTotal: \$41.60")
        assertEquals(Category.RECEIPT, result)
    }

    @Test
    fun `classifies text with an email address as contact`() = runTest {
        val result = classifier.classify("Jane Doe\njane.doe@example.com")
        assertEquals(Category.CONTACT, result)
    }

    @Test
    fun `classifies text with a phone number as contact`() = runTest {
        val result = classifier.classify("Call me at 415-555-0192")
        assertEquals(Category.CONTACT, result)
    }

    @Test
    fun `classifies recipe text with ingredient keywords`() = runTest {
        val result = classifier.classify("Ingredients:\n2 cups flour\n1 tsp salt\nPreheat oven to 350F")
        assertEquals(Category.RECIPE, result)
    }

    @Test
    fun `falls back to note for plain text`() = runTest {
        val result = classifier.classify("Remember to water the plants this weekend")
        assertEquals(Category.NOTE, result)
    }
}
