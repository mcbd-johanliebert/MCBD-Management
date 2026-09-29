package com.mcbdone.app.ui.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mcbdone.app.BuildConfig
import com.mcbdone.app.data.model.AppVersionInfo
import com.mcbdone.app.data.model.UserProfile
import com.mcbdone.app.data.model.UserStatus
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.data.remote.UpdateManager
import com.mcbdone.app.ui.components.ContourHeader
import com.mcbdone.app.ui.components.ForcedUpdateModal
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    supabaseManager: SupabaseManager,
    onBackClick: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var currentUser by remember { mutableStateOf(supabaseManager.currentUser) }
    val isDeveloper = supabaseManager.isCurrentDeveloper

    var selectedStatus by remember { mutableStateOf(currentUser?.status ?: "online") }
    var bioText by remember { mutableStateOf(currentUser?.bio ?: "Active Community Member") }
    var isSaving by remember { mutableStateOf(false) }

    // Version Control System states for developer
    var cloudVersion by remember { mutableStateOf<AppVersionInfo?>(null) }
    var newVersionCodeText by remember { mutableStateOf("2") }
    var newVersionNameText by remember { mutableStateOf("1.0.1") }
    var minVersionCodeText by remember { mutableStateOf("2") }
    var githubUrlText by remember { mutableStateOf("https://github.com/mcbd-johanliebert/MCBD-Management") }
    var apkUrlText by remember { mutableStateOf("https://github.com/mcbd-johanliebert/MCBD-Management/releases/latest/download/MCBD-Glass-Messenger-v1.0.0.apk") }
    var releaseNotesText by remember { mutableStateOf("World-class glass UI optimizations, improved realtime stream, and bug fixes.") }
    var isMandatoryUpdate by remember { mutableStateOf(true) }
    var isPublishingVersion by remember { mutableStateOf(false) }
    var previewModalInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

    LaunchedEffect(Unit) {
        val latest = supabaseManager.fetchLatestAppVersion()
        cloudVersion = latest
        latest?.let {
            newVersionCodeText = (it.versionCode + 1).toString()
            newVersionNameText = "1.0.${it.versionCode}"
            minVersionCodeText = (it.versionCode + 1).toString()
            if (!it.githubUrl.isNullOrBlank()) githubUrlText = it.githubUrl
            if (it.apkUrl.isNotBlank()) apkUrlText = it.apkUrl
            if (!it.releaseNotes.isNullOrBlank()) releaseNotesText = it.releaseNotes
            isMandatoryUpdate = it.isMandatory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .verticalScroll(scrollState)
    ) {
        // Contour Header
        ContourHeader(
            title = "profile",
            isBackButton = true,
            onLeftClick = onBackClick,
            rightIcon = Icons.Default.Settings,
            badgeCount = null,
            onRightClick = {
                Toast.makeText(context, "Settings", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Photo & Details Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfacePureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFE2E8F0), CircleShape)
                            .border(2.dp, if (isDeveloper) Color(0xFF2563EB) else Color(0x33000000), CircleShape)
                    ) {
                        if (!currentUser?.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = currentUser?.avatarUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentUser?.fullName ?: "Member",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Text(
                        text = currentUser?.email ?: "account@google.com",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge Pill
                    Surface(
                        shape = CircleShape,
                        color = if (isDeveloper) Color(0xFF1E3A8A) else Color(0xFF0F172A),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDeveloper) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = if (isDeveloper) "Verified Developer & System Architect" else "Community Member",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Developer Privilege Card
            if (isDeveloper) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.2.dp, Color(0xFF93C5FD), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Full Developer Access Active",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your account (nazmusshakibshihan01@gmail.com) has unrestricted root permissions: delete any message, manage channels, and inspect real-time database feeds.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF1E3A8A)
                        )
                    }
                }
            }

            // Over-The-Air Version Management Card (For Root Developer)
            if (isDeveloper) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfacePureWhite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .border(1.2.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OTA Version Control",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF0FDF4),
                                modifier = Modifier.border(1.dp, Color(0xFFBBF7D0), CircleShape)
                            ) {
                                Text(
                                    text = "Current: v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Control in-app mandatory updates via Supabase. Users will be strictly forced to update directly within the app without external browser redirects.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newVersionCodeText,
                                onValueChange = { newVersionCodeText = it },
                                label = { Text("Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = newVersionNameText,
                                onValueChange = { newVersionNameText = it },
                                label = { Text("Version") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = minVersionCodeText,
                                onValueChange = { minVersionCodeText = it },
                                label = { Text("Min Code") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = githubUrlText,
                            onValueChange = {
                                githubUrlText = it
                                val trimmed = it.trim().removeSuffix("/")
                                apkUrlText = "$trimmed/releases/latest/download/MCBD-Glass-Messenger-v1.0.0.apk"
                            },
                            label = { Text("GitHub Repository URL") },
                            placeholder = { Text("https://github.com/mcbd-johanliebert/MCBD-Management") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = apkUrlText,
                            onValueChange = { apkUrlText = it },
                            label = { Text("Direct APK Download URL (GitHub Releases)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = releaseNotesText,
                            onValueChange = { releaseNotesText = it },
                            label = { Text("Release Notes (Shown to users)") },
                            singleLine = false,
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Test Modal Button
                            OutlinedButton(
                                onClick = {
                                    val simulatedInfo = AppVersionInfo(
                                        versionCode = newVersionCodeText.toIntOrNull() ?: 2,
                                        versionName = newVersionNameText.ifBlank { "1.0.1" },
                                        minSupportedVersion = minVersionCodeText.toIntOrNull() ?: 2,
                                        apkUrl = apkUrlText,
                                        githubUrl = githubUrlText,
                                        releaseNotes = releaseNotesText,
                                        isMandatory = isMandatoryUpdate
                                    )
                                    previewModalInfo = simulatedInfo
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Preview,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview", fontSize = 12.sp)
                            }

                            // Publish to Supabase Button
                            Button(
                                onClick = {
                                    val code = newVersionCodeText.toIntOrNull() ?: 2
                                    val minCode = minVersionCodeText.toIntOrNull() ?: 2
                                    val info = AppVersionInfo(
                                        id = "latest",
                                        versionCode = code,
                                        versionName = newVersionNameText.trim().ifBlank { "1.0.1" },
                                        minSupportedVersion = minCode,
                                        apkUrl = apkUrlText.trim(),
                                        githubUrl = githubUrlText.trim(),
                                        releaseNotes = releaseNotesText.trim(),
                                        isMandatory = isMandatoryUpdate
                                    )
                                    coroutineScope.launch {
                                        isPublishingVersion = true
                                        val res = supabaseManager.publishAppVersion(info)
                                        isPublishingVersion = false
                                        if (res.isSuccess) {
                                            cloudVersion = info
                                            Toast.makeText(context, "Version $code published to Supabase!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Publish failed: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (isPublishingVersion) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Publish,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Publish OTA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Selector Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfacePureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val statusList = listOf(
                        UserStatus.ONLINE to "online",
                        UserStatus.AWAY to "away",
                        UserStatus.BUSY to "busy",
                        UserStatus.OFFLINE to "offline"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statusList.forEach { (stat, key) ->
                            val isSelected = selectedStatus.equals(key, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFFE2E8F0) else Color(0xFFF8FAFC),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        if (isSelected) 1.5.dp else 1.dp,
                                        if (isSelected) Color(0xFF0F172A) else Color(0x1AE2E8F0),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable { selectedStatus = key }
                                    .padding(vertical = 10.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(stat.colorHex), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stat.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = TextPrimaryDark
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = bioText,
                        onValueChange = { bioText = it },
                        label = { Text("Bio / Status message") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isSaving = true
                                val res = supabaseManager.updateUserStatus(selectedStatus, bioText)
                                isSaving = false
                                if (res.isSuccess) {
                                    currentUser = supabaseManager.currentUser
                                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Status", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Integration & SHA1 Card
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfacePureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "INTEGRATION INFO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ProfileInfoRow(label = "Package Name", value = "com.mcbdone.app") {
                        clipboardManager.setText(AnnotatedString("com.mcbdone.app"))
                        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                    }

                    HorizontalDivider(color = Color(0x0DE2E8F0), modifier = Modifier.padding(vertical = 6.dp))

                    ProfileInfoRow(label = "Release Keystore SHA-1", value = "3D:4A:E1:18:95:1E:B3:9D:0D:21:36:97:60:1B:88:E5:91:43:C3:75") {
                        clipboardManager.setText(AnnotatedString("3D:4A:E1:18:95:1E:B3:9D:0D:21:36:97:60:1B:88:E5:91:43:C3:75"))
                        Toast.makeText(context, "SHA-1 copied", Toast.LENGTH_SHORT).show()
                    }

                    HorizontalDivider(color = Color(0x0DE2E8F0), modifier = Modifier.padding(vertical = 6.dp))

                    ProfileInfoRow(label = "Debug Keystore SHA-1", value = "81:45:57:AA:A8:E2:8F:72:F6:72:F3:FB:53:E9:28:3F:80:CF:8A:90") {
                        clipboardManager.setText(AnnotatedString("81:45:57:AA:A8:E2:8F:72:F6:72:F3:FB:53:E9:28:3F:80:CF:8A:90"))
                        Toast.makeText(context, "SHA-1 copied", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Out Button
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFEF2F2),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(20.dp))
                    .clickable {
                        supabaseManager.signOut()
                        onSignedOut()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (previewModalInfo != null) {
        ForcedUpdateModal(
            versionInfo = previewModalInfo!!,
            currentVersionName = BuildConfig.VERSION_NAME,
            updateManager = UpdateManager(context)
        )
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCopy)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = TextPrimaryDark,
                maxLines = 1
            )
        }
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy",
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(16.dp)
        )
    }
}
