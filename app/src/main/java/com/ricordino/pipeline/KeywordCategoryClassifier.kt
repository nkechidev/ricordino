package com.ricordino.pipeline

import javax.inject.Inject

class KeywordCategoryClassifier @Inject constructor() : CategoryClassifier {
    override suspend fun classify(text: String): Category {
        val lower = text.lowercase()
        return when {
            RECEIPT_KEYWORDS.any { it in lower } && CURRENCY_REGEX.containsMatchIn(text) -> Category.RECEIPT
            "@" in text || PHONE_REGEX.containsMatchIn(text) -> Category.CONTACT
            RECIPE_KEYWORDS.any { it in lower } -> Category.RECIPE
            else -> Category.NOTE
        }
    }

    private companion object {
        val RECEIPT_KEYWORDS = listOf("total", "subtotal", "receipt", "tax", "change due")
        val RECIPE_KEYWORDS = listOf("ingredients", "recipe", "cup ", "tbsp", "tsp", "preheat", "bake")
        val CURRENCY_REGEX = Regex("""[$€£]\s?\d""")
        val PHONE_REGEX = Regex("""(\+?\d[\d\-.\s]{7,}\d)""")
    }
}
