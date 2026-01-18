package com.countdown.widgetproremain.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.countdown.widgetproremain.data.model.CountdownEvent

@Database(entities = [CountdownEvent::class], version = 2, exportSchema = false)
abstract class CountdownDatabase : RoomDatabase() {
    abstract fun countdownDao(): CountdownDao

    companion object {
        @Volatile
        private var Instance: CountdownDatabase? = null

        fun getDatabase(context: Context): CountdownDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, CountdownDatabase::class.java, "countdown_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
