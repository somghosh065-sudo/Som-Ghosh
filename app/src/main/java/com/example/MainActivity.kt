package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.TuitionAppMain
import com.example.ui.TuitionViewModel
import com.example.ui.TuitionViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TuitionViewModel by viewModels {
        TuitionViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val config by viewModel.appConfig.collectAsState()
            val isDark = config?.isDarkMode ?: false

            MyApplicationTheme(darkTheme = isDark) {
                TuitionAppMain(viewModel = viewModel)
            }
        }
    }
}
