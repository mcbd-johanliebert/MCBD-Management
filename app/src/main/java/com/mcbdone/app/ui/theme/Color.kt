package com.mcbdone.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// COLOR PALETTE MATCHING DESIGN PHOTOS & THEME TOKENS
// =========================================================================

// Deep Contoured Header & Accent Backgrounds (Photos 1, 2, 3)
val DarkContourBg = Color(0xFF06141D)
val DarkContourSurface = Color(0xFF091C28)
val DarkContourBorder = Color(0x3338BDF8)

// Radiant White & Light Elements
val BackgroundWhite = Color(0xFFF6F9FB)
val SurfacePureWhite = Color(0xFFFFFFFF)
val SurfaceSubtle = Color(0xFFF1F5F9)

// Message Bubbles
val BubbleIncomingBg = Color(0xFFFFFFFF)
val BubbleOutgoingBg = Color(0xFF0A1926)
val BubbleAudioBg = Color(0xFF0A1926)

// Action Colors
val LikeGreen = Color(0xFF63C700)
val NotificationOrange = Color(0xFFF97316)
val AccentPrimary = Color(0xFF2563EB)
val AccentCyan = Color(0xFF0EA5E9)

// Ambient Canvas Colors for Glassmorphism
val AmbientLavender = Color(0xFFE9D5FF)
val AmbientCyan = Color(0xFFBAE6FD)
val AmbientRose = Color(0xFFFFE4E6)
val AmbientMint = Color(0xFFA7F3D0)

// Typography
val TextPrimaryDark = Color(0xFF0F172A)
val TextSecondaryDark = Color(0xFF64748B)
val TextPrimary = TextPrimaryDark
val TextSecondary = TextSecondaryDark
val TextMuted = Color(0xFF94A3B8)
val TextOnAccent = Color(0xFFFFFFFF)
val TextLight = Color(0xFFFFFFFF)
val TextLightSecondary = Color(0xCCFFFFFF)

// Glass Surfaces & Borders
val GlassWhiteUltra = Color(0xF5FFFFFF)
val GlassWhitePrimary = Color(0xEBFFFFFF)
val GlassWhiteMedium = Color(0x99FFFFFF)
val GlassDarkPill = Color(0xE60A1926)

val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0x66FFFFFF),
        Color(0x99FFFFFF),
        Color(0x33FFFFFF)
    )
)

val GlassBorderHighlightBrush = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xCCFFFFFF))
)

val GlassBorderLight = Color(0x40E2E8F0)
val GlassShadowColor = Color(0x120F172A)
val GlassShadowElevated = Color(0x240F172A)
