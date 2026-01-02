package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity

@Database(entities = [PromptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun promptDao(): PromptsHistoryDao
}
