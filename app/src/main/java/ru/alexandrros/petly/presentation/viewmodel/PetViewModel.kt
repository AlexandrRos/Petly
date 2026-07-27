package ru.alexandrros.petly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.alexandrros.petly.data.repository.FirebasePetRepository
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.UserRepository

class PetListViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val userIdFlow = userRepository.getCurrentUser()
        .map { it?.uid ?: "" }
    private val repositoryFlow = userIdFlow.map { uid ->
        if (uid.isNotEmpty()) FirebasePetRepository(uid)
        else null
    }

    // Flatten: StateFlow of list of pets, empty if no user
    @OptIn(ExperimentalCoroutinesApi::class)
    val pets: StateFlow<List<Pet>> = repositoryFlow.flatMapLatest { repo ->
        repo?.getAllPets() ?: MutableStateFlow(emptyList())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private suspend fun currentRepository(): FirebasePetRepository? {
        val uid = userIdFlow.first()
        return if (uid.isNotEmpty()) FirebasePetRepository(uid) else null
    }

    fun getPetById(id: String): Pet? {
        // This is a blocking call on StateFlow – acceptable for UI lookups
        return pets.value.find { it.id == id }
    }

    fun addPet(pet: Pet) {
        viewModelScope.launch {
            currentRepository()?.addPet(pet)
        }
    }

    fun updatePet(updatedPet: Pet) {
        viewModelScope.launch {
            currentRepository()?.updatePet(updatedPet)
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            currentRepository()?.deletePet(petId)
        }
    }

    class Factory(
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PetListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PetListViewModel(userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
