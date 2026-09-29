# MCBD Glass Messenger (Android)

> **The World's Best White-Themed Glassmorphism Messaging App in Kotlin**  
> Package Name: `com.mcbdone.app`  
> Built with Jetpack Compose, Material 3, Android Credential Manager, and Supabase Realtime.  
> GitHub Repository: [https://github.com/mcbd-johanliebert/MCBD-Management](https://github.com/mcbd-johanliebert/MCBD-Management)

---

## 📱 Release APK
The signed release APK is built and ready for installation:
- **Root APK File**: `MCBD-Glass-Messenger-v1.0.0.apk`
- **Gradle Release Output**: `app/build/outputs/apk/release/app-release.apk`
- **Size**: ~12.7 MB

---

## 🔑 Certificate SHA-1 & SHA-256 Fingerprints

Add these fingerprints to your **Google Cloud Console** under **Credentials -> OAuth 2.0 Client IDs -> Android Client ID (`com.mcbdone.app`)**:

### 1. Active Release Keystore (`mcbd-debug.keystore` - used to sign APK)
- **SHA-1**: `3D:4A:E1:18:95:1E:B3:9D:0D:21:36:97:60:1B:88:E5:91:43:C3:75`
- **SHA-256**: `72:9D:8E:8F:72:32:7D:09:D2:62:67:02:E0:DD:53:EE:83:DF:EE:80:92:22:CA:C8:83:B2:3A:57:12:20:C3:17`
- **Alias**: `mcbdkey`
- **Store / Key Pass**: `mcbd123`

### 2. Standard Android Debug Keystore (`~/.android/debug.keystore`)
- **SHA-1**: `81:45:57:AA:A8:E2:8F:72:F6:72:F3:FB:53:E9:28:3F:80:CF:8A:90`
- **SHA-256**: `8D:25:BE:63:9A:28:90:65:05:03:96:57:EE:EA:3D:0F:A9:AB:96:24:4C:19:6F:A8:EE:A2:D7:39:14:DE:60:6E`

---

## ✨ Features & Architecture

### 1. Pure White Glassmorphism Design System
- **Luminous White Glass Surfaces**: Frosted translucent acrylic containers (`0xCCFFFFFF` to `0xF2FFFFFF`) with soft backdrop blur simulation.
- **Dynamic Specular Borders**: Micro-fine 1.dp crystal highlight gradients (`Brush.linearGradient` with pure white specular reflection transitioning to subtle frost).
- **Ambient Iridescent Aurora Orbs**: Gentle animated lavender, soft cyan, rose pearl, and frosted mint light spheres drift smoothly behind the translucent white glass, providing authentic optical depth and refraction.
- **Glass Message Bubbles**:
  - **Outgoing**: Crystalline royal blue & indigo gradient with specular glass highlights and read receipt checkmarks.
  - **Incoming**: Pure snow glass with fine specular highlight border and high-contrast typography.
- **Floating Glass Dock & Input Capsule**: Elevated frosted glass pills with soft shadows and animated active tabs.
- **Zero Unicode Emojis**: 100% clean, professional vector iconography throughout the entire user interface.

### 2. Developer Access & Permissions
- Root Developer Account: `nazmusshakibshihan01@gmail.com`
- Full administrative access: Manage channels, broadcast messages, update app versions, and trigger forced OTA updates.

### 3. In-App OTA Version Control System
- **Live Supabase Sync**: The app polls `public.app_versions` for version code differences.
- **Strict Forced Update Modal**: Non-dismissible frosted glass dialog when a mandatory update is published.
- **Direct Background APK Stream & Installer**: Downloads APK directly to cache with progress bar and launches Android `PackageInstaller`.
- **Developer Version Management Console**: Root developers can publish new version numbers, direct APK URLs, and release notes directly from the profile screen.

### 4. Google-Only Authentication
- **Credential Manager**:
  - Configured via `local.properties` (or pre-bundled fallback values).
  - See `local.properties.example` for details.
- Instant fallback JWT decoding for reliable authentication.

### 5. Realtime Supabase Backend
- **Project URL**: `https://cvppveogubeudebsmazd.supabase.co`
- Full SQL setup available in `supabase_schema.sql` (Profiles, Channels, Messages, App Versions, RLS policies, Realtime triggers).

---

## 🚀 How to Build & Run
```powershell
# Compile debug variant
.\gradlew.bat compileDebugKotlin

# Assemble signed release APK
.\gradlew.bat assembleRelease
```
