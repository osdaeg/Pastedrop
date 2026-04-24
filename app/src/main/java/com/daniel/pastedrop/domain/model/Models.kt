package com.daniel.pastedrop.domain.model

data class Snippet(
    val id: Long,
    val title: String?,
    val content: String,
    val language: String,
    val ttlSeconds: Int?,
    val createdAt: String,
    val pendingSync: Boolean = false,
    val pendingDelete: Boolean = false,
    val serverId: String? = null   // ID del servidor (string hex) una vez sincronizado
)

sealed class SyncResult {
    data class Success(val synced: Int) : SyncResult()
    data class Error(val message: String) : SyncResult()
}
