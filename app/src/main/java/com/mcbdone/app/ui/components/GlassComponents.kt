package com.mcbdone.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.mcbdone.app.data.model.Message
import com.mcbdone.app.data.model.ReactionIcon
import com.mcbdone.app.ui.theme.*

// =========================================================================
// 1. TOP CONTOUR WAVE HEADER (MATCHING PHOTOS 1, 2, 3)
// =========================================================================
@Composable
fun ContourHeader(
    title: String,
    modifier: Modifier = Modifier,
    isBackButton: Boolean = false,
    onLeftClick: () -> Unit = {},
    rightIcon: ImageVector = Icons.Default.Notifications,
    badgeCount: Int? = null,
    onRightClick: () -> Unit = {},
    subtitle: @Composable (ColumnScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(DarkContourBg)
            .drawBehind {
                // Draw elegant subtle organic wave lines matching the screenshot
                val w = size.width
                val h = size.height

                val path1 = Path().apply {
                    moveTo(0f, h * 0.4f)
                    cubicTo(w * 0.25f, h * 0.15f, w * 0.75f, h * 0.6f, w, h * 0.35f)
                }
                drawPath(path1, color = Color(0x1838BDF8), style = Stroke(width = 1.5f))

                val path2 = Path().apply {
                    moveTo(0f, h * 0.75f)
                    cubicTo(w * 0.35f, h * 0.95f, w * 0.65f, h * 0.5f, w, h * 0.8f)
                }
                drawPath(path2, color = Color(0x1438BDF8), style = Stroke(width = 1.5f))
            }
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Main Top Bar Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Circular White Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape, ambientColor = Color(0x40000000))
                        .clip(CircleShape)
                        .background(SurfacePureWhite, CircleShape)
                        .clickable(onClick = onLeftClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBackButton) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Menu,
                        contentDescription = "Menu/Back",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center Lowercase Clean Title
                Text(
                    text = title.lowercase(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextLight,
                    fontSize = 20.sp
                )

                // Right Circular White Action Button
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(6.dp, CircleShape, ambientColor = Color(0x40000000))
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .clickable(onClick = onRightClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = rightIcon,
                            contentDescription = "Action",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Notification Badge
                    if (badgeCount != null && badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .offset(x = 2.dp, y = (-2).dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(NotificationOrange, CircleShape)
                                .border(1.5.dp, DarkContourBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Subtitle / Stacked Avatars Row
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(14.dp))
                subtitle()
            }
        }
    }
}

// =========================================================================
// 2. STACKED AVATARS SUB-HEADER COMPONENT
// =========================================================================
@Composable
fun StackedAvatarsHeader(
    avatars: List<String>,
    groupTitle: String,
    memberInfo: String,
    modifier: Modifier = Modifier,
    isOnlineState: Boolean = false,
    extraCount: Int = 14
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Overlapping Avatars
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val displayAvatars = avatars.take(3)
            val defaultAvatars = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=120&q=80",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=120&q=80"
            )

            val effectiveAvatars = if (displayAvatars.isNotEmpty()) displayAvatars else defaultAvatars

            Box {
                effectiveAvatars.forEachIndexed { index, url ->
                    Box(
                        modifier = Modifier
                            .offset(x = (index * 26).dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .border(2.dp, DarkContourBg, CircleShape)
                            .background(Color(0xFF334155), CircleShape)
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Plus badge pill
                Box(
                    modifier = Modifier
                        .offset(x = (effectiveAvatars.size * 26).dp)
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(Color(0x59000000), CircleShape)
                        .border(1.5.dp, Color(0x33FFFFFF), CircleShape)
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+$extraCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Title and Member Count Info
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextLight
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOnlineState) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Online",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF22C55E),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = memberInfo,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark
                )
            }
        }
    }
}

// =========================================================================
// 3. MAIN HERO FEED CARD (MATCHING PHOTO 1)
// =========================================================================
@Composable
fun HeroFeedCard(
    title: String,
    tags: List<String>,
    imageUrl: String,
    onCloseClick: () -> Unit,
    onStarClick: () -> Unit,
    onShareClick: () -> Unit,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(480.dp)
            .shadow(16.dp, cardShape, ambientColor = Color(0x33000000))
            .clip(cardShape)
            .background(Color(0xFF1E293B), cardShape)
    ) {
        // Card Background Photo
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Overlay for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x20000000),
                                Color(0xCC000000)
                            ),
                            startY = size.height * 0.45f
                        )
                    )
                }
        )

        // Card Content & Buttons
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Headline
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tag Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.take(4).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0x59000000), CircleShape)
                            .border(1.dp, Color(0x33FFFFFF), CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Row (Matching Photo 1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Close Button (White Circle)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SurfacePureWhite, CircleShape)
                        .clickable(onClick = onCloseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Pass",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 2. Star Button (White Circle)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SurfacePureWhite, CircleShape)
                        .clickable(onClick = onStarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Superlike",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 3. Share Button (White Circle)
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SurfacePureWhite, CircleShape)
                        .clickable(onClick = onShareClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Forward,
                        contentDescription = "Share",
                        tint = TextPrimaryDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // 4. Large Green Pill Button with Heart (Matching Photo 1)
                Box(
                    modifier = Modifier
                        .height(54.dp)
                        .weight(1f)
                        .padding(start = 12.dp)
                        .shadow(8.dp, CircleShape, ambientColor = Color(0x6663C700))
                        .clip(CircleShape)
                        .background(LikeGreen, CircleShape)
                        .clickable(onClick = onLikeClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Like",
                        tint = Color.Black,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. CONNECTED ORGANIC BOTTOM NAVIGATION DOCK (MATCHING PHOTO 1)
// =========================================================================
@Composable
fun ConnectedBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        Icons.Default.Favorite,          // 0: Heart
        Icons.Default.Groups,            // 1: Groups / Community
        Icons.Default.Home,              // 2: Home
        Icons.Default.ChatBubbleOutline, // 3: Chat / Messages
        Icons.Default.Person             // 4: Profile
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Connected Dark Glass Dock
        Surface(
            shape = CircleShape,
            color = Color.Transparent,
            modifier = Modifier
                .shadow(18.dp, CircleShape, ambientColor = Color(0x66000000))
                .clip(CircleShape)
                .background(Color(0xCC06141D), CircleShape)
                .border(1.2.dp, Color(0x33FFFFFF), CircleShape)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tabs.forEachIndexed { index, icon ->
                    val isSelected = currentTab == index
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) SurfacePureWhite else Color.Transparent,
                                CircleShape
                            )
                            .clickable { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else Color(0xFF94A3B8),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. CHAT STREAM BUBBLES (MATCHING PHOTO 2)
// =========================================================================

/**
 * Standard Text Message Bubble (Incoming = White, Outgoing = Dark)
 */
@Composable
fun PhotoStyleMessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onReactionClick: (String) -> Unit,
    onLongPress: () -> Unit,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bubbleShape = RoundedCornerShape(22.dp)
    val reactionsMap = message.parseReactions()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
        ) {
            // Incoming Avatar
            if (!isCurrentUser) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE2E8F0), CircleShape)
                ) {
                    if (!message.userAvatar.isNullOrBlank()) {
                        AsyncImage(
                            model = message.userAvatar,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Bubble with floating reaction overlapping bottom-right corner (Matching Photo 2)
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 290.dp)
                        .shadow(
                            elevation = if (isCurrentUser) 4.dp else 2.dp,
                            shape = bubbleShape,
                            ambientColor = Color(0x1F000000)
                        )
                        .clip(bubbleShape)
                        .background(
                            if (isCurrentUser) BubbleOutgoingBg else BubbleIncomingBg,
                            bubbleShape
                        )
                        .border(
                            1.dp,
                            if (isCurrentUser) Color(0x33FFFFFF) else Color(0x1AE2E8F0),
                            bubbleShape
                        )
                        .clickable(onClick = onLongPress)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isCurrentUser) TextLight else TextPrimaryDark,
                        lineHeight = 22.sp
                    )
                }

                // Floating reaction badge on bottom right (Matching Photo 2 red heart badge)
                if (reactionsMap.isNotEmpty()) {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        modifier = Modifier
                            .offset(x = 6.dp, y = 6.dp)
                            .shadow(3.dp, CircleShape)
                            .border(1.dp, Color(0x1AE2E8F0), CircleShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            reactionsMap.forEach { (key, _) ->
                                val rIcon = ReactionIcon.fromKey(key) ?: ReactionIcon.HEART
                                Icon(
                                    imageVector = rIcon.icon,
                                    contentDescription = null,
                                    tint = rIcon.tint,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Developer delete option
            if (canDelete) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(onClick = onDelete)
                )
            }
        }

        // Timestamp beneath
        Text(
            text = formatSimpleTime(message.createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(
                top = 2.dp,
                start = if (!isCurrentUser) 42.dp else 0.dp,
                end = if (isCurrentUser) 8.dp else 0.dp
            )
        )
    }
}

/**
 * Audio Voice Message Bubble (Matching Photo 2)
 */
@Composable
fun AudioVoiceMessageBubble(
    duration: String,
    createdAt: String?,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.End
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BubbleAudioBg,
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(24.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Circle
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfacePureWhite, CircleShape)
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Sound Waveform Bars (Matching Photo 2)
                Row(
                    modifier = Modifier.width(140.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val heights = listOf(8, 14, 22, 10, 18, 24, 12, 16, 26, 14, 20, 12, 18, 22, 16, 10, 14, 8)
                    heights.forEach { h ->
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFE2E8F0))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Duration Text
                Text(
                    text = duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Timestamp
        Text(
            text = formatSimpleTime(createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp, end = 8.dp)
        )
    }
}

/**
 * Media Gallery Message Bubble (Matching Photo 2)
 */
@Composable
fun MediaGalleryBubble(
    headerText: String,
    image1Url: String,
    image2Url: String,
    createdAt: String?,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BubbleIncomingBg,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .shadow(4.dp, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimaryDark,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Two side by side photos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = image1Url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        AsyncImage(
                            model = image2Url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Reactions (Stack + Fire + Thumbs matching Photo 2)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 3 micro overlapping avatars matching Photo 2
                        Box(modifier = Modifier.width(36.dp).height(18.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155))
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=60&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .offset(x = 9.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF64748B))
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=60&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .offset(x = 18.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF94A3B8))
                            ) {
                                AsyncImage(
                                    model = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=60&q=80",
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Fire",
                            tint = Color(0xFFF97316),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Like",
                            tint = Color(0xFF64748B),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onReactionClick("thumb_up") }
                        )
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = "Dislike",
                            tint = Color(0xFF64748B),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onReactionClick("thumb_down") }
                        )
                    }
                }
            }
        }

        Text(
            text = formatSimpleTime(createdAt),
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(top = 2.dp, start = 8.dp)
        )
    }
}

// =========================================================================
// 6. PHOTO 2 BOTTOM CHAT INPUT BAR
// =========================================================================
@Composable
fun PhotoStyleInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onAddMedia: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Circular White '+' Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(SurfacePureWhite, CircleShape)
                .border(1.dp, Color(0x1AE2E8F0), CircleShape)
                .clickable(onClick = onAddMedia),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Center Pill Text Field
        Surface(
            shape = CircleShape,
            color = SurfacePureWhite,
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .shadow(4.dp, CircleShape)
                .border(1.dp, Color(0x1AE2E8F0), CircleShape)
        ) {
            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = "Type your message...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF94A3B8)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = true,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Right Dark Circular Send Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(BubbleOutgoingBg, CircleShape)
                .clickable(enabled = text.isNotBlank(), onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =========================================================================
// 7. PHOTO 3 CALL SCREEN CONTROLS DOCK
// =========================================================================
@Composable
fun CallControlsDock(
    isVideoOn: Boolean,
    isMicOn: Boolean,
    isHeadsetOn: Boolean,
    onToggleVideo: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleHeadset: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color(0xCC06141D),
        modifier = modifier
            .shadow(16.dp, CircleShape)
            .border(1.dp, Color(0x33FFFFFF), CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Video Toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A), CircleShape)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    .clickable(onClick = onToggleVideo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isVideoOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Headset Toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A), CircleShape)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    .clickable(onClick = onToggleHeadset),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Mic Toggle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A), CircleShape)
                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                    .clickable(onClick = onToggleMic),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // End Call (Orange/Red)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(NotificationOrange, CircleShape)
                    .clickable(onClick = onEndCall),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// =========================================================================
// 8. VECTOR ICON REACTIONS PALETTE (NO EMOJIS)
// =========================================================================
@Composable
fun IconReactionsBar(
    onSelectReaction: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = CircleShape,
            color = Color.White,
            modifier = Modifier
                .shadow(16.dp, CircleShape)
                .border(1.dp, Color(0x1AE2E8F0), CircleShape)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionIcon.entries.forEach { item ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSelectReaction(item.key)
                                onDismiss()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.key,
                            tint = item.tint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatSimpleTime(timestamp: String?): String {
    if (timestamp.isNullOrBlank()) return "Just now"
    return try {
        if (timestamp.contains("T")) {
            timestamp.substringAfter("T").take(5)
        } else {
            timestamp
        }
    } catch (_: Exception) {
        "Just now"
    }
}
