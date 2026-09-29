package com.mcbdone.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import com.mcbdone.app.BuildConfig
import com.mcbdone.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID
import java.util.concurrent.TimeUnit

class SupabaseManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("mcbd_glass_prefs", Context.MODE_PRIVATE)
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val baseUrl = BuildConfig.SUPABASE_URL
    private val anonKey = BuildConfig.SUPABASE_ANON_KEY

    var currentSessionToken: String?
        get() = prefs.getString("access_token", null)
        set(value) = prefs.edit().putString("access_token", value).apply()

    var currentUser: UserProfile?
        get() {
            val raw = prefs.getString("current_user", null) ?: return null
            return try {
                json.decodeFromString<UserProfile>(raw)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value == null) {
                prefs.edit().remove("current_user").remove("access_token").apply()
            } else {
                prefs.edit().putString("current_user", json.encodeToString(value)).apply()
            }
        }

    fun isUserLoggedIn(): Boolean = currentUser != null

    val isCurrentDeveloper: Boolean
        get() = isDeveloperEmail(currentUser?.email)

    fun signOut() {
        prefs.edit().clear().apply()
    }

    /**
     * Authenticates with Supabase using Google ID Token.
     */
    suspend fun exchangeGoogleToken(idToken: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/auth/v1/token?grant_type=id_token"
            val payload = """
                {
                    "provider": "google",
                    "id_token": "$idToken",
                    "client_id": "${BuildConfig.GOOGLE_WEB_CLIENT_ID}"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody("application/json".toMediaType()))
                .build()

            var accessToken: String? = null
            var profile: UserProfile? = null

            try {
                val response = client.newCall(request).execute()
                val body = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    val jsonObj = json.parseToJsonElement(body).jsonObject
                    accessToken = jsonObj["access_token"]?.jsonPrimitive?.content
                    val userObj = jsonObj["user"]?.jsonObject
                    val userId = userObj?.get("id")?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                    val email = userObj?.get("email")?.jsonPrimitive?.content ?: ""
                    val metadata = userObj?.get("user_metadata")?.jsonObject
                    val fullName = metadata?.get("full_name")?.jsonPrimitive?.content
                        ?: metadata?.get("name")?.jsonPrimitive?.content ?: "Member"
                    val avatar = metadata?.get("avatar_url")?.jsonPrimitive?.content
                        ?: "https://lh3.googleusercontent.com/a/default-user=s96-c"

                    val isDev = isDeveloperEmail(email)

                    profile = UserProfile(
                        id = userId,
                        email = email,
                        fullName = fullName,
                        avatarUrl = avatar,
                        rank = if (isDev) "System Architect & Developer" else "Member",
                        status = "online",
                        bio = if (isDev) "Lead Developer & System Architect (Root Access)" else "Active Member"
                    )
                }
            } catch (e: Exception) {
                Log.w("SupabaseManager", "Auth exchange fallback to direct claims", e)
            }

            // Fallback: Decode Google JWT token payload directly
            if (profile == null) {
                val claims = decodeJwtPayload(idToken)
                val sub = claims["sub"]?.jsonPrimitive?.content ?: UUID.randomUUID().toString()
                val uuid = try {
                    UUID.nameUUIDFromBytes(sub.toByteArray()).toString()
                } catch (_: Exception) {
                    UUID.randomUUID().toString()
                }
                val email = claims["email"]?.jsonPrimitive?.content ?: ""
                val fullName = claims["name"]?.jsonPrimitive?.content ?: "Member"
                val picture = claims["picture"]?.jsonPrimitive?.content ?: "https://lh3.googleusercontent.com/a/default-user=s96-c"

                val isDev = isDeveloperEmail(email)

                profile = UserProfile(
                    id = uuid,
                    email = email,
                    fullName = fullName,
                    avatarUrl = picture,
                    rank = if (isDev) "System Architect & Developer" else "Member",
                    status = "online",
                    bio = if (isDev) "Lead Developer & System Architect (Root Access)" else "Active Member"
                )
            }

            currentSessionToken = accessToken ?: anonKey
            currentUser = profile
            upsertProfile(profile)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("SupabaseManager", "Error in exchangeGoogleToken", e)
            Result.failure(e)
        }
    }

    private fun decodeJwtPayload(jwt: String): Map<String, JsonElement> {
        return try {
            val parts = jwt.split(".")
            if (parts.size >= 2) {
                val payloadBytes = Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                val payloadString = String(payloadBytes, Charsets.UTF_8)
                json.parseToJsonElement(payloadString).jsonObject
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun upsertProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles"
            val body = json.encodeToString(profile)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Upsert profile error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ONLY returns real profiles from database. Never injects fake/dummy accounts!
     */
    suspend fun fetchProfiles(): List<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/profiles?select=*&order=updated_at.desc.nullslast"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return@withContext json.decodeFromString<List<UserProfile>>(body)
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Failed to fetch profiles", e)
        }
        emptyList()
    }

    suspend fun fetchChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/channels?select=*&order=created_at.asc"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val channels = json.decodeFromString<List<Channel>>(body)
                    if (channels.isNotEmpty()) {
                        return@withContext channels
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Fetch channels error", e)
        }

        // Seed default channels using icon names (NO EMOJIS)
        val defaults = defaultChannels
        defaults.forEach { ch ->
            createChannel(ch)
        }
        defaults
    }

    suspend fun createChannel(channel: Channel): Result<Channel> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/channels"
            val body = json.encodeToString(channel)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(channel)
                else Result.failure(Exception("Create channel error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteChannel(channelId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isCurrentDeveloper) return@withContext Result.failure(Exception("Permission denied: Developer access required"))
        try {
            val url = "$baseUrl/rest/v1/channels?id=eq.$channelId"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete channel error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchMessages(channelId: String, limit: Int = 100): List<Message> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages?channel_id=eq.$channelId&select=*&order=created_at.asc&limit=$limit"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    return@withContext json.decodeFromString<List<Message>>(body)
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Fetch messages failed for $channelId", e)
        }
        emptyList()
    }

    suspend fun sendMessage(message: Message): Result<Message> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages"
            val body = json.encodeToString(message)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "return=representation")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val respBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val inserted = json.decodeFromString<List<Message>>(respBody).firstOrNull() ?: message
                    Result.success(inserted)
                } else {
                    Result.failure(Exception("Failed to send message: ${response.code}"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/messages?id=eq.$messageId"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Delete message failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Toggles an icon reaction (key: heart, fire, thumb_up, thumb_down, star)
     */
    suspend fun toggleReaction(messageId: String, reactionKey: String, currentReactions: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = mutableMapOf<String, Int>()
            if (!currentReactions.isNullOrBlank()) {
                currentReactions.split(",").forEach { part ->
                    val kv = part.split(":")
                    if (kv.isNotEmpty()) {
                        val k = kv[0].trim()
                        val v = if (kv.size > 1) kv[1].trim().toIntOrNull() ?: 1 else 1
                        if (k.isNotEmpty()) existing[k] = v
                    }
                }
            }

            val cur = existing[reactionKey] ?: 0
            if (cur > 0) {
                existing.remove(reactionKey)
            } else {
                existing[reactionKey] = 1
            }

            val updatedString = existing.entries.joinToString(",") { "${it.key}:${it.value}" }

            val url = "$baseUrl/rest/v1/messages?id=eq.$messageId"
            val payload = """{"reactions":"$updatedString"}"""
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Content-Type", "application/json")
                .patch(payload.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Reaction update failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(status: String, bio: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = currentUser ?: return@withContext Result.failure(Exception("Not logged in"))
        try {
            val updated = user.copy(status = status, bio = bio)
            currentUser = updated
            upsertProfile(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLatestAppVersion(): AppVersionInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "$baseUrl/rest/v1/app_versions?select=*&order=version_code.desc&limit=1"
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string().orEmpty()
                    val list = json.decodeFromString<List<AppVersionInfo>>(body)
                    return@withContext list.firstOrNull()
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseManager", "Failed to fetch app version", e)
        }
        null
    }

    suspend fun publishAppVersion(version: AppVersionInfo): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isCurrentDeveloper) return@withContext Result.failure(Exception("Permission denied: Developer access required"))
        try {
            val url = "$baseUrl/rest/v1/app_versions"
            val body = json.encodeToString(version)
            val request = Request.Builder()
                .url(url)
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer ${currentSessionToken ?: anonKey}")
                .addHeader("Prefer", "resolution=merge-duplicates")
                .addHeader("Content-Type", "application/json")
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) Result.success(Unit)
                else Result.failure(Exception("Publish version error: ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private val defaultChannels = listOf(
        Channel(
            id = "peak-seekers",
            name = "peak seekers",
            topic = "Outdoor adventures, hiking & photography",
            icon = "explore",
            isAnnouncement = false
        ),
        Channel(
            id = "code-cocktails",
            name = "code & cocktails",
            topic = "Developers, design & tech discussions",
            icon = "code",
            isAnnouncement = false
        ),
        Channel(
            id = "room-chat",
            name = "room chat",
            topic = "Live group voice & video hangout",
            icon = "videocam",
            isAnnouncement = false
        ),
        Channel(
            id = "announcements",
            name = "official broadcasts",
            topic = "Community updates and releases",
            icon = "campaign",
            isAnnouncement = true
        )
    )
}
