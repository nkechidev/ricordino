package com.ricordino.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val extractedText: String,
    val category: String,
    val timestamp: Long,
    val imageFilePath: String,
    val detectedEntities: List<DetectedEntity> = emptyList(),
)

data class DetectedEntity(
    val type: EntityType,
    val value: String,
)

enum class EntityType { DATE, PHONE, ADDRESS }
