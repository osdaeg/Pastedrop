package com.daniel.pastedrop.data.local

import androidx.room.*
import com.daniel.pastedrop.domain.model.Snippet
import kotlinx.coroutines.flow.Flow

// ── Entity ────────────────────────────────────────────────────────────────────

@Entity(tableName = "snippets")
data class SnippetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serverId: String?,
    val title: String?,
    val content: String,
    val language: String,
    val ttlSeconds: Int?,
    val createdAt: String,
    val pendingSync: Boolean = true,
    val pendingDelete: Boolean = false
)

// ── DAO ───────────────────────────────────────────────────────────────────────

@Dao
interface SnippetDao {

    @Query("SELECT * FROM snippets WHERE pendingDelete = 0 ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SnippetEntity>>

    @Query("SELECT * FROM snippets WHERE pendingSync = 1 OR pendingDelete = 1")
    suspend fun getPending(): List<SnippetEntity>

    @Query("SELECT COUNT(*) FROM snippets WHERE pendingSync = 1 OR pendingDelete = 1")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snippet: SnippetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(snippets: List<SnippetEntity>)

    @Query("DELETE FROM snippets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM snippets WHERE pendingSync = 0 AND pendingDelete = 0")
    suspend fun deleteAllSynced()

    @Query("UPDATE snippets SET pendingSync = 0, serverId = :serverId WHERE id = :id")
    suspend fun markSynced(id: Long, serverId: String)

    @Query("UPDATE snippets SET pendingDelete = 1 WHERE id = :id")
    suspend fun markPendingDelete(id: Long)
}

// ── Database ──────────────────────────────────────────────────────────────────

@Database(entities = [SnippetEntity::class], version = 1, exportSchema = false)
abstract class PasteDropDatabase : RoomDatabase() {
    abstract fun snippetDao(): SnippetDao
}

// ── Mappers ───────────────────────────────────────────────────────────────────

fun SnippetEntity.toDomain() = Snippet(
    id, title, content, language, ttlSeconds, createdAt, pendingSync, pendingDelete, serverId
)
