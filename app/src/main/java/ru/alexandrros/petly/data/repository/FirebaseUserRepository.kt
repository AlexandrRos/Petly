package ru.alexandrros.petly.data.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.alexandrros.petly.data.remote.datasource.FirebaseUserDataSource
import ru.alexandrros.petly.data.remote.mapper.toDomain
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository


class FirebaseUserRepository(
    private val dataSource: FirebaseUserDataSource = FirebaseUserDataSource()
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val dto = dataSource.login(email, password)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val dto = dataSource.register(email, password, name)
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> {
        return dataSource.observeCurrentUser()
            .map { dto -> dto?.toDomain() }
    }

    override suspend fun logout() {
        dataSource.logout()
    }

    override fun getUserById(uid: String): Flow<User?> {
        return dataSource.getUserById(uid)
            .map { dto -> dto?.toDomain() }
    }

    override suspend fun updateSpecialist(uid: String, specialist: String): Result<Unit> {
        return try {
            dataSource.updateSpecialist(uid, specialist)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserName(uid: String, name: String): Result<Unit> {
        return try {
            dataSource.updateUserName(uid, name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserPhoto(uid: String, photoBytes: ByteArray): Result<Unit> {
        return try {
            dataSource.updateUserPhoto(uid, photoBytes)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllSpecialists(): Flow<List<User>> {
        return dataSource.getAllSpecialists()
            .map { dtos -> dtos.map { it.toDomain() } }
    }
}