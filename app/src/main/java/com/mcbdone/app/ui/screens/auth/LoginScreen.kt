package com.mcbdone.app.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mcbdone.app.data.remote.GoogleAuthManager
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    googleAuthManager: GoogleAuthManager,
    supabaseManager: SupabaseManager,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkContourBg)
            .drawBehind {
                val w = size.width
                val h = size.height

                val path1 = Path().apply {
                    moveTo(0f, h * 0.25f)
                    cubicTo(w * 0.3f, h * 0.1f, w * 0.7f, h * 0.45f, w, h * 0.2f)
                }
                drawPath(path1, color = Color(0x1F38BDF8), style = Stroke(width = 1.5f))

                val path2 = Path().apply {
                    moveTo(0f, h * 0.65f)
                    cubicTo(w * 0.35f, h * 0.85f, w * 0.65f, h * 0.55f, w, h * 0.7f)
                }
                drawPath(path2, color = Color(0x1838BDF8), style = Stroke(width = 1.5f))
            }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // App Emblem
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(16.dp, CircleShape, ambientColor = Color(0x66000000))
                    .clip(CircleShape)
                    .background(SurfacePureWhite, CircleShape)
                    .border(2.dp, Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Logo",
                    tint = TextPrimaryDark,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = "friendlyhours",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 32.sp
            )

            Text(
                text = "Next-Generation Glass Realtime Messenger",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Main White Glass Card
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = SurfacePureWhite,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(28.dp))
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )

                    Text(
                        text = "Sign in securely with your Google Account to join channels and chat in realtime.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )

                    // Features
                    LoginFeatureRow(
                        icon = Icons.Default.Security,
                        title = "Google Zero-Trust Identity",
                        desc = "Secure Single Sign-On"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LoginFeatureRow(
                        icon = Icons.Default.Speed,
                        title = "Supabase Realtime Cloud",
                        desc = "Instant real-time message stream"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LoginFeatureRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Developer Privileges",
                        desc = "Root access for nazmusshakibshihan01@gmail.com"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Google-Only Sign-In Button
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(20.dp))
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    val tokenResult = googleAuthManager.signInWithGoogle()
                                    tokenResult.fold(
                                        onSuccess = { idToken ->
                                            val exchangeResult = supabaseManager.exchangeGoogleToken(idToken)
                                            exchangeResult.fold(
                                                onSuccess = { profile ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Welcome, ${profile.fullName}!", Toast.LENGTH_SHORT).show()
                                                    onLoginSuccess()
                                                },
                                                onFailure = { err ->
                                                    isLoading = false
                                                    Toast.makeText(context, "Login error: ${err.message}", Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        onFailure = { err ->
                                            isLoading = false
                                            Toast.makeText(context, err.message ?: "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Package: com.mcbdone.app • Verified Developer Suite",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
private fun LoginFeatureRow(
    icon: ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0x1AE2E8F0), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFFE2E8F0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondaryDark,
                fontSize = 11.sp
            )
        }
    }
}
