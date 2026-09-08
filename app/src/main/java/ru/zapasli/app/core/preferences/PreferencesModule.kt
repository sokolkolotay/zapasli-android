package ru.zapasli.app.core.preferences

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        implementation: DataStoreUserPreferencesRepository,
    ): UserPreferencesRepository
}
