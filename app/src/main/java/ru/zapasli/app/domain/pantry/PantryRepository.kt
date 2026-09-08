package ru.zapasli.app.domain.pantry

import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    fun observeItems(): Flow<List<PantryItem>>

    suspend fun findItem(id: String): PantryItem?

    suspend fun saveItem(item: PantryItem)

    suspend fun deleteItem(id: String)
}
