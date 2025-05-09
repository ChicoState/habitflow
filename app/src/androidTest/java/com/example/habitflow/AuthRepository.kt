package com.example.habitflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.junit.Test
import java.util.concurrent.CountDownLatch
import org.junit.Assert.fail
import java.util.concurrent.TimeUnit
import com.example.habitflow.model.User
import org.junit.Assert.assertFalse


@RunWith(AndroidJUnit4::class)
class AuthRepositoryInstrumentedTest {

    @Test
    fun signUp_createsUserAndUserDocument_thenCleansUp() {
        runBlocking {
            val repo = AuthRepository()
            val db = FirebaseFirestore.getInstance()
            val email = "signup_test_${System.currentTimeMillis()}@example.com"
            val password = "testpass123"
            val result = repo.signUp(email, password)
            val uid = result.getOrNull()
            assertNotNull(uid)
            val doc = db.collection("users").document(uid!!).get().await()
            assertTrue(doc.exists())
            assertEquals(email, doc.getString("email"))
            FirebaseAuth.getInstance().currentUser?.delete()
            db.collection("users").document(uid).delete()
        }
    }

    @Test
    fun login_signsInExistingUser() = runBlocking {
        val email = "testuser@example.com"
        val password = "testpass123"
        val repo = AuthRepository()
        val result = repo.login(email, password)
        assertTrue(result.isSuccess)
    }

    @Test
    fun saveProfileData_writesUserFieldsCorrectly() = runBlocking {
        val repo = AuthRepository()
        val email = "profile_test_${System.currentTimeMillis()}@example.com"
        val password = "testpass123"
        val db = FirebaseFirestore.getInstance()
        val uid = repo.signUp(email, password).getOrNull()
        assertNotNull(uid)
        val latch = CountDownLatch(1)
        repo.saveProfileData(uid!!, "Test Name", 30, "Nonbinary", onSuccess = {
            db.collection("users").document(uid).get()
                .addOnSuccessListener {
                    assertEquals("Test Name", it.getString("name"))
                    assertEquals(30L, it.getLong("age"))
                    assertEquals("Nonbinary", it.getString("gender"))
                    FirebaseAuth.getInstance().currentUser?.delete()
                    db.collection("users").document(uid).delete()
                    latch.countDown()
                }
        }, onFailure = {
            fail("Failed: ${it.message}")
            latch.countDown()
        })
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun fetchUserData_returnsCorrectUserObject() = runBlocking {
        val email = "fetch_test_${System.currentTimeMillis()}@example.com"
        val password = "testpass123"
        val repo = AuthRepository()
        val db = FirebaseFirestore.getInstance()
        val uid = repo.signUp(email, password).getOrNull()
        assertNotNull(uid)
        val latch = CountDownLatch(1)
        repo.saveProfileData(uid!!, "Fetch Test", 21, "Other", onSuccess = {
            repo.fetchUserData { user ->
                assertNotNull(user)
                assertEquals("Fetch Test", user?.name)
                assertEquals("Other", user?.gender)
                FirebaseAuth.getInstance().currentUser?.delete()
                db.collection("users").document(uid).delete()
                latch.countDown()
            }
        }, onFailure = {
            fail("Save failed: ${it.message}")
            latch.countDown()
        })
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun updateUserData_overwritesUserDocument() = runBlocking {
        val email = "update_test_${System.currentTimeMillis()}@example.com"
        val password = "testpass123"
        val repo = AuthRepository()
        val db = FirebaseFirestore.getInstance()
        val uid = repo.signUp(email, password).getOrNull()
        assertNotNull(uid)
        val latch = CountDownLatch(1)
        val updatedUser = User(
            name = "Updated Name",
            age = 99,
            gender = "Alien",
            email = email,
            habits = listOf("habitX"),
            pastHabits = listOf("habitY")
        )
        repo.updateUserData(updatedUser,
            onSuccess = {
                db.collection("users").document(uid!!).get()
                    .addOnSuccessListener {
                        assertEquals("Updated Name", it.getString("name"))
                        assertEquals(99L, it.getLong("age"))
                        FirebaseAuth.getInstance().currentUser?.delete()
                        db.collection("users").document(uid).delete()
                        latch.countDown()
                    }
            },
            onFailure = {
                fail("Update failed: ${it.message}")
                latch.countDown()
            })
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun deleteUserAccount_deletesAuthUserAndFirestoreDoc() = runBlocking {
        val email = "delete_test_${System.currentTimeMillis()}@example.com"
        val password = "testpass123"
        val repo = AuthRepository()
        val db = FirebaseFirestore.getInstance()
        val uid = repo.signUp(email, password).getOrNull()
        assertNotNull(uid)
        val latch = CountDownLatch(1)
        repo.deleteUserAccount(
            onSuccess = {
                db.collection("users").document(uid!!).get()
                    .addOnSuccessListener {
                        assertFalse("User doc should be deleted", it.exists())
                        latch.countDown()
                    }
            },
            onFailure = {
                fail("Deletion failed: ${it.message}")
                latch.countDown()
            }
        )
        assertTrue("Test timeout", latch.await(15, TimeUnit.SECONDS))
    }
}