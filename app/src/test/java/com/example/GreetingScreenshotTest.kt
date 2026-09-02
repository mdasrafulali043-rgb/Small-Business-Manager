package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.dashboard.KpiCard
import com.example.ui.theme.BusinessManagerTheme
import com.example.ui.theme.EmeraldContainerLight
import com.example.ui.theme.EmeraldPrimaryLight
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun dashboard_kpi_screenshot() {
    composeTestRule.setContent {
      BusinessManagerTheme {
        KpiCard(
          title = "Today's Sales",
          value = "৳ 25,400",
          subtext = "18 Orders",
          icon = Icons.Default.PointOfSale,
          backgroundColor = EmeraldContainerLight,
          iconColor = EmeraldPrimaryLight
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/kpi_card.png")
  }
}

