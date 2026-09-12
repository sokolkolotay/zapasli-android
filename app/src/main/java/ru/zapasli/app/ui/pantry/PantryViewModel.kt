package ru.zapasli.app.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.PantryRepository
import ru.zapasli.app.domain.catalog.ProductCatalogRepository
import ru.zapasli.app.domain.catalog.ProductCatalogResult
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import ru.zapasli.app.core.preferences.PantryFilterPreference
import ru.zapasli.app.core.preferences.UserPreferencesRepository

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryRepository: PantryRepository,
    private val productCatalogRepository: ProductCatalogRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(PantryFilter.ALL)
    private val searchQuery = MutableStateFlow("")
    private val message = MutableStateFlow<PantryMessage?>(null)
    private val retrySignal = MutableStateFlow(0)
    private val productLookup = MutableStateFlow<ProductLookupUiState>(ProductLookupUiState.Idle)
    private var lookupJob: Job? = null

    init {
        viewModelScope.launch {
            userPreferencesRepository.userPreferences
                .map { it.defaultPantryFilter }
                .distinctUntilChanged()
                .collectLatest { selectedFilter.value = it.toUiFilter() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val itemsResult: Flow<ItemsResult> = retrySignal.flatMapLatest {
        pantryRepository.observeItems()
            .map<List<PantryItem>, ItemsResult>(ItemsResult::Content)
            .onStart { emit(ItemsResult.Loading) }
            .catch { emit(ItemsResult.Error) }
    }

    val uiState = combine(
        itemsResult,
        selectedFilter,
        searchQuery,
        message,
        productLookup,
    ) { result, filter, query, currentMessage, currentProductLookup ->
        when (result) {
            ItemsResult.Loading -> PantryUiState(
                isLoading = true,
                selectedFilter = filter,
                searchQuery = query,
                message = currentMessage,
                productLookup = currentProductLookup,
            )
            ItemsResult.Error -> PantryUiState(
                isLoading = false,
                selectedFilter = filter,
                searchQuery = query,
                loadFailed = true,
                message = currentMessage,
                productLookup = currentProductLookup,
            )
            is ItemsResult.Content -> {
                val today = LocalDate.now()
                PantryUiState(
                    isLoading = false,
                    items = searchPantryItems(
                        filterPantryItems(result.items, filter, today),
                        query,
                    ),
                    allItems = result.items,
                    selectedFilter = filter,
                    searchQuery = query,
                    totalItemCount = result.items.size,
                    attentionItemCount = result.items.attentionCount(today),
                    message = currentMessage,
                    productLookup = currentProductLookup,
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PantryUiState(),
    )

    fun selectFilter(filter: PantryFilter) {
        selectedFilter.value = filter
        viewModelScope.launch {
            userPreferencesRepository.setDefaultPantryFilter(filter.toPreference())
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun retry() {
        retrySignal.update(Int::inc)
    }

    fun saveItem(input: PantryItemInput) {
        val validationError = validatePantryItemInput(input)
        if (validationError != null) {
            message.value = PantryMessage.ValidationFailed(validationError)
            return
        }

        viewModelScope.launch {
            val result = runCatching {
                val existingItem = input.id?.let { pantryRepository.findItem(it) }
                val now = Instant.now()
                pantryRepository.saveItem(
                    PantryItem(
                        id = input.id ?: UUID.randomUUID().toString(),
                        name = input.name.trim(),
                        barcode = input.barcode?.trim()?.ifBlank { null },
                        quantity = input.quantity,
                        unit = input.unit,
                        storageLocation = input.storageLocation,
                        expiresOn = input.expiresOn,
                        nutritionPer100g = input.nutritionPer100g,
                        createdAt = existingItem?.createdAt ?: now,
                        updatedAt = now,
                    ),
                )
            }

            message.value = if (result.isSuccess) {
                if (input.id == null) PantryMessage.ItemAdded else PantryMessage.ItemUpdated
            } else {
                PantryMessage.SaveFailed
            }
        }
    }

    fun deleteItem(id: String) {
        viewModelScope.launch {
            message.value = runCatching { pantryRepository.deleteItem(id) }
                .fold(
                    onSuccess = { PantryMessage.ItemDeleted },
                    onFailure = { PantryMessage.DeleteFailed },
                )
        }
    }

    fun consumeMessage() {
        message.value = null
    }

    fun lookupProduct(barcode: String) {
        lookupJob?.cancel()
        productLookup.value = ProductLookupUiState.Loading(barcode)
        lookupJob = viewModelScope.launch {
            val result = try {
                productCatalogRepository.findByBarcode(barcode)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                ProductCatalogResult.Unavailable
            }

            productLookup.value = when (result) {
                is ProductCatalogResult.Found -> ProductLookupUiState.Found(result.product)
                ProductCatalogResult.NotFound -> ProductLookupUiState.NotFound(barcode)
                ProductCatalogResult.Unavailable -> ProductLookupUiState.Failed(barcode)
            }
        }
    }

    fun clearProductLookup() {
        lookupJob?.cancel()
        lookupJob = null
        productLookup.value = ProductLookupUiState.Idle
    }

    private sealed interface ItemsResult {
        data object Loading : ItemsResult
        data object Error : ItemsResult
        data class Content(val items: List<PantryItem>) : ItemsResult
    }
}

private fun PantryFilterPreference.toUiFilter(): PantryFilter = when (this) {
    PantryFilterPreference.ALL -> PantryFilter.ALL
    PantryFilterPreference.EXPIRING_SOON -> PantryFilter.EXPIRING_SOON
    PantryFilterPreference.EXPIRED -> PantryFilter.EXPIRED
    PantryFilterPreference.NO_DATE -> PantryFilter.NO_DATE
}

private fun PantryFilter.toPreference(): PantryFilterPreference = when (this) {
    PantryFilter.ALL -> PantryFilterPreference.ALL
    PantryFilter.EXPIRING_SOON -> PantryFilterPreference.EXPIRING_SOON
    PantryFilter.EXPIRED -> PantryFilterPreference.EXPIRED
    PantryFilter.NO_DATE -> PantryFilterPreference.NO_DATE
}
