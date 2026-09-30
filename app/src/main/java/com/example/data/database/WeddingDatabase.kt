package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.WeddingDao
import com.example.data.model.ClientSubmission
import com.example.data.model.WeddingEvent
import com.example.data.model.WeddingPhoto

@Database(
    entities = [
        WeddingEvent::class,
        WeddingPhoto::class,
        ClientSubmission::class
    ],
    version = 1,
    exportSchema = false
)
abstract class WeddingDatabase : RoomDatabase() {

    abstract fun weddingDao(): WeddingDao

    companion object {
        @Volatile
        private var INSTANCE: WeddingDatabase? = null

        fun getDatabase(context: Context): WeddingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeddingDatabase::class.java,
                    "wedding_lens_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
