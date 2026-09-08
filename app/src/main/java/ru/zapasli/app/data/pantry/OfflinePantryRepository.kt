package ru.zapasli.app.data.pantry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.zapasli.app.core.database.PantryItemDao
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.PantryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflinePantryRepository @Inject constructor(
    private val pantryItemDao: PantryItemDao,
) : PantryRepository {
    override fun observeItems(): Flow<List<PantryItem>> =
        pantryItemDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun findItem(id: String): PantryItem? =
        pantryItemDao.findById(id)?.toDomain()

    override suspend fun saveItem(item: PantryItem) {
        pantryItemDao.upsert(item.toEntity())
    }

    override suspend fun deleteItem(id: String) {
        pantryItemDao.deleteById(id)
    }
}
