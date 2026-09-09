package ru.zapasli.app.data.catalog

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import ru.zapasli.app.domain.catalog.ProductCatalogRepository
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProductCatalogModule {
    @Binds
    @Singleton
    abstract fun bindProductCatalogRepository(
        implementation: OpenFoodFactsProductCatalogRepository,
    ): ProductCatalogRepository

    companion object {
        @Provides
        @Singleton
        fun provideHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            isLenient = false
        }
    }
}
