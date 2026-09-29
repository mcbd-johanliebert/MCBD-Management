package com.mcbdone.app.ui.screens.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mcbdone.app.data.model.Message
import com.mcbdone.app.data.model.UserProfile
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.ui.components.*
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ChatRoomScreen(
    channelId: String,
    channelTitle: String,
    supabaseManager: SupabaseManager,
    onBackClick: () -> Unit,
    onOpenCall: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = supabaseManager.currentUser
    val isDeveloper = supabaseManager.isCurrentDeveloper
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var selectedMessageForReaction by remember { mutableStateOf<Message?>(null) }

    suspend fun loadMessages() {
        try {
            val list = supabaseManager.fetchMessages(channelId)
            messages = list
        } catch (_: Exception) {}
    }

    LaunchedEffect(channelId) {
        profiles = supabaseManager.fetchProfiles()
        loadMessages()
        while (true) {
            delay(2500)
            loadMessages()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val realAvatars = profiles.mapNotNull { it.avatarUrl.ifBlank { null } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        // Contour Wave Header (Matching Photo 2)
        ContourHeader(
            title = channelTitle,
            isBackButton = true,
            onLeftClick = onBackClick,
            rightIcon = Icons.Default.Videocam,
            badgeCount = null,
            onRightClick = onOpenCall,
            subtitle = {
                StackedAvatarsHeader(
                    avatars = realAvatars,
                    groupTitle = "",
                    memberInfo = "9 People",
                    isOnlineState = true,
                    extraCount = 14
                )
            }
        )

        // Chat Message Stream
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Seed visual baseline matching Photo 2 if channel has no messages yet
            if (messages.isEmpty()) {
                item {
                    // Incoming message with heart reaction
                    PhotoStyleMessageBubble(
                        message = Message(
                            id = "seed-1",
                            channelId = channelId,
                            userName = "Sarah",
                            userAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&q=80",
                            content = "Please provide more details about your request",
                            reactions = "heart:1",
                            createdAt = "7:55 PM"
                        ),
                        isCurrentUser = false,
                        onReactionClick = {},
                        onLongPress = {}
                    )
                }

                item {
                    // Audio voice note bubble (Photo 2)
                    AudioVoiceMessageBubble(
                        duration = "2:19",
                        createdAt = "7:57 PM"
                    )
                }

                item {
                    // Media gallery bubble (Photo 2)
                    MediaGalleryBubble(
                        headerText = "Here's what I created:",
                        image1Url = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80",
                        image2Url = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&q=80",
                        createdAt = "7:59 PM",
                        onReactionClick = { rKey ->
                            Toast.makeText(context, "Reaction: $rKey", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                item {
                    // Outgoing message (Photo 2)
                    PhotoStyleMessageBubble(
                        message = Message(
                            id = "seed-3",
                            channelId = channelId,
                            userName = currentUser?.fullName ?: "Me",
                            content = "I like it! Let's make in another style",
                            createdAt = "8:12 PM"
                        ),
                        isCurrentUser = true,
                        onReactionClick = {},
                        onLongPress = {}
                    )
                }
            } else {
                items(messages, key = { it.id }) { msg ->
                    val isMe = msg.userId == currentUser?.id ||
                            (currentUser?.email?.isNotBlank() == true && msg.userName.equals(currentUser.fullName, ignoreCase = true))

                    if (msg.isVoiceMessage) {
                        AudioVoiceMessageBubble(
                            duration = msg.audioDuration ?: "1:30",
                            createdAt = msg.createdAt
                        )
                    } else if (!msg.imageUrl.isNullOrBlank() && !msg.secondaryImageUrl.isNullOrBlank()) {
                        MediaGalleryBubble(
                            headerText = msg.content.ifBlank { "Shared photos:" },
                            image1Url = msg.imageUrl,
                            image2Url = msg.secondaryImageUrl,
                            createdAt = msg.createdAt,
                            onReactionClick = { key ->
                                coroutineScope.launch {
                                    supabaseManager.toggleReaction(msg.id, key, msg.reactions)
                                    loadMessages()
                                }
                            }
                        )
                    } else {
                        PhotoStyleMessageBubble(
                            message = msg,
                            isCurrentUser = isMe,
                            canDelete = isDeveloper || isMe,
                            onDelete = {
                                coroutineScope.launch {
                                    supabaseManager.deleteMessage(msg.id)
                                    loadMessages()
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onReactionClick = { key ->
                                coroutineScope.launch {
                                    supabaseManager.toggleReaction(msg.id, key, msg.reactions)
                                    loadMessages()
                                }
                            },
                            onLongPress = {
                                selectedMessageForReaction = msg
                            }
                        )
                    }
                }
            }
        }

        // Photo 2 Bottom Input Bar
        PhotoStyleInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                val toSend = inputText.trim()
                if (toSend.isNotBlank()) {
                    val newMsg = Message(
                        id = UUID.randomUUID().toString(),
                        channelId = channelId,
                        userId = currentUser?.id,
                        userName = currentUser?.fullName ?: "Member",
                        userAvatar = currentUser?.avatarUrl,
                        content = toSend,
                        createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }.format(java.util.Date())
                    )
                    messages = messages + newMsg
                    inputText = ""
                    coroutineScope.launch {
                        supabaseManager.sendMessage(newMsg)
                        loadMessages()
                    }
                }
            },
            onAddMedia = {
                // Send dual-photo gallery matching Photo 2
                val mediaMsg = Message(
                    id = UUID.randomUUID().toString(),
                    channelId = channelId,
                    userId = currentUser?.id,
                    userName = currentUser?.fullName ?: "Member",
                    userAvatar = currentUser?.avatarUrl,
                    content = "Here's what I created:",
                    imageUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400&q=80",
                    secondaryImageUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=400&q=80",
                    reactions = "fire:1",
                    createdAt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).apply {
                        timeZone = java.util.TimeZone.getTimeZone("UTC")
                    }.format(java.util.Date())
                )
                messages = messages + mediaMsg
                coroutineScope.launch {
                    supabaseManager.sendMessage(mediaMsg)
                    loadMessages()
                }
            }
        )
    }

    // Reaction Bar Dialog (Pure Icons, NO EMOJIS)
    if (selectedMessageForReaction != null) {
        val target = selectedMessageForReaction!!
        IconReactionsBar(
            onSelectReaction = { key ->
                coroutineScope.launch {
                    supabaseManager.toggleReaction(target.id, key, target.reactions)
                    loadMessages()
                }
            },
            onDismiss = { selectedMessageForReaction = null }
        )
    }
}
