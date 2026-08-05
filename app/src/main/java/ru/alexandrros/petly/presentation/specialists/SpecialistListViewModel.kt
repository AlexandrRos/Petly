package ru.alexandrros.petly.presentation.specialists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.usecase.GetAllSpecialistsUseCase
import kotlin.collections.emptyList

class SpecialistListViewModel(
    private val getAllSpecialists: GetAllSpecialistsUseCase
) : ViewModel() {

    val specialists: StateFlow<List<User>> = getAllSpecialists()
        .catch { e ->
            Log.e("SpecialistListVM", "Error loading specialists", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    class Factory(
        private val getAllSpecialists: GetAllSpecialistsUseCase
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SpecialistListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SpecialistListViewModel(getAllSpecialists) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}