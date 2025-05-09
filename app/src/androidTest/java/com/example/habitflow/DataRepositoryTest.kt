package com.example.habitflow

import android.os.Handler
import android.os.Looper
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.repository.DataRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import com.github.mikephil.charting.data.Entry

@RunWith(AndroidJUnit4::class)
class DataRepositoryInstrumentedTest {

    @Test
    fun createEmptyUserData_createsDocument_andCleansUp() {
        val latch = CountDownLatch(1)
        val repo = DataRepository()
        repo.createEmptyUserData(
            type = true,
            deadline = "12/31/2025",
            trackingMethod = "manual",
            onSuccess = { userDataId ->
                val db = FirebaseFirestore.getInstance()
                db.collection("userData").document(userDataId).get()
                    .addOnSuccessListener { snapshot ->
                        assertTrue(snapshot.exists())
                        assertEquals("good", snapshot.getString("type"))
                        db.collection("userData").document(userDataId).delete().addOnCompleteListener {
                            latch.countDown()
                        }
                    }
            },
            onFailure = {
                fail("Creation failed: $it")
                latch.countDown()
            }
        )
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun loadUserDataFromFirestore_returnsUserData_andCleansUp() {
        val latch = CountDownLatch(1)
        val repo = DataRepository()
        val db = FirebaseFirestore.getInstance()
        val testDoc = hashMapOf(
            "data" to emptyList<Map<String, Any>>(),
            "createDate" to Timestamp.now(),
            "deadline" to "12/31/2025",
            "lastUpdated" to Timestamp.now(),
            "type" to "bad",
            "trackingMethod" to "auto"
        )

        db.collection("userData").add(testDoc).addOnSuccessListener { docRef ->
            repo.loadUserDataFromFirestore(docRef.id) { result ->
                try {
                    val userData = result.getOrNull()
                    assertNotNull(userData)
                    assertEquals("bad", userData?.type)
                } finally {
                    docRef.delete().addOnCompleteListener { latch.countDown() }
                }
            }
        }
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun updateUserData_addsEntryWithStreak_andCleansUp() = runBlocking {
        val repo = DataRepository()
        val db = FirebaseFirestore.getInstance()
        val latch = CountDownLatch(1)
        val docRef = db.collection("userData").document()
        val initial = hashMapOf(
            "data" to emptyList<Map<String, Any>>(),
            "createDate" to Timestamp.now(),
            "lastUpdated" to Timestamp.now(),
            "type" to "good",
            "trackingMethod" to "manual"
        )
        docRef.set(initial).addOnSuccessListener {
            val entry = mapOf("x" to 1f, "y" to 5f)
            runBlocking {
                repo.updateUserData(docRef.id, entry, didCompleteHabit = true)
                delay(3000) // Give Firestore time to sync
                docRef.get().addOnSuccessListener { snapshot ->
                    val data = snapshot.get("data") as? List<*>
                    assertFalse(data.isNullOrEmpty())
                    assertTrue((data?.last() as Map<*, *>)["streak"] == 1L)
                    docRef.delete().addOnCompleteListener { latch.countDown() }
                }
            }
        }
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun updateLastEntryInFirestore_updatesLastYValue_andCleansUp() {
        val latch = CountDownLatch(1)
        val db = FirebaseFirestore.getInstance()
        val repo = DataRepository()
        val initialEntry = mapOf("x" to 1f, "y" to 3f)
        val doc = mapOf(
            "data" to listOf(initialEntry),
            "createDate" to Timestamp.now(),
            "lastUpdated" to Timestamp.now(),
            "type" to "bad",
            "trackingMethod" to "auto"
        )
        db.collection("userData").add(doc).addOnSuccessListener { docRef ->
            repo.updateLastEntryInFirestore(docRef.id, Entry(1f, 9f))
            Handler(Looper.getMainLooper()).postDelayed({
                docRef.get().addOnSuccessListener { snapshot ->
                    val updated = snapshot.get("data") as List<*>
                    val last = updated.last() as Map<*, *>
                    assertEquals(9f, (last["value"] as Number).toFloat())
                    docRef.delete().addOnCompleteListener { latch.countDown() }
                }
            }, 3000)
        }
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun deleteUserData_deletesDocument() {
        val latch = CountDownLatch(1)
        val repo = DataRepository()
        val db = FirebaseFirestore.getInstance()
        val doc = mapOf(
            "data" to emptyList<Map<String, Any>>(),
            "createDate" to Timestamp.now(),
            "lastUpdated" to Timestamp.now(),
            "type" to "good",
            "trackingMethod" to "auto"
        )
        db.collection("userData").add(doc).addOnSuccessListener { docRef ->
            repo.deleteUserData(docRef.id) { success ->
                assertTrue(success)
                db.collection("userData").document(docRef.id).get()
                    .addOnSuccessListener { snap ->
                        assertFalse(snap.exists())
                        latch.countDown()
                    }
            }
        }
        assertTrue("Test timeout", latch.await(10, TimeUnit.SECONDS))
    }
}