package ru.zapasli.app.ui.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.zapasli.app.domain.pantry.PantryItem
import ru.zapasli.app.domain.pantry.PantryRepository
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryRepository: PantryRepository,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(PantryFilter.ALL)
    private val message = MutableStateFlow<PantryMessage?>(null)
    private val retrySignal = MutableStateFlow(0)

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
        message,
    ) { result, filter, currentMessage ->
        when (result) {
            ItemsResult.Loading -> PantryUiState(
                isLoading = true,
                selectedFilter = filter,
                message = currentMessage,
            )
            ItemsResult.Error -> PantryUiState(
                isLoading = false,
                selectedFilter = filter,
                loadFailed = true,
                message = currentMessage,
            )
            is ItemsResult.Content -> {
                val today = LocalDate.now()
                PantryUiState(
                    isLoading = false,
                    items = filterPantryItems(result.items, filter, today),
                    selectedFilter = filter,
                    totalItemCount = result.items.size,
                    attentionItemCount = result.items.attentionCount(today),
                    message = currentMessage,
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

    private sealed interface ItemsResult {
        data object Loading : ItemsResult
        data object Error : ItemsResult
        data class Content(val items: List<PantryItem>) : ItemsResult
    }
}
