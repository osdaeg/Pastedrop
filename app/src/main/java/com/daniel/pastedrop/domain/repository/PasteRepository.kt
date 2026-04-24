package com.daniel.pastedrop.domain.repository

import com.daniel.pastedrop.data.local.*
import com.daniel.pastedrop.data.remote.*
import com.daniel.pastedrop.domain.model.Snippet
import com.daniel.pastedrop.domain.model.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasteRepository @Inject constructor(
    private val dao: SnippetDao,
    private val apiProvider: ApiProvider
) {
    fun observeAll(): Flow<List<Snippet>> = dao.observeAll().map { it.map(SnippetEntity::toDomain) }

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    /** Trae todos los pastes del servidor y los merge con los pendientes locales */
    suspend fun refreshFromServer() {
        val api = apiProvider.apiOrNull() ?: return
        val remote = runCatching { api.getPastes().bodyOrThrow() }.getOrNull() ?: return
        val pending = dao.getPending()
        val remoteIds = remote.map { it.id }.toSet()
        // Borra los sincronizados (los pendientes se mantienen)
        dao.deleteAllSynced()
        // Inserta todo lo que vino del servidor
        dao.upsertAll(remote.map { it.toEntity() })
        // Restaura los pendientes que no están en el servidor todavía
        pending.filter { it.serverId !in remoteIds }.forEach { dao.upsert(it) }
    }

    suspend fun addSnippet(
        title: String?,
        content: String,
        language: String,
        ttlSeconds: Int?
    ): Result<Unit> = runCatching {
        val now = Instant.now().toString()
        val api = apiProvider.apiOrNull()
        if (api != null) {
            val created = api.createPaste(CreatePasteBody(title, content, language, ttlSeconds)).bodyOrThrow()
            dao.upsert(created.toEntity())
        } else {
            dao.upsert(SnippetEntity(
                serverId = null, title = title, content = content,
                language = language, ttlSeconds = ttlSeconds,
                createdAt = now, pendingSync = true
            ))
        }
    }

    suspend fun deleteSnippet(snippet: Snippet): Result<Unit> = runCatching {
        val api = apiProvider.apiOrNull()
        when {
            snippet.serverId != null && api != null -> {
                api.deletePaste(snippet.serverId)
                dao.deleteById(snippet.id)
            }
            snippet.serverId != null -> dao.markPendingDelete(snippet.id)
            else -> dao.deleteById(snippet.id)
        }
    }

    suspend fun syncPending(): SyncResult {
        val api = apiProvider.apiOrNull() ?: return SyncResult.Error("Sin conexión")
        val pending = dao.getPending()
        var synced = 0
        for (entity in pending) {
            try {
                when {
                    entity.pendingDelete && entity.serverId != null -> {
                        api.deletePaste(entity.serverId)
                        dao.deleteById(entity.id)
                        synced++
                    }
                    entity.pendingDelete -> {
                        dao.deleteById(entity.id)
                        synced++
                    }
                    entity.pendingSync && entity.serverId == null -> {
                        val created = api.createPaste(
                            CreatePasteBody(entity.title, entity.content, entity.language, entity.ttlSeconds)
                        ).bodyOrThrow()
                        dao.markSynced(entity.id, created.id)
                        synced++
                    }
                }
            } catch (_: Exception) { }
        }
        return SyncResult.Success(synced)
    }
}

fun PasteDto.toEntity() = SnippetEntity(
    serverId = id, title = title, content = content,
    language = language, ttlSeconds = ttlSeconds,
    createdAt = createdAt, pendingSync = false
)
