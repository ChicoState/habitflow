package com.example.habitflow.model
import com.github.mikephil.charting.data.Entry
import com.google.firebase.Timestamp

data class UserData (
    val userData: List<Entry> = emptyList(),
    val decreasing: Boolean = false,
    var lastDate: Timestamp = Timestamp.now(),
)

