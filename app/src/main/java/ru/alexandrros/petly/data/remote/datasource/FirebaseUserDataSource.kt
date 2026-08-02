package ru.alexandrros.petly.data.remote.datasource

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.data.remote.model.UserDto
import ru.alexandrros.petly.data.remote.mapper.toBlob
import ru.alexandrros.petly.data.remote.mapper.toBytes

class FirebaseUserDataSource {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): UserDto {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("User ID not found")
        val doc = firestore.collection("users").document(uid).get().await()
        return if (doc.exists()) {
            UserDto(
                uid = uid,
                email = email,
                name = doc.getString("name") ?: "",
                specialist = doc.getString("specialist"),
                photoBytes = doc.getBlob("photoBytes").toBytes()
            )
        } else {
            firestore.collection("users").document(uid)
                .set(mapOf("email" to email, "name" to ""))
                .await()
            UserDto(uid, email, "")
        }
    }

    suspend fun register(email: String, password: String, name: String): UserDto {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = authResult.user?.uid ?: throw Exception("User ID not found")
        val userData = hashMapOf(
            "email" to email,
            "name" to name,
            "specialist" to "None",
            "photoBytes" to null
        )
        firestore.collection("users").document(uid).set(userData).await()
        return UserDto(uid, email, name, "None")
    }

    fun observeCurrentUser(): Flow<UserDto?> = callbackFlow {
        var firestoreListener: ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { authInstance ->
            val firebaseUser = authInstance.currentUser
            if (firebaseUser != null) {
                val docRef = firestore.collection("users").document(firebaseUser.uid)
                firestoreListener?.remove()
                firestoreListener = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("UserDataSource", "Firestore listen error", error)
                        return@addSnapshotListener
                    }
                    val dto = if (snapshot != null && snapshot.exists()) {
                        UserDto(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = snapshot.getString("name") ?: "",
                            specialist = snapshot.getString("specialist"),
                            photoBytes = snapshot.getBlob("photoBytes").toBytes()
                        )
                    } else {
                        UserDto(
                            uid = firebaseUser.uid,
                            email = firebaseUser.email ?: "",
                            name = "",
                            specialist = null
                        )
                    }
                    trySend(dto)
                }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            firestoreListener?.remove()
        }
    }

     fun logout() {
        auth.signOut()
    }

    fun getUserById(uid: String): Flow<UserDto?> = flow {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val dto = UserDto(
                    uid = uid,
                    email = doc.getString("email") ?: "",
                    name = doc.getString("name") ?: "",
                    specialist = doc.getString("specialist"),
                    photoBytes = doc.getBlob("photoBytes").toBytes()
                )
                emit(dto)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("UserDataSource", "getUserById error", e)
            emit(null)
        }
    }

    suspend fun updateSpecialist(uid: String, specialist: String) {
        firestore.collection("users").document(uid)
            .update("specialist", specialist)
            .await()
    }

    suspend fun updateUserName(uid: String, name: String) {
        firestore.collection("users").document(uid)
            .update("name", name)
            .await()
    }

    suspend fun updateUserPhoto(uid: String, photoBytes: ByteArray) {
        firestore.collection("users").document(uid)
            .update("photoBytes", photoBytes.toBlob())
            .await()
    }
}