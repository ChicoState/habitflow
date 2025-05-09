package com.example.habitflow

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.habitflow.repository.HabitRepository

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.habitflow", appContext.packageName)
    }
}

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

}