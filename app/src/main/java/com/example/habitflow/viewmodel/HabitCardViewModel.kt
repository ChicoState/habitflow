package com.example.habitflow.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import com.example.habitflow.model.Habit
import com.example.habitflow.model.UserData

class HabitCardViewModel(
    private val habit: Habit,
    private val userData: UserData
) : ViewModel() {

    private var maxSwipe = 200f
    var swipeOffset = mutableFloatStateOf(0f)
    var showDeleteIcon = mutableStateOf(false)

    fun handleHorizontalDrag(
        dragAmount: Float,
        onDragEnd: () -> Unit
    ) {
        swipeOffset.floatValue = (swipeOffset.floatValue + dragAmount).coerceIn(0f, maxSwipe)
        if (swipeOffset.floatValue > maxSwipe * 0.5f) {
            showDeleteIcon.value = true
        } else {
            showDeleteIcon.value = false
        }
        onDragEnd()
    }

    fun getNotificationIcon(): ImageVector {
        return when (userData.promptEntry) {
            true -> Icons.Default.Check
            false -> Icons.Default.Add
        }
    }
}