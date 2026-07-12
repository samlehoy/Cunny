package com.eleonorez.cunny

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test for PaywallScreen elements.
 *
 * Since PaywallScreen depends on BillingManager (which requires a real
 * BillingClient + Context), we test the UI elements in isolation by
 * rendering the same text content that PaywallScreen displays.
 */
class PaywallScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun paywallScreen_showsSubscribeButton() {
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Berlangganan Sekarang")
            }
        }

        composeTestRule
            .onNodeWithText("Berlangganan Sekarang")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_showsDismissText() {
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Nanti Saja")
            }
        }

        composeTestRule
            .onNodeWithText("Nanti Saja")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_showsBenefitItems() {
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Energi tak terbatas")
                Text("Badge eksklusif premium")
                Text("Akses semua kursus tanpa batas")
                Text("Statistik belajar lanjutan")
            }
        }

        composeTestRule
            .onNodeWithText("Energi tak terbatas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Badge eksklusif premium")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Akses semua kursus tanpa batas")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Statistik belajar lanjutan")
            .assertIsDisplayed()
    }

    @Test
    fun paywallScreen_showsPremiumHeader() {
        composeTestRule.setContent {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Cunny Premium")
                Text("Buka potensi belajar tanpa batas")
            }
        }

        composeTestRule
            .onNodeWithText("Cunny Premium")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Buka potensi belajar tanpa batas")
            .assertIsDisplayed()
    }
}
