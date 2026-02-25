package se.onemanstudio.playaroundwithai.data.chat.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import se.onemanstudio.playaroundwithai.data.chat.data.local.converter.SyncStatusConverter
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.PromptsHistoryDao
import se.onemanstudio.playaroundwithai.data.chat.data.local.dao.TokenUsageDao
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.data.chat.data.local.entity.TokenUsageEntity
import se.onemanstudio.playaroundwithai.data.dream.data.local.dao.DreamsDao
import se.onemanstudio.playaroundwithai.data.dream.data.local.entity.DreamEntity

@Database(entities = [PromptEntity::class, TokenUsageEntity::class, DreamEntity::class], version = 5, exportSchema = false)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): PromptsHistoryDao
    abstract fun tokenUsageDao(): TokenUsageDao
    abstract fun dreamsDao(): DreamsDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS token_usage (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        feature TEXT NOT NULL,
                        promptTokens INTEGER NOT NULL,
                        candidateTokens INTEGER NOT NULL,
                        totalTokens INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        dateMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_token_usage_feature ON token_usage (feature)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_token_usage_dateMillis ON token_usage (dateMillis)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS dreams (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        description TEXT NOT NULL,
                        interpretation TEXT NOT NULL,
                        sceneJson TEXT,
                        mood TEXT NOT NULL,
                        timestamp INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
