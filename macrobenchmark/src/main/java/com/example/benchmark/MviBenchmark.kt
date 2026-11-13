package macrobenchmark

import android.content.Intent
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MviBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun measureMviScroll() {
        benchmarkRule.measureRepeated(
            packageName = "com.example.architecturepatternscomparative",
            metrics = listOf(FrameTimingMetric()),
            iterations = 5,
            startupMode = StartupMode.COLD
        ) {
            val intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage("com.example.architecturepatternscomparative")
                putExtra("TARGET_ROUTE", "MVI")
            }

            startActivityAndWait(intent)

            val feedList = device.wait(Until.findObject(By.res("feed_list")), 5000)

            feedList?.setGestureMargin(device.displayWidth / 5)

            repeat(10) {
                feedList?.scroll(Direction.DOWN, 1f)
                device.waitForIdle()
            }
        }
    }
}