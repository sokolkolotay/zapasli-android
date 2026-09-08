package ru.zapasli.app.core.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "zapasli.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ZapasliDatabase =
        Room.databaseBuilder(
            context,
            ZapasliDatabase::class.java,
            DATABASE_NAME,
        ).build()

    @Provides
    fun providePantryItemDao(database: ZapasliDatabase): PantryItemDao =
        database.pantryItemDao()
}
