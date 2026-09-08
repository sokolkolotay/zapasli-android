package ru.zapasli.app.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {
    @Query(
        """
        SELECT * FROM pantry_items
        ORDER BY expires_at_epoch_day IS NULL,
                 expires_at_epoch_day ASC,
                 name COLLATE NOCASE ASC
        """,
    )
    fun observeAll(): Flow<List<PantryItemEntity>>

    @Query("SELECT * FROM pantry_items WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): PantryItemEntity?

    @Upsert
    suspend fun upsert(item: PantryItemEntity)

    @Query("DELETE FROM pantry_items WHERE id = :id")
    suspend fun deleteById(id: String)
}
