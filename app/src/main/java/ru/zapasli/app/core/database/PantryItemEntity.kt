package ru.zapasli.app.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pantry_items",
    indices = [Index(value = ["barcode"])],
)
data class PantryItemEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val barcode: String?,
    val quantity: Double,
    val unit: String,
    @ColumnInfo(name = "storage_location")
    val storageLocation: String,
    @ColumnInfo(name = "expires_at_epoch_day")
    val expiresAtEpochDay: Long?,
    @ColumnInfo(name = "calories_per_100g")
    val caloriesPer100g: Double?,
    @ColumnInfo(name = "protein_per_100g")
    val proteinPer100g: Double?,
    @ColumnInfo(name = "fat_per_100g")
    val fatPer100g: Double?,
    @ColumnInfo(name = "carbohydrates_per_100g")
    val carbohydratesPer100g: Double?,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis")
    val updatedAtEpochMillis: Long,
)
