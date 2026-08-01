package com.ricordino.data.local

import androidx.room.TypeConverter

// Hand-rolled instead of pulling in a JSON library — Room only needs to persist
// a handful of short entity values, and "::"/"||" won't appear in dates, phone
// numbers, or addresses.
class Converters {
    @TypeConverter
    fun fromDetectedEntities(entities: List<DetectedEntity>): String =
        entities.joinToString(separator = "||") { "${it.type.name}::${it.value}" }

    @TypeConverter
    fun toDetectedEntities(raw: String): List<DetectedEntity> {
        if (raw.isEmpty()) return emptyList()
        return raw.split("||").map { entry ->
            val (type, value) = entry.split("::", limit = 2)
            DetectedEntity(EntityType.valueOf(type), value)
        }
    }
}
