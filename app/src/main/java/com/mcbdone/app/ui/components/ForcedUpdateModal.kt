package com.mcbdone.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mcbdone.app.data.model.AppVersionInfo
import com.mcbdone.app.data.remote.DownloadState
import com.mcbdone.app.data.remote.UpdateManager
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ForcedUpdateModal(
    versionInfo: AppVersionInfo,
    currentVersionName: String,
    updateManager: UpdateManager
) {
    val coroutineScope = rememberCoroutineScope()
    var downloadState by remember { mutableStateOf<DownloadState>(DownloadState.Idle) }
    var downloadedApkFile by remember { mutableStateOf<File?>(null) }

    val animatedProgress by animateFloatAsState(
        targetValue = when (val state = downloadState) {
            is DownloadState.Downloading -> state.progressPercent / 100f
            is DownloadState.DownloadCompleted -> 1f
            else -> 0f
        },
        label = "DownloadProgress"
    )

    // Strict non-dismissible dialog (No skip, no dismiss)
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCC06141D)) // Dark contour background overlay
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = SurfacePureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(32.dp))
                    .border(1.dp, Color(0x33E2E8F0), RoundedCornerShape(32.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Emblem Icon
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (downloadState) {
                                is DownloadState.DownloadCompleted -> Icons.Default.CheckCircle
                                is DownloadState.Error -> Icons.Default.ErrorOutline
                                else -> Icons.Default.CloudDownload
                            },
                            contentDescription = "Update",
                            tint = when (downloadState) {
                                is DownloadState.DownloadCompleted -> Color(0xFF10B981)
                                is DownloadState.Error -> Color(0xFFEF4444)
                                else -> Color.White
                            },
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Update Required",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Version comparison pill
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF2F2),
                        modifier = Modifier.border(1.dp, Color(0xFFFECACA), CircleShape)
                    ) {
                        Text(
                            text = "v$currentVersionName  →  v${versionInfo.versionName} (Mandatory)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Release notes card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(18.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(
                                text = "WHAT'S NEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = versionInfo.releaseNotes ?: "Critical security, performance updates, and interface enhancements. Upgrading is required to continue using the application.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryDark,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dynamic States: Idle, Downloading, Completed, Error
                    when (val state = downloadState) {
                        is DownloadState.Idle -> {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        updateManager.downloadApk(versionInfo.apkUrl).collect { updateState ->
                                            downloadState = updateState
                                            if (updateState is DownloadState.DownloadCompleted) {
                                                downloadedApkFile = updateState.apkFile
                                                // Trigger auto-install prompt
                                                updateManager.installApk(updateState.apkFile)
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Update Now",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Direct In-App Upgrade • No Browser Redirect",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        is DownloadState.Downloading -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Downloading update package...",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimaryDark
                                    )
                                    Text(
                                        text = "${state.progressPercent}%",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Custom styled linear progress indicator
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape),
                                    color = Color(0xFF2563EB),
                                    trackColor = Color(0xFFE2E8F0)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                val downloadedMb = state.downloadedBytes / (1024f * 1024f)
                                val totalMb = state.totalBytes / (1024f * 1024f)

                                Text(
                                    text = if (totalMb > 0) "%.1f MB / %.1f MB".format(downloadedMb, totalMb) else "%.1f MB downloaded".format(downloadedMb),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        is DownloadState.DownloadCompleted -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Download Complete!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        downloadedApkFile?.let { apkFile ->
                                            updateManager.installApk(apkFile)
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SecurityUpdateGood,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Install Update",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        is DownloadState.Error -> {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Download error: ${state.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFEF4444),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        downloadState = DownloadState.Idle
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Retry Download", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
