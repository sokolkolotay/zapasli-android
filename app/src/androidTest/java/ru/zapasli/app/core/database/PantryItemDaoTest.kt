package ru.zapasli.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PantryItemDaoTest {
    private lateinit var database: ZapasliDatabase
    private lateinit var dao: PantryItemDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ZapasliDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.pantryItemDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun upsertObserveAndDeleteItem() = runBlocking {
        val item = pantryItem(id = "milk", expiryDay = 20_000)

        dao.upsert(item)
        assertEquals(listOf(item), dao.observeAll().first())

        dao.deleteById(item.id)
        assertEquals(emptyList<PantryItemEntity>(), dao.observeAll().first())
    }

    @Test
    fun itemsAreOrderedByExpiryThenName() = runBlocking {
        val noDate = pantryItem(id = "bread", name = "Bread", expiryDay = null)
        val later = pantryItem(id = "yogurt", name = "Yogurt", expiryDay = 20_002)
        val sooner = pantryItem(id = "milk", name = "Milk", expiryDay = 20_001)

        dao.upsert(noDate)
        dao.upsert(later)
        dao.upsert(sooner)

        assertEquals(
            listOf("milk", "yogurt", "bread"),
            dao.observeAll().first().map(PantryItemEntity::id),
        )
    }

    private fun pantryItem(
        id: String,
        name: String = "Milk",
        expiryDay: Long?,
    ) = PantryItemEntity(
        id = id,
        name = name,
        barcode = null,
        quantity = 1.0,
        unit = "PIECE",
        storageLocation = "FRIDGE",
        expiresAtEpochDay = expiryDay,
        caloriesPer100g = null,
        proteinPer100g = null,
        fatPer100g = null,
        carbohydratesPer100g = null,
        createdAtEpochMillis = 0,
        updatedAtEpochMillis = 0,
    )
}
