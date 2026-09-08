package ru.zapasli.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PantryItemEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ZapasliDatabase : RoomDatabase() {
    abstract fun pantryItemDao(): PantryItemDao
}
