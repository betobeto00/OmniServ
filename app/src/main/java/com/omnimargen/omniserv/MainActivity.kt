package com.omnimargen.omniserv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.omnimargen.omniserv.ui.navigation.OmniServNavGraph
import com.omnimargen.omniserv.ui.screens.splash.SplashScreen
import com.omnimargen.omniserv.ui.theme.OmniServTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmniServTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    OmniServNavGraph()
                }
            }
        }
    }
}
