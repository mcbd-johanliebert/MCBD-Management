package com.mcbdone.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// =========================================================================
// GLASSMORHPISM DESIGN SYSTEM UTILITIES
// =========================================================================

/**
 * Applies a true frosted glass effect with translucent acrylic tint,
 * specular reflection border, and soft elevated drop shadow.
 */
fun Modifier.glassmorphic(
    backgroundColor: Color = GlassWhitePrimary,
    borderColor: Brush = GlassBorderGradient,
    borderWidth: Dp = 1.dp,
    shape: Shape = RoundedCornerShape(24.dp),
    shadowElevation: Dp = 8.dp,
    shadowColor: Color = GlassShadowColor
): Modifier = this
    .shadow(
        elevation = shadowElevation,
        shape = shape,
        ambientColor = shadowColor,
        spotColor = shadowColor
    )
    .clip(shape)
    .background(backgroundColor, shape)
    .border(borderWidth, borderColor, shape)

/**
 * Ultra-frosted pill shape modifier for floating navigation bars,
 * search bars, and floating action capsules.
 */
fun Modifier.glassPill(
    backgroundColor: Color = GlassWhiteUltra,
    borderColor: Brush = GlassBorderGradient,
    borderWidth: Dp = 1.2.dp,
    elevation: Dp = 12.dp
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = CircleShape,
        ambientColor = GlassShadowElevated,
        spotColor = GlassShadowElevated
    )
    .clip(CircleShape)
    .background(backgroundColor, CircleShape)
    .border(borderWidth, borderColor, CircleShape)

/**
 * Animated Shimmer effect that sweeps a soft luminous white highlight
 * across glass surfaces for interactive, dynamic wow factor.
 */
@Composable
fun Modifier.glassShimmer(durationMillis: Int = 2400): Modifier {
    val transition = rememberInfiniteTransition(label = "glassShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return this.drawBehind {
        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.25f),
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.25f),
                Color.Transparent
            ),
            start = Offset(translateAnim, 0f),
            end = Offset(translateAnim + 300f, size.height)
        )
        drawRect(brush = shimmerBrush)
    }
}

/**
 * Fullscreen White Glass Background with soft, animated iridescent aurora orbs.
 * These subtle pastel colors drift smoothly behind the translucent white glass
 * layers, giving the UI breathtaking optical depth and frosted glass realism.
 */
@Composable
fun WhiteGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "auroraShift")
    val shiftOne by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shiftOne"
    )
    val shiftTwo by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shiftTwo"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
            .drawBehind {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Base clean radiant light fill
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SurfacePureWhite,
                            BackgroundWhite,
                            Color(0xFFF1F5F9)
                        )
                    )
                )

                // Floating Lavender Glass Orb (Top Right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmbientLavender.copy(alpha = 0.45f),
                            AmbientLavender.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * (0.85f - 0.1f * shiftOne), canvasHeight * (0.15f + 0.08f * shiftTwo)),
                        radius = canvasWidth * 0.75f
                    ),
                    center = Offset(canvasWidth * (0.85f - 0.1f * shiftOne), canvasHeight * (0.15f + 0.08f * shiftTwo)),
                    radius = canvasWidth * 0.75f
                )

                // Floating Soft Cyan Glass Orb (Top Left & Center)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmbientCyan.copy(alpha = 0.45f),
                            AmbientCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * (0.15f + 0.1f * shiftTwo), canvasHeight * (0.35f - 0.05f * shiftOne)),
                        radius = canvasWidth * 0.7f
                    ),
                    center = Offset(canvasWidth * (0.15f + 0.1f * shiftTwo), canvasHeight * (0.35f - 0.05f * shiftOne)),
                    radius = canvasWidth * 0.7f
                )

                // Floating Soft Rose Pearl Orb (Bottom Right)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmbientRose.copy(alpha = 0.40f),
                            AmbientRose.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * (0.8f + 0.05f * shiftTwo), canvasHeight * (0.75f - 0.05f * shiftOne)),
                        radius = canvasWidth * 0.65f
                    ),
                    center = Offset(canvasWidth * (0.8f + 0.05f * shiftTwo), canvasHeight * (0.75f - 0.05f * shiftOne)),
                    radius = canvasWidth * 0.65f
                )

                // Floating Frosted Mint Orb (Bottom Left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AmbientMint.copy(alpha = 0.35f),
                            AmbientMint.copy(alpha = 0.10f),
                            Color.Transparent
                        ),
                        center = Offset(canvasWidth * (0.2f - 0.05f * shiftOne), canvasHeight * (0.85f + 0.05f * shiftTwo)),
                        radius = canvasWidth * 0.6f
                    ),
                    center = Offset(canvasWidth * (0.2f - 0.05f * shiftOne), canvasHeight * (0.85f + 0.05f * shiftTwo)),
                    radius = canvasWidth * 0.6f
                )
            }
    ) {
        content()
    }
}
