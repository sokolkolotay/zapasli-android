package ru.zapasli.app.data.pantry

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.zapasli.app.domain.pantry.PantryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PantryDataModule {
    @Binds
    @Singleton
    abstract fun bindPantryRepository(
        implementation: OfflinePantryRepository,
    ): PantryRepository
}
