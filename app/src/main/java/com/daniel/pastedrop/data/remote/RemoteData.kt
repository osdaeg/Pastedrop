package com.daniel.pastedrop.data.remote

import com.daniel.pastedrop.data.local.SettingsDataStore
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// ── Adapters ──────────────────────────────────────────────────────────────────

// SQLite devuelve 0/1 para booleanos; este adapter los acepta también
object NumberToBooleanAdapter {
    @FromJson fun fromJson(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER  -> reader.nextInt() != 0
            else -> { reader.skipValue(); false }
        }
    }
    @ToJson fun toJson(writer: JsonWriter, value: Boolean) { writer.value(value) }
}

// ── DTOs ──────────────────────────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class PasteDto(
    val id: String,
    val title: String?,
    val content: String,
    val language: String,
    @Json(name = "ttl_seconds") val ttlSeconds: Int?,
    @Json(name = "created_at") val createdAt: String,
    val archived: Boolean,
    val views: Int
)

@JsonClass(generateAdapter = true)
data class CreatePasteBody(
    val title: String?,
    val content: String,
    val language: String,
    @Json(name = "ttl_seconds") val ttlSeconds: Int?
)

// ── Interface ─────────────────────────────────────────────────────────────────

interface PasteApi {
    @GET("api/pastes")
    suspend fun getPastes(): Response<List<PasteDto>>

    @POST("api/pastes")
    suspend fun createPaste(@Body body: CreatePasteBody): Response<PasteDto>

    @DELETE("api/pastes/{id}")
    suspend fun deletePaste(@Path("id") id: String): Response<Unit>
}

// ── Provider ──────────────────────────────────────────────────────────────────

@Singleton
class ApiProvider @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) {
    private var cachedApi: PasteApi? = null
    private var cachedUrl: String = ""

    suspend fun api(): PasteApi {
        val url = settingsDataStore.settings.first().serverUrl
        if (cachedApi == null || url != cachedUrl) {
            cachedUrl = url
            cachedApi = buildApi(url)
        }
        return cachedApi!!
    }

    suspend fun apiOrNull(): PasteApi? = try {
        api().also { it.getPastes() }
    } catch (e: ConnectException)      { null }
      catch (e: SocketTimeoutException) { null }
      catch (e: java.net.UnknownHostException) { null }
      catch (_: Exception) { api() }

    private fun buildApi(baseUrl: String): PasteApi {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val moshi = Moshi.Builder()
            .add(NumberToBooleanAdapter)
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(PasteApi::class.java)
    }
}

fun <T> Response<T>.bodyOrThrow(): T =
    if (isSuccessful) body()!! else error("HTTP ${code()}")
