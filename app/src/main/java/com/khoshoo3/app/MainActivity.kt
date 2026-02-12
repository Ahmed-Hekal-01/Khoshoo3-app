package com.khoshoo3.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.khoshoo3.app.ui.MainScreen
import com.khoshoo3.app.ui.theme.Khoshoo3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Khoshoo3Theme {
                MainScreen()
            }
        }
    }
}
