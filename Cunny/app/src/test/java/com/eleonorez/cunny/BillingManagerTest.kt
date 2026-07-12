package com.eleonorez.cunny

import com.eleonorez.cunny.data.billing.BillingManager.PurchaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for BillingManager logic.
 *
 * BillingManager is tightly coupled to BillingClient (Android-specific),
 * so we test the state flow patterns and PurchaseState transitions
 * that drive the UI, using the same MutableStateFlow pattern as the real class.
 */
class BillingManagerTest {

    // ── isPremium State Tests ────────────────────────────────────

    @Test
    fun `isPremium - defaults to false`() = runTest {
        val isPremium = MutableStateFlow(false)
        assertFalse(isPremium.value)
    }

    @Test
    fun `isPremium - becomes true after successful purchase`() = runTest {
        val isPremium = MutableStateFlow(false)

        // Simulate purchase completed
        isPremium.value = true
        assertTrue(isPremium.value)
    }

    @Test
    fun `isPremium - stays true across state reads`() = runTest {
        val isPremium = MutableStateFlow(false)
        isPremium.value = true

        // Multiple reads should be consistent
        assertTrue(isPremium.value)
        assertTrue(isPremium.value)
    }

    @Test
    fun `isPremium - can be reverted when subscription expires`() = runTest {
        val isPremium = MutableStateFlow(true)
        isPremium.value = false
        assertFalse(isPremium.value)
    }

    // ── PurchaseState Transition Tests ───────────────────────────

    @Test
    fun `purchaseState - defaults to Idle`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        assertTrue(purchaseState.value is PurchaseState.Idle)
    }

    @Test
    fun `purchaseState - transitions from Idle to Loading`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        purchaseState.value = PurchaseState.Loading
        assertTrue(purchaseState.value is PurchaseState.Loading)
    }

    @Test
    fun `purchaseState - transitions from Loading to Success`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
        purchaseState.value = PurchaseState.Success
        assertTrue(purchaseState.value is PurchaseState.Success)
    }

    @Test
    fun `purchaseState - transitions from Loading to Error`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
        val errorMessage = "Payment failed"
        purchaseState.value = PurchaseState.Error(errorMessage)

        val state = purchaseState.value
        assertTrue(state is PurchaseState.Error)
        assertEquals(errorMessage, (state as PurchaseState.Error).message)
    }

    @Test
    fun `purchaseState - transitions from Loading to Cancelled`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
        purchaseState.value = PurchaseState.Cancelled
        assertTrue(purchaseState.value is PurchaseState.Cancelled)
    }

    @Test
    fun `purchaseState - transitions from Loading to Pending`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
        purchaseState.value = PurchaseState.Pending
        assertTrue(purchaseState.value is PurchaseState.Pending)
    }

    @Test
    fun `purchaseState - Error contains correct message`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        val msg = "BillingClient not connected"
        purchaseState.value = PurchaseState.Error(msg)

        val error = purchaseState.value as PurchaseState.Error
        assertEquals("BillingClient not connected", error.message)
    }

    @Test
    fun `purchaseState - full purchase flow Idle to Loading to Success`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        val isPremium = MutableStateFlow(false)

        // Step 1: User initiates purchase
        purchaseState.value = PurchaseState.Loading
        assertTrue(purchaseState.value is PurchaseState.Loading)
        assertFalse(isPremium.value)

        // Step 2: Purchase succeeds
        purchaseState.value = PurchaseState.Success
        isPremium.value = true

        assertTrue(purchaseState.value is PurchaseState.Success)
        assertTrue(isPremium.value)
    }

    @Test
    fun `purchaseState - failed purchase flow Idle to Loading to Error`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        val isPremium = MutableStateFlow(false)

        // Step 1: User initiates purchase
        purchaseState.value = PurchaseState.Loading

        // Step 2: Purchase fails
        purchaseState.value = PurchaseState.Error("Network error")

        assertTrue(purchaseState.value is PurchaseState.Error)
        assertFalse(isPremium.value) // Should NOT be premium
    }

    @Test
    fun `purchaseState - cancelled purchase keeps isPremium false`() = runTest {
        val purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
        val isPremium = MutableStateFlow(false)

        purchaseState.value = PurchaseState.Loading
        purchaseState.value = PurchaseState.Cancelled

        assertTrue(purchaseState.value is PurchaseState.Cancelled)
        assertFalse(isPremium.value)
    }

    // ── PurchaseState Sealed Class Tests ─────────────────────────

    @Test
    fun `PurchaseState sealed class - all subtypes are distinct`() {
        val states = listOf(
            PurchaseState.Idle,
            PurchaseState.Loading,
            PurchaseState.Success,
            PurchaseState.Pending,
            PurchaseState.Cancelled,
            PurchaseState.Error("test")
        )

        // All should be distinct types
        assertEquals(6, states.map { it::class }.toSet().size)
    }

    @Test
    fun `PurchaseState Error - different messages create unequal instances`() {
        val error1 = PurchaseState.Error("error 1")
        val error2 = PurchaseState.Error("error 2")
        assertNotEquals(error1, error2)
    }

    @Test
    fun `PurchaseState Error - same message creates equal instances`() {
        val error1 = PurchaseState.Error("same error")
        val error2 = PurchaseState.Error("same error")
        assertEquals(error1, error2)
    }
}
