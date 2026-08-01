package com.ricordino.pipeline

enum class Category { RECEIPT, CONTACT, RECIPE, NOTE, OTHER }

// The one seam designed for v2 churn: KeywordCategoryClassifier implements this now,
// LlmCategoryClassifier implements it later — no caller changes when that swap happens.
interface CategoryClassifier {
    suspend fun classify(text: String): Category
}
