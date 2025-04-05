package com.example.habitflow.repository

import com.example.habitflow.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.AuthResult
import kotlinx.coroutines.tasks.await

class AuthRepository(
	private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
	private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
	suspend fun signUp(email: String, password: String): Result<String> {
		return try {
			val result: AuthResult = auth.createUserWithEmailAndPassword(email, password).await()
			val user = result.user ?: return Result.failure(Exception("User creation failed"))

			val userData = hashMapOf(
				"email" to user.email.orEmpty(),
				"name" to "",
				"age" to 0,
				"gender" to ""
			)

			db.collection("users").document(user.uid).set(userData).await()
			Result.success(user.uid)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	suspend fun login(email: String, password: String): Result<Unit> {
		return try {
			auth.signInWithEmailAndPassword(email, password).await()
			Result.success(Unit)
		} catch (e: Exception) {
			Result.failure(e)
		}
	}

	fun saveProfileData(
		uid: String,
		name: String,
		age: Int,
		gender: String,
		onSuccess: () -> Unit,
		onFailure: (Throwable) -> Unit
	) {
		val db = FirebaseFirestore.getInstance()
		val userDoc = db.collection("users").document(uid)

		val profileData = mapOf(
			"name" to name,
			"age" to age,
			"gender" to gender
		)

		userDoc.set(profileData)
			.addOnSuccessListener { onSuccess() }
			.addOnFailureListener { e -> onFailure(e) }
	}

	fun fetchUserData(onComplete: (User?) -> Unit) {
		val user = auth.currentUser ?: return onComplete(null)
		db.collection("users").document(user.uid).get()
			.addOnSuccessListener { doc ->
				val userObj = doc.toObject(User::class.java)
				onComplete(userObj?.copy(email = user.email ?: ""))
			}
			.addOnFailureListener { onComplete(null) }
	}

	fun updateUserData(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
		val uid = auth.currentUser?.uid ?: return
		db.collection("users").document(uid).set(user)
			.addOnSuccessListener { onSuccess() }
			.addOnFailureListener { onFailure(it) }
	}

	fun deleteUserAccount(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
		val user = auth.currentUser
		if (user != null) {
			db.collection("users").document(user.uid).delete()
				.addOnSuccessListener {
					user.delete()
						.addOnSuccessListener { onSuccess() }
						.addOnFailureListener { onFailure(it) }
				}
				.addOnFailureListener { onFailure(it) }
		} else {
			onFailure(Exception("No user signed in"))
		}
	}
}