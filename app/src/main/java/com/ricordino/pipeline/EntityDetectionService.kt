package com.ricordino.pipeline

import android.content.Context
import android.view.textclassifier.TextClassificationManager
import android.view.textclassifier.TextClassifier
import android.view.textclassifier.TextLinks
import com.ricordino.data.local.DetectedEntity
import com.ricordino.data.local.EntityType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EntityDetectionService @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    // android.view.textclassifier.TextClassifier.generateLinks is a blocking call, not
    // Task-based like ML Kit — Dispatchers.Default since this is CPU-bound, not I/O.
    suspend fun detect(text: String): List<DetectedEntity> = withContext(Dispatchers.Default) {
        if (text.isBlank()) return@withContext emptyList()

        val manager = context.getSystemService(TextClassificationManager::class.java)
            ?: return@withContext emptyList()
        val links = manager.textClassifier.generateLinks(TextLinks.Request.Builder(text).build())

        links.links.mapNotNull { link ->
            if (link.entityCount == 0) return@mapNotNull null
            val entityType = link.getEntity(0)
            // On dense numeric/technical text (ingredient percentages, measurements) the
            // classifier sometimes misfires with low confidence — e.g. "0.4%" read as a
            // fragment of a date. Requiring a reasonably confident match filters those out.
            if (link.getConfidenceScore(entityType) < MIN_CONFIDENCE) return@mapNotNull null
            val type = when (entityType) {
                TextClassifier.TYPE_DATE, TextClassifier.TYPE_DATE_TIME -> EntityType.DATE
                TextClassifier.TYPE_PHONE -> EntityType.PHONE
                TextClassifier.TYPE_ADDRESS -> EntityType.ADDRESS
                else -> null
            } ?: return@mapNotNull null
            DetectedEntity(type, text.substring(link.start, link.end))
        }
    }

    private companion object {
        const val MIN_CONFIDENCE = 0.5f
    }
}
