package com.countdown.widgetproremain

import android.app.Application
import com.countdown.widgetproremain.data.local.CountdownDatabase
import com.countdown.widgetproremain.data.repository.CountdownRepository

class CountdownApplication : Application() {
    val database by lazy { CountdownDatabase.getDatabase(this) }
    val repository by lazy { CountdownRepository(database.countdownDao()) }
}
