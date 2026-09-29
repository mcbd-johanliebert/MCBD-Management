package com.mcbdone.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mcbdone.app.data.remote.GoogleAuthManager
import com.mcbdone.app.data.remote.SupabaseManager
import com.mcbdone.app.ui.navigation.AppNavigation
import com.mcbdone.app.ui.theme.BackgroundWhite
import com.mcbdone.app.ui.theme.MCBDGlassTheme

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthManager: GoogleAuthManager
    private lateinit var supabaseManager: SupabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        googleAuthManager = GoogleAuthManager(this)
        supabaseManager = SupabaseManager(this)

        setContent {
            MCBDGlassTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundWhite
                ) {
                    AppNavigation(
                        googleAuthManager = googleAuthManager,
                        supabaseManager = supabaseManager
                    )
                }
            }
        }
    }
}
