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
import com.example.architecturepatternscomparative.core.data.Scenario
import com.example.architecturepatternscomparative.mvi.FeedRouteMvi
import com.example.architecturepatternscomparative.mvvm.FeedRouteMvvm
import com.example.architecturepatternscomparative.ui.theme.ArchitecturePatternsComparativeTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val architecture =
            requireNotNull(intent.getStringExtra(EXTRA_ARCHITECTURE)) {
                "Extra $EXTRA_ARCHITECTURE ausente"
            }
        val scenario = Scenario.valueOf(
            requireNotNull(intent.getStringExtra(EXTRA_SCENARIO)) { "Extra $EXTRA_SCENARIO ausente" }
        )
        setContent {
            ArchitecturePatternsComparativeTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { testTagsAsResourceId = true }
                ) {
                    when (architecture) {
                        "MVVM" -> FeedRouteMvvm(scenario)
                        "MVI" -> FeedRouteMvi(scenario)
                        else -> error("Arquitetura desconhecida: $architecture")
                    }
                }
            }
        }
    }

    companion object {

        const val EXTRA_ARCHITECTURE = "ARCHITECTURE"
        const val EXTRA_SCENARIO = "SCENARIO"
    }
}