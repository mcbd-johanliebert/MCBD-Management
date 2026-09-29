package com.mcbdone.app.ui.screens.call

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mcbdone.app.ui.components.CallControlsDock
import com.mcbdone.app.ui.components.ContourHeader
import com.mcbdone.app.ui.theme.DarkContourBg

enum class TileStatus {
    SPEAKING, // Displays audio waveform GraphicEq (Photo 3: Tile 1, 4)
    MIC_ON,   // Displays Mic icon (Photo 3: Tile 2, 5)
    MUTED     // Displays MicOff icon (Photo 3: Tile 3, 6)
}

data class ParticipantTile(
    val id: String,
    val name: String,
    val imageUrl: String,
    val status: TileStatus
)

@Composable
fun RoomCallScreen(
    onBackClick: () -> Unit,
    onOpenChat: () -> Unit
) {
    val context = LocalContext.current

    var isVideoOn by remember { mutableStateOf(true) }
    var isMicOn by remember { mutableStateOf(true) }
    var isHeadsetOn by remember { mutableStateOf(true) }

    val participants = listOf(
        // Row 1: Left has speaking waves, Right has active mic
        ParticipantTile("1", "Alex", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500&q=80", TileStatus.SPEAKING),
        ParticipantTile("2", "Elena", "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500&q=80", TileStatus.MIC_ON),
        // Row 2: Left has mic muted, Right has speaking waves
        ParticipantTile("3", "Maya", "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=500&q=80", TileStatus.MUTED),
        ParticipantTile("4", "Jordan", "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500&q=80", TileStatus.SPEAKING),
        // Row 3: Left has active mic, Right has mic muted
        ParticipantTile("5", "Sam", "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?w=500&q=80", TileStatus.MIC_ON),
        ParticipantTile("6", "Chloe", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500&q=80", TileStatus.MUTED)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkContourBg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Contour Header (Matching Photo 3)
            ContourHeader(
                title = "room chat",
                isBackButton = true,
                onLeftClick = onBackClick,
                rightIcon = Icons.Default.ChatBubbleOutline,
                badgeCount = 2,
                onRightClick = onOpenChat
            )

            // 2 Columns x 3 Rows Video Grid (Matching Photo 3)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0 until 2) {
                            val index = row * 2 + col
                            val participant = participants.getOrNull(index)
                            if (participant != null) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFF1E293B), RoundedCornerShape(24.dp))
                                        .border(1.dp, Color(0x2238BDF8), RoundedCornerShape(24.dp))
                                ) {
                                    AsyncImage(
                                        model = participant.imageUrl,
                                        contentDescription = participant.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Micro Status Icon in top-left (Matching Photo 3)
                                    Box(
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x66000000), CircleShape)
                                            .border(1.dp, Color(0x33FFFFFF), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (participant.status) {
                                                TileStatus.SPEAKING -> Icons.Default.GraphicEq
                                                TileStatus.MIC_ON -> Icons.Default.Mic
                                                TileStatus.MUTED -> Icons.Default.MicOff
                                            },
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Call Controls Dock (Matching Photo 3)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CallControlsDock(
                    isVideoOn = isVideoOn,
                    isMicOn = isMicOn,
                    isHeadsetOn = isHeadsetOn,
                    onToggleVideo = {
                        isVideoOn = !isVideoOn
                        Toast.makeText(context, if (isVideoOn) "Camera enabled" else "Camera disabled", Toast.LENGTH_SHORT).show()
                    },
                    onToggleMic = {
                        isMicOn = !isMicOn
                        Toast.makeText(context, if (isMicOn) "Microphone unmuted" else "Microphone muted", Toast.LENGTH_SHORT).show()
                    },
                    onToggleHeadset = {
                        isHeadsetOn = !isHeadsetOn
                        Toast.makeText(context, if (isHeadsetOn) "Audio output active" else "Audio output muted", Toast.LENGTH_SHORT).show()
                    },
                    onEndCall = {
                        Toast.makeText(context, "Call ended", Toast.LENGTH_SHORT).show()
                        onBackClick()
                    }
                )
            }
        }
    }
}
