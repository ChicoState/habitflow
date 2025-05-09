package com.example.habitflow

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.model.NewHabit
import com.example.habitflow.repository.HabitRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class HabitRepositoryInstrumentedTest {

    @Test
    fun getHabitFromFirestore_returnsValidHabit() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val knownHabitId = "OatOkVR6Vf1eC2T03zDM"
        val latch = java.util.concurrent.CountDownLatch(1)
        HabitRepository.getHabitFromFirestore(knownHabitId) { habit ->
            try {
                assertNotNull("Habit should not be null", habit)
                assertEquals("Smoking", habit?.name)
            } finally {
                latch.countDown()
            }
        }
        assertTrue("Callback not called in time", latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }

    @Test
    fun getHabitFromFirestore_returnsNullForNonexistentId() {
        val nonexistentHabitId = "this_id_does_not_exist"
        val latch = java.util.concurrent.CountDownLatch(1)
        HabitRepository.getHabitFromFirestore(nonexistentHabitId) { habit ->
            try {
                assertNull("Habit should be null for nonexistent ID", habit)
            } finally {
                latch.countDown()
            }
        }
        assertTrue("Callback not called in time", latch.await(5, java.util.concurrent.TimeUnit.SECONDS))
    }

    @Test
    fun updateUserHabitList_updatesHabitsAndCleansUp() {
        val latch = CountDownLatch(1)
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)
                val initialData = mapOf("habits" to listOf("initial"))
                userDocRef.set(initialData)
                    .addOnSuccessListener {
                        val method = HabitRepository::class.java.getDeclaredMethod(
                            "updateUserHabitList",
                            FirebaseUser::class.java,
                            List::class.java,
                            Function0::class.java,
                            Function1::class.java
                        )
                        method.isAccessible = true
                        val newHabits = listOf("habitA", "habitB")
                        method.invoke(
                            HabitRepository,
                            user,
                            newHabits,
                            {
                                userDocRef.get().addOnSuccessListener { snapshot ->
                                    val updated = snapshot.get("habits") as? List<*>
                                    assertNotNull(updated)
                                    assertEquals(newHabits, updated)
                                    userDocRef.delete().addOnSuccessListener {
                                        auth.signOut()
                                        latch.countDown()
                                    }.addOnFailureListener {
                                        latch.countDown()
                                    }
                                }
                            },
                            { error: String ->
                                fail("Update failed: $error")
                                latch.countDown()
                            }
                        )
                    }
                    .addOnFailureListener {
                        fail("Initial doc setup failed: ${it.message}")
                        latch.countDown()
                    }
            }
            .addOnFailureListener {
                fail("Anonymous login failed: ${it.message}")
                latch.countDown()
            }
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun loadHabitsFromFirestore_returnsHabitsList_andCleansUp() {
        val latch = CountDownLatch(1)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val email = "testuser@example.com"
        val password = "testpass123"
        val expectedHabits = listOf("habit1", "habit2", "habit3")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)
                userDocRef.set(mapOf("habits" to expectedHabits))
                    .addOnSuccessListener {
                        HabitRepository.loadHabitsFromFirestore(
                            user = user,
                            onSuccess = { habits ->
                                try {
                                    assertEquals(expectedHabits, habits)
                                } finally {
                                    userDocRef.delete().addOnCompleteListener {
                                        auth.signOut()
                                        latch.countDown()
                                    }
                                }
                            },
                            onFailure = { error ->
                                fail("loadHabitsFromFirestore failed: $error")
                                userDocRef.delete().addOnCompleteListener {
                                    auth.signOut()
                                    latch.countDown()
                                }
                            }
                        )
                    }
                    .addOnFailureListener {
                        fail("Failed to create test user document: ${it.message}")
                        latch.countDown()
                    }
            }
            .addOnFailureListener {
                fail("Firebase auth failed: ${it.message}")
                latch.countDown()
            }

        assertTrue("Test timed out", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun createHabitForUser_createsHabitAndUpdatesUserDoc_thenCleansUp() {
        val latch = CountDownLatch(1)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val email = "testuser@example.com"
        val password = "testpass123"
        val context = ApplicationProvider.getApplicationContext<Context>()

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)
                userDocRef.set(mapOf("habits" to listOf<String>()))
                    .addOnSuccessListener {
                        val habit = NewHabit(
                            name = "Test Habit",
                            description = "This is a test",
                            duration = 10,
                            goalAmount = 5f,
                            precision = "day",
                            remindersEnabled = false,
                            category = "Test",
                            frequency = "daily",
                            userDataId = "userdata_test"
                        )
                        HabitRepository.createHabitForUser(
                            context = context,
                            user = user,
                            habit = habit,
                            onSuccess = {
                                userDocRef.get().addOnSuccessListener { snapshot ->
                                    val habitIds = snapshot.get("habits") as? List<*>
                                    assertNotNull(habitIds)
                                    assertTrue(habitIds!!.isNotEmpty())
                                    val newHabitId = habitIds.last() as String
                                    val habitDocRef = db.collection("habits").document(newHabitId)
                                    habitDocRef.get().addOnSuccessListener { habitDoc ->
                                        assertTrue(habitDoc.exists())
                                        assertEquals("Test Habit", habitDoc.getString("name"))
                                        habitDocRef.delete()
                                            .addOnCompleteListener {
                                                userDocRef.set(mapOf("habits" to listOf<String>()))
                                                    .addOnCompleteListener {
                                                        auth.signOut()
                                                        latch.countDown()
                                                    }
                                            }
                                    }
                                }
                            },
                            onFailure = { error ->
                                fail("createHabitForUser failed: $error")
                                latch.countDown()
                            }
                        )
                    }
                    .addOnFailureListener {
                        fail("Failed to reset user doc: ${it.message}")
                        latch.countDown()
                    }
            }
            .addOnFailureListener {
                fail("Login failed: ${it.message}")
                latch.countDown()
            }
        assertTrue("Test timeout", latch.await(15, TimeUnit.SECONDS))
    }

    @Test
    fun deleteHabitsForUser_removesHabitAndReference_thenCleansUp() {
        val latch = CountDownLatch(1)
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val context = ApplicationProvider.getApplicationContext<Context>()
        val email = "testuser@example.com"
        val password = "testpass123"

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user!!
                val userDocRef = db.collection("users").document(user.uid)
                val habitData = mapOf(
                    "name" to "Temp Habit",
                    "description" to "To be deleted",
                    "duration" to 5,
                    "goalAmount" to 1.0f,
                    "precision" to "day",
                    "userDataId" to "userdata",
                    "notificaitonTriggered" to false
                )
                db.collection("habits").add(habitData)
                    .addOnSuccessListener { habitDocRef ->
                        val habitId = habitDocRef.id
                        userDocRef.set(mapOf("habits" to listOf(habitId)))
                            .addOnSuccessListener {
                                HabitRepository.deleteHabitsForUser(
                                    context = context,
                                    user = user,
                                    habitIds = setOf(habitId),
                                    onComplete = {
                                        db.collection("habits").document(habitId).get()
                                            .addOnSuccessListener { deletedHabitDoc ->
                                                assertFalse("Habit document should be deleted", deletedHabitDoc.exists())
                                                userDocRef.get().addOnSuccessListener { userSnapshot ->
                                                    val habits = userSnapshot.get("habits") as? List<*>
                                                    assertTrue("Habit should be removed from user", habits?.contains(habitId) == false)
                                                    userDocRef.set(mapOf("habits" to listOf<String>()))
                                                        .addOnCompleteListener {
                                                            auth.signOut()
                                                            latch.countDown()
                                                        }
                                                }
                                            }
                                    }
                                )
                            }
                    }
                    .addOnFailureListener {
                        fail("Failed to create habit for test: ${it.message}")
                        latch.countDown()
                    }
            }
            .addOnFailureListener {
                fail("Auth failed: ${it.message}")
                latch.countDown()
            }
        assertTrue("Test timed out", latch.await(15, TimeUnit.SECONDS))
    }
}