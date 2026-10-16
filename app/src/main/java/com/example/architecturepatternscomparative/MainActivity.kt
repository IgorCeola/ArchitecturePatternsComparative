package com.example.architecturepatternscomparative

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.example.architecturepatternscomparative.mvi.FeedRouteMvi
import com.example.architecturepatternscomparative.mvvm.FeedRouteMvvm

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val route = intent.getStringExtra("TARGET_ROUTE") ?: "MVVM"

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTagsAsResourceId = true }
            ) {
                if (route == "MVI") {
                    FeedRouteMvi()
                } else {
                    FeedRouteMvvm()
                }
            }
        }
    }
}