package com.example.novabrief

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.novabrief.core.designsystem.theme.NovaBriefTheme
import com.example.novabrief.core.navigation.AppNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovaBriefTheme {
                AppNavHost()
            }
        }
    }
}
