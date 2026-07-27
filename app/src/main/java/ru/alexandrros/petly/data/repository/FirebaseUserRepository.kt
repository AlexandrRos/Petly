package ru.alexandrros.petly.data.repository


import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                // Fetch full user from Firestore
                firestore.collection("users").document(firebaseUser.uid).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val name = doc.getString("name") ?: ""
                            trySend(User(firebaseUser.uid, firebaseUser.email ?: "", name))
                        } else {
                            trySend(User(firebaseUser.uid, firebaseUser.email ?: "", ""))
                        }
                    }
                    .addOnFailureListener { trySend(null) }
            } else {
                trySend(null)
            }
        }
        auth.addAuthStateListener(listener)
        // Emit current state immediately
        listener.onAuthStateChanged(auth)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun logout() {
        auth.signOut()
        // Optionally clear any cached data
    }
}