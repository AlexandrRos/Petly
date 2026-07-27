package ru.alexandrros.petly.data.repository


import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
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
            // Fetch user data from Firestore
            val document = firestore.collection("users").document(uid).get().await()
            if (document.exists()) {
                val name = document.getString("name") ?: ""
                Result.success(User(uid, email, name))
            } else {
                // In case Firestore document is missing, create a minimal one
                val user = User(uid, email, "")
                firestore.collection("users").document(uid).set(mapOf(
                    "email" to email,
                    "name" to ""
                )).await()
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
            // Create Firestore user document
            val userData = hashMapOf(
                "email" to email,
                "name" to name,
                "login" to name,        // you can change this to a separate username later
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
}