package com.example.benchmark

import android.content.Intent
import android.os.SystemClock
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.TraceSectionMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TARGET_PACKAGE = "com.example.architecturepatternscomparative"
private const val STATE_UPDATE_SECTION = "FeedStateUpdate"
private const val FLINGS = 5
private const val TAP_CYCLES = 3
private const val TAPS_PER_CYCLE = 4
private const val PAUSE_AFTER_FLING_MS = 1_000L
private const val PAUSE_AFTER_TAP_MS = 300L

@OptIn(ExperimentalMetricApi::class)
@RunWith(AndroidJUnit4::class)
class FeedBenchmark {
    @get:Rule
    val rule = MacrobenchmarkRule()
    private val args = InstrumentationRegistry.getArguments()
    private val architecture = requireNotNull(args.getString("architecture"))
    {
        "Argumento 'architecture' ausente (MVVM ou MVI)"
    }
    private val scenario = requireNotNull(args.getString("scenario")) {
        "Argumento 'scenario' ausente (S1_CONTROL, S2_ITEM_UPDATES, S3_USER_INTERACTION, S4_AUX_STATE)"
    }
    private val iterations = args.getString("iterations")?.toInt() ?: 10

    @Test
    fun feed() = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(
            FrameTimingMetric(),
            TraceSectionMetric(
                STATE_UPDATE_SECTION,
                TraceSectionMetric.Mode.Sum
            )
        ),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD,
        iterations = iterations,
        setupBlock = {
            pressHome()
            startActivityAndWait(
                Intent().apply {
                    setClassName(
                        TARGET_PACKAGE,
                        "$TARGET_PACKAGE.MainActivity"
                    )
                    putExtra("ARCHITECTURE", architecture)
                    putExtra("SCENARIO", scenario)
                }
            )
            check(device.wait(Until.hasObject(By.res("feed_item")), 5_000)) {
                "A lista não carregou em 5 s"
            }
        }
    ) {
        val list = requireNotNull(device.findObject(By.res("feed_list"))) {
            "feed_list não encontrada"
        }
        list.setGestureMargin(device.displayWidth / 5)
        when (scenario) {
            "S3_USER_INTERACTION" -> repeat(TAP_CYCLES) {
                tapVisibleFavorites(TAPS_PER_CYCLE)
                flingDown(list)
            }

            else -> repeat(FLINGS) { flingDown(list) }
        }
    }
}

private fun flingDown(list: UiObject2) {
    list.fling(Direction.DOWN)
    SystemClock.sleep(PAUSE_AFTER_FLING_MS)
}

/** Busca os botões a cada toque: a recomposição pode invalidar referências
antigas. */
private fun MacrobenchmarkScope.tapVisibleFavorites(count: Int) {
    repeat(count) { index ->
        val buttons = device.findObjects(By.res("favorite_button"))
        val button = requireNotNull(buttons.getOrNull(index)) {
            "Botão de favorito $index não visível (encontrados:${buttons.size})"
        }
        button.click()
        SystemClock.sleep(PAUSE_AFTER_TAP_MS)
    }
}