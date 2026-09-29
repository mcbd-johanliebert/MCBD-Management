package com.mcbdone.app.ui.screens.feed

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.data.model.Channel
import com.mcbdone.app.data.model.UserProfile
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.ui.components.*
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun FeedScreen(
    supabaseManager: SupabaseManager,
    onOpenChatRoom: (channelId: String, channelTitle: String) -> Unit,
    onOpenProfile: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = supabaseManager.currentUser
    val isDeveloper = supabaseManager.isCurrentDeveloper
    val scrollState = rememberScrollState()

    var channels by remember { mutableStateOf<List<Channel>>(emptyList()) }
    var profiles by remember { mutableStateOf<List<UserProfile>>(emptyList()) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                channels = supabaseManager.fetchChannels()
                profiles = supabaseManager.fetchProfiles()
            } catch (_: Exception) {}
            kotlinx.coroutines.delay(4000)
        }
    }

    val realAvatars = profiles.mapNotNull { it.avatarUrl.ifBlank { null } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkContourBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 90.dp)
        ) {
            // Contour Top Bar (Matching Photo 1)
            ContourHeader(
                title = "friendlyhours",
                isBackButton = false,
                onLeftClick = onOpenProfile,
                rightIcon = Icons.Default.Notifications,
                badgeCount = 2,
                onRightClick = {
                    Toast.makeText(context, "Notifications", Toast.LENGTH_SHORT).show()
                },
                subtitle = {
                    StackedAvatarsHeader(
                        avatars = realAvatars,
                        groupTitle = "Code & Cocktails",
                        memberInfo = "${profiles.size.coerceAtLeast(1)} Members",
                        extraCount = 14
                    )
                }
            )

            // Developer Access Banner if logged in as nazmusshakibshihan01@gmail.com
            if (isDeveloper) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0x332563EB),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Root Developer Access Active (${currentUser?.email})",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF93C5FD)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Hero Feed Card (Matching Photo 1)
            Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                HeroFeedCard(
                    title = "Chasing sunsets and mountain tops",
                    tags = listOf("CraftBeer", "Hiking", "Running", "Bouldering"),
                    imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&q=80",
                    onCloseClick = {
                        Toast.makeText(context, "Passed", Toast.LENGTH_SHORT).show()
                    },
                    onStarClick = {
                        Toast.makeText(context, "Superliked!", Toast.LENGTH_SHORT).show()
                    },
                    onShareClick = {
                        Toast.makeText(context, "Shared to room", Toast.LENGTH_SHORT).show()
                    },
                    onLikeClick = {
                        Toast.makeText(context, "Liked! Opening chat...", Toast.LENGTH_SHORT).show()
                        onOpenChatRoom("peak-seekers", "peak seekers")
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Next Peek Card: Peak Seekers (Matching Photo 1 bottom)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = DarkContourSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable {
                        onOpenChatRoom("peak-seekers", "peak seekers")
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StackedAvatarsHeader(
                        avatars = realAvatars,
                        groupTitle = "Peak Seekers",
                        memberInfo = "7 members",
                        extraCount = 4
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Peeking next card top curve (Matching Photo 1 bottom background)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0xFF1E293B))
                    .clickable {
                        onOpenChatRoom("peak-seekers", "peak seekers")
                    }
            ) {
                coil.compose.AsyncImage(
                    model = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=900&q=80",
                    contentDescription = null,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Connected Bottom Navigation Dock (Matching Photo 1)
        ConnectedBottomBar(
            currentTab = 0,
            onTabSelected = { tabIndex ->
                when (tabIndex) {
                    0 -> { /* Current Feed */ }
                    1 -> onOpenChatRoom("code-cocktails", "code & cocktails")
                    2 -> { /* Home / Feed */ }
                    3 -> onOpenChatRoom("peak-seekers", "peak seekers")
                    4 -> onOpenProfile()
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
