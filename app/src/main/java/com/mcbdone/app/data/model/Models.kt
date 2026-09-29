package com.mcbdone.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun isDeveloperEmail(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    val trimmed = email.trim().lowercase()
    return trimmed == "nazmusshakibshihan01@gmail.com" || trimmed == "nazmusshakibshihan@gmail.com"
}

@Serializable
data class UserProfile(
    val id: String,
    val email: String = "",
    @SerialName("full_name") val fullName: String = "Member",
    @SerialName("avatar_url") val avatarUrl: String = "",
    val rank: String = "Member",
    val status: String = "online", // "online", "away", "busy", "offline"
    val bio: String = "Active Community Member",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    val isDeveloper: Boolean
        get() = isDeveloperEmail(email)
}

@Serializable
data class Channel(
    val id: String,
    val name: String,
    val topic: String? = null,
    val icon: String = "chat", // Icon name identifier (NO EMOJIS)
    @SerialName("is_announcement") val isAnnouncement: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    @SerialName("channel_id") val channelId: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("user_name") val userName: String = "Member",
    @SerialName("user_avatar") val userAvatar: String? = null,
    val content: String = "",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("secondary_image_url") val secondaryImageUrl: String? = null,
    @SerialName("audio_duration") val audioDuration: String? = null, // e.g. "2:19"
    @SerialName("reply_to_id") val replyToId: String? = null,
    @SerialName("reply_to_content") val replyToContent: String? = null,
    @SerialName("reply_to_sender") val replyToSender: String? = null,
    val reactions: String? = null, // "heart:3,fire:1,thumb_up:2" (NO EMOJIS, pure icon identifiers)
    @SerialName("created_at") val createdAt: String? = null
) {
    val isVoiceMessage: Boolean
        get() = !audioDuration.isNullOrBlank()

    fun parseReactions(): Map<String, Int> {
        if (reactions.isNullOrBlank()) return emptyMap()
        val result = mutableMapOf<String, Int>()
        try {
            reactions.split(",").forEach { part ->
                val kv = part.split(":")
                if (kv.isNotEmpty()) {
                    val key = kv[0].trim()
                    val count = if (kv.size > 1) kv[1].trim().toIntOrNull() ?: 1 else 1
                    if (key.isNotEmpty()) result[key] = count
                }
            }
        } catch (_: Exception) {}
        return result
    }
}

// Icon based reaction definition (NO EMOJIS)
enum class ReactionIcon(val key: String, val icon: ImageVector, val tint: Color) {
    HEART("heart", Icons.Default.Favorite, Color(0xFFEF4444)),
    FIRE("fire", Icons.Default.Whatshot, Color(0xFFF97316)),
    THUMB_UP("thumb_up", Icons.Default.ThumbUp, Color(0xFF10B981)),
    THUMB_DOWN("thumb_down", Icons.Default.ThumbDown, Color(0xFF64748B)),
    STAR("star", Icons.Default.Star, Color(0xFFF59E0B));

    companion object {
        fun fromKey(key: String): ReactionIcon? = entries.find { it.key.equals(key, ignoreCase = true) }
    }
}

enum class UserStatus(val title: String, val colorHex: Long) {
    ONLINE("Online", 0xFF10B981),
    AWAY("Away", 0xFFF59E0B),
    BUSY("Busy", 0xFFEF4444),
    OFFLINE("Offline", 0xFF94A3B8)
}

data class FeedPost(
    val id: String,
    val title: String,
    val groupName: String,
    val memberCount: Int,
    val imageUrl: String,
    val tags: List<String>,
    val memberAvatars: List<String>
)

@Serializable
data class AppVersionInfo(
    val id: String = "latest",
    @SerialName("version_code") val versionCode: Int = 1,
    @SerialName("version_name") val versionName: String = "1.0.0",
    @SerialName("min_supported_version") val minSupportedVersion: Int = 1,
    @SerialName("apk_url") val apkUrl: String = "",
    @SerialName("github_url") val githubUrl: String? = "https://github.com/mcbd-johanliebert/MCBD-Management",
    @SerialName("release_notes") val releaseNotes: String? = null,
    @SerialName("is_mandatory") val isMandatory: Boolean = true,
    @SerialName("updated_at") val updatedAt: String? = null
) {
    fun isUpdateAvailable(currentCode: Int): Boolean = versionCode > currentCode

    fun isStrictUpdateRequired(currentCode: Int): Boolean =
        (minSupportedVersion > currentCode) || (isMandatory && versionCode > currentCode)
}
