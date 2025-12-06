package se.onemanstudio.playaroundwithai.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PromptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptsHistoryDao
}
