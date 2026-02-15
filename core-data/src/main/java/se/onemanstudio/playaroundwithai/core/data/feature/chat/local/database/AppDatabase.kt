package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.converter.SyncStatusConverter

@Database(entities = [PromptEntity::class], version = 3, exportSchema = false)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): PromptsHistoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE prompt_history ADD COLUMN firestoreDocId TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_prompt_history_syncStatus ON prompt_history (syncStatus)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_prompt_history_firestoreDocId ON prompt_history (firestoreDocId)")
            }
        }
    }
}
