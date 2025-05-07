package com.example.habitflow.activity

import android.app.Application
import com.github.mikephil.charting.utils.Utils

class HabitFlowApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}