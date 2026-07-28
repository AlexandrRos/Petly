package ru.alexandrros.petly.data.repository;


import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.alexandrros.petly.data.remote.datasource.FirebasePetDataSource
import ru.alexandrros.petly.data.remote.mapper.toDomain
import ru.alexandrros.petly.data.remote.mapper.toDto
import ru.alexandrros.petly.domain.model.Pet
import ru.alexandrros.petly.domain.repository.PetRepository
import kotlin.coroutines.cancellation.CancellationException


class FirebasePetRepository(
    private val userIdFlow: Flow<String>,
    private val dataSource: FirebasePetDataSource = FirebasePetDataSource(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) : PetRepository {

    private val petsFlow: Flow<List<Pet>> = userIdFlow
        .flatMapLatest { uid ->
            if (uid.isEmpty()) flowOf(emptyList())
            else dataSource.getPetsByUser(uid)
                .map { dtos -> dtos.map { it.toDomain() } }
        }
        .catch { e ->
            Log.e("FirebasePetRepo", "Error observing pets", e)
            emit(emptyList())
        }

    private val petsState = petsFlow.stateIn(
        scope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    override fun getAllPets(): StateFlow<List<Pet>> = petsState

    override fun getPetById(id: String): Pet? = petsState.value.find { it.id == id }

    override fun getPetById(userId: String, petId: String): Flow<Pet?> = flow {
        val dto = dataSource.getPetById(userId, petId)
        emit(dto?.toDomain())
    }.catch { e ->
        if (e is CancellationException) throw e
        Log.e("FirebasePetRepo", "Error fetching pet by id", e)
        emit(null)
    }

    override suspend fun addPet(pet: Pet) {
        val uid = userIdFlow.first()
        if (uid.isNotEmpty()) dataSource.addPet(uid, pet.toDto())
    }

    override suspend fun updatePet(updatedPet: Pet) {
        val uid = userIdFlow.first()
        if (uid.isNotEmpty()) dataSource.updatePet(uid, updatedPet.toDto())
    }

    override suspend fun deletePet(petId: String) {
        val uid = userIdFlow.first()
        if (uid.isNotEmpty()) dataSource.deletePet(uid, petId)
    }
}