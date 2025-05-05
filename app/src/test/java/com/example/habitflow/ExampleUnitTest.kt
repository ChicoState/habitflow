package com.example.habitflow

import com.example.habitflow.model.NewHabit
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

class NewHabitTest {
    @Test
    fun constructorTest() {
        val o = NewHabit("", "", "", 0, 0f, "", emptyList(), false, null, "", "", null, null, false, "")
        assertEquals("", o.id)
    }
}