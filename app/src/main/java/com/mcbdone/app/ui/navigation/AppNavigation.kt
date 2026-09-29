package com.mcbdone.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mcbdone.app.data.remote.GoogleAuthManager
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.ui.screens.auth.LoginScreen
import com.mcbdone.app.ui.screens.call.RoomCallScreen
import com.mcbdone.app.ui.screens.chat.ChatRoomScreen
import com.mcbdone.app.ui.screens.feed.FeedScreen
import com.mcbdone.app.ui.screens.profile.ProfileScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.mcbdone.app.BuildConfig
import com.mcbdone.app.data.model.AppVersionInfo
import com.mcbdone.app.data.remote.UpdateManager
import com.mcbdone.app.ui.components.ForcedUpdateModal

object AppRoutes {
    const val LOGIN = "login"
    const val FEED = "feed"
    const val CHAT_ROOM = "chat_room/{channelId}/{channelTitle}"
    const val ROOM_CALL = "room_call"
    const val PROFILE = "profile"

    fun chatRoomRoute(channelId: String, channelTitle: String): String {
        val encodedTitle = URLEncoder.encode(channelTitle, StandardCharsets.UTF_8.toString())
        return "chat_room/$channelId/$encodedTitle"
    }
}

@Composable
fun AppNavigation(
    googleAuthManager: GoogleAuthManager,
    supabaseManager: SupabaseManager
) {
    val context = LocalContext.current
    val updateManager = remember { UpdateManager(context) }
    var mandatoryUpdateInfo by remember { mutableStateOf<AppVersionInfo?>(null) }

    LaunchedEffect(Unit) {
        val latest = supabaseManager.fetchLatestAppVersion()
        if (latest != null && latest.isStrictUpdateRequired(BuildConfig.VERSION_CODE)) {
            mandatoryUpdateInfo = latest
        }
    }

    val navController = rememberNavController()
    val startDestination = if (supabaseManager.isUserLoggedIn()) AppRoutes.FEED else AppRoutes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(250))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(250))
        }
    ) {
        // Login Screen
        composable(AppRoutes.LOGIN) {
            LoginScreen(
                googleAuthManager = googleAuthManager,
                supabaseManager = supabaseManager,
                onLoginSuccess = {
                    navController.navigate(AppRoutes.FEED) {
                        popUpTo(AppRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Photo 1 Main Feed Screen
        composable(AppRoutes.FEED) {
            FeedScreen(
                supabaseManager = supabaseManager,
                onOpenChatRoom = { channelId, channelTitle ->
                    navController.navigate(AppRoutes.chatRoomRoute(channelId, channelTitle))
                },
                onOpenProfile = {
                    navController.navigate(AppRoutes.PROFILE)
                }
            )
        }

        // Photo 2 Chat Room Screen
        composable(
            route = AppRoutes.CHAT_ROOM,
            arguments = listOf(
                navArgument("channelId") { type = NavType.StringType },
                navArgument("channelTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val channelId = backStackEntry.arguments?.getString("channelId") ?: "peak-seekers"
            val rawTitle = backStackEntry.arguments?.getString("channelTitle") ?: "peak seekers"
            val channelTitle = try {
                URLDecoder.decode(rawTitle, StandardCharsets.UTF_8.toString())
            } catch (_: Exception) {
                rawTitle
            }

            ChatRoomScreen(
                channelId = channelId,
                channelTitle = channelTitle,
                supabaseManager = supabaseManager,
                onBackClick = { navController.popBackStack() },
                onOpenCall = { navController.navigate(AppRoutes.ROOM_CALL) }
            )
        }

        // Photo 3 Room Video/Audio Call Screen
        composable(AppRoutes.ROOM_CALL) {
            RoomCallScreen(
                onBackClick = { navController.popBackStack() },
                onOpenChat = {
                    navController.navigate(AppRoutes.chatRoomRoute("peak-seekers", "peak seekers"))
                }
            )
        }

        // Profile & Developer Suite Screen
        composable(AppRoutes.PROFILE) {
            ProfileScreen(
                supabaseManager = supabaseManager,
                onBackClick = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(AppRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }

    // Strict Forced In-App Update Modal
    if (mandatoryUpdateInfo != null) {
        ForcedUpdateModal(
            versionInfo = mandatoryUpdateInfo!!,
            currentVersionName = BuildConfig.VERSION_NAME,
            updateManager = updateManager
        )
    }
}
