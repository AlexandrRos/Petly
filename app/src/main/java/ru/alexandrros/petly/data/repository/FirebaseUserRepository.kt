package ru.alexandrros.petly.data.repository


import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import ru.alexandrros.petly.domain.model.User
import ru.alexandrros.petly.domain.repository.UserRepository

class FirebaseUserRepository : UserRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val firestore = FirebaseFirestore.getInstance()

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID not found")
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val name = document.getString("name") ?: ""
                Result.success(User(uid, email, name))
            } else {
                val user = User(uid, email, "")
                firestore.collection("users").document(uid).set(
                    mapOf("email" to email, "name" to "")
                ).await()
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: throw Exception("User ID not found")
            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "login" to name,
                "phone" to "",
                "pets" to emptyList<String>(),
                "requests" to emptyList<String>(),
                "specialist" to "None"
            )
            firestore.collection("users").document(uid).set(userData).await()
            Result.success(User(uid, email, name))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        var snapshotListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { authInstance ->
            val firebaseUser = authInstance.currentUser
            if (firebaseUser != null) {
                val docRef = firestore.collection("users").document(firebaseUser.uid)

                // Start listening to Firestore document changes
                snapshotListener?.remove()
                snapshotListener = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        return@addSnapshotListener
                    }
                    if (snapshot != null && snapshot.exists()) {
                        val name = snapshot.getString("name") ?: ""
                        val specialist = snapshot.getString("specialist")
                        trySend(
                            User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = name,
                                specialist = specialist
                            )
                        )
                    } else {
                        trySend(
                            User(
                                uid = firebaseUser.uid,
                                email = firebaseUser.email ?: "",
                                name = "",
                                specialist = null
                            )
                        )
                    }
                }
            } else {
                trySend(null)
            }
        }

        auth.addAuthStateListener(authListener)

        awaitClose {
            // Clean up both listeners
            auth.removeAuthStateListener(authListener)
            snapshotListener?.remove()
        }
    }


    override suspend fun logout() {
        auth.signOut()
    }

    override fun getUserById(uid: String): Flow<User?> = flow {
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val name = doc.getString("name") ?: ""
                val specialist = doc.getString("specialist")
                emit(User(uid, email = doc.getString("email") ?: "", name = name, specialist = specialist))
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("FirebaseUserRepo", "getUserById error", e)
            emit(null)
        }
    }

    override suspend fun updateSpecialist(uid: String, specialist: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .update("specialist", specialist)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseUserRepo", "Update specialist error", e)
            Result.failure(e)
        }
    }
}