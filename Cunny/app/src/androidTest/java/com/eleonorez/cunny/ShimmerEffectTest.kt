package com.eleonorez.cunny

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.eleonorez.cunny.ui.compose.components.ShimmerEffect
import org.junit.Rule
import org.junit.Test

/**
 * Basic Compose UI test verifying ShimmerEffect renders without crashing.
 */
class ShimmerEffectTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shimmerEffect_rendersWithoutCrashing() {
        composeTestRule.setContent {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            )
        }

        // Verify the root node exists (composable rendered successfully)
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun shimmerEffect_rendersWithCustomDuration() {
        composeTestRule.setContent {
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                durationMillis = 2000
            )
        }

        composeTestRule.onRoot().assertExists()
    }
}
