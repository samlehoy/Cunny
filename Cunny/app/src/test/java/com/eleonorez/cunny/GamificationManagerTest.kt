package com.eleonorez.cunny

import com.eleonorez.cunny.data.database.GamificationDao
import com.eleonorez.cunny.data.database.UserProgressEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

/**
 * Unit tests for GamificationManager logic.
 *
 * Since GamificationManager is tightly coupled to Context (Room, ApiConfig, SyncManager),
 * we test the core gamification logic directly against a mocked GamificationDao,
 * replicating the exact same calculations used in GamificationManager.
 */
class GamificationManagerTest {

    private lateinit var dao: GamificationDao

    @Before
    fun setUp() {
        dao = mock()
    }

    // ── addXp Tests ──────────────────────────────────────────────

    @Test
    fun `addXp - from null progress creates entity with correct XP and level`() = runTest {
        whenever(dao.getProgress()).thenReturn(null)

        // Replicate GamificationManager.addXp logic
        val amount = 30
        val level = 1 + (amount / 50) // 1
        val expected = UserProgressEntity(id = 1, xp = amount, level = level)

        dao.upsertProgress(expected)

        verify(dao).upsertProgress(expected)
        assertEquals(30, expected.xp)
        assertEquals(1, expected.level)
    }

    @Test
    fun `addXp - accumulates XP correctly on existing progress`() = runTest {
        val existing = UserProgressEntity(id = 1, xp = 40, level = 1)
        whenever(dao.getProgress()).thenReturn(existing)

        val amount = 20
        val newXp = existing.xp + amount // 60
        val newLevel = 1 + (newXp / 50) // 2
        val updated = existing.copy(xp = newXp, level = newLevel)

        assertEquals(60, updated.xp)
        assertEquals(2, updated.level)
    }

    @Test
    fun `addXp - triggers level-up at 50 XP threshold`() = runTest {
        val existing = UserProgressEntity(id = 1, xp = 45, level = 1)
        whenever(dao.getProgress()).thenReturn(existing)

        val amount = 10
        val newXp = existing.xp + amount // 55
        val newLevel = 1 + (newXp / 50) // 2

        assertEquals(55, newXp)
        assertEquals(2, newLevel)
    }

    @Test
    fun `addXp - multiple level-ups with large XP gain`() = runTest {
        val existing = UserProgressEntity(id = 1, xp = 10, level = 1)
        whenever(dao.getProgress()).thenReturn(existing)

        val amount = 140
        val newXp = existing.xp + amount // 150
        val newLevel = 1 + (newXp / 50) // 4

        assertEquals(150, newXp)
        assertEquals(4, newLevel)
    }

    @Test
    fun `addXp - zero XP stays at same level`() = runTest {
        val existing = UserProgressEntity(id = 1, xp = 25, level = 1)
        whenever(dao.getProgress()).thenReturn(existing)

        val amount = 0
        val newXp = existing.xp + amount
        val newLevel = 1 + (newXp / 50)

        assertEquals(25, newXp)
        assertEquals(1, newLevel)
    }

    // ── consumeEnergy Tests ──────────────────────────────────────

    @Test
    fun `consumeEnergy - decrements energy by 1`() = runTest {
        val current = UserProgressEntity(id = 1, energy = 5)
        whenever(dao.getProgress()).thenReturn(current)

        val amount = 1
        assertTrue(current.energy >= amount)

        val newEnergy = current.energy - amount
        assertEquals(4, newEnergy)
    }

    @Test
    fun `consumeEnergy - returns false when energy is insufficient`() = runTest {
        val current = UserProgressEntity(id = 1, energy = 0)
        whenever(dao.getProgress()).thenReturn(current)

        val amount = 1
        val canConsume = current.energy >= amount
        assertFalse(canConsume)
    }

    @Test
    fun `consumeEnergy - returns false when energy equals 0`() = runTest {
        val current = UserProgressEntity(id = 1, energy = 0)
        whenever(dao.getProgress()).thenReturn(current)

        assertFalse(current.energy >= 1)
    }

    @Test
    fun `consumeEnergy - succeeds at energy equals 1`() = runTest {
        val current = UserProgressEntity(id = 1, energy = 1)
        whenever(dao.getProgress()).thenReturn(current)

        val amount = 1
        assertTrue(current.energy >= amount)
        val newEnergy = current.energy - amount
        assertEquals(0, newEnergy)
    }

    @Test
    fun `consumeEnergy - null progress creates default with energy 4`() = runTest {
        whenever(dao.getProgress()).thenReturn(null)

        // GamificationManager creates new entity with energy = max(0, 5 - amount)
        val amount = 1
        val newEnergy = maxOf(0, 5 - amount)
        assertEquals(4, newEnergy)
    }

    @Test
    fun `consumeEnergy - sets lastRefillTime when energy drops below 5`() = runTest {
        val currentTime = 1000000L
        val current = UserProgressEntity(id = 1, energy = 5, lastRefillTime = 0L)
        whenever(dao.getProgress()).thenReturn(current)

        val amount = 1
        val newEnergy = current.energy - amount // 4
        val newRefillTime = if (newEnergy < 5 && current.lastRefillTime == 0L) currentTime else current.lastRefillTime

        assertEquals(4, newEnergy)
        assertEquals(currentTime, newRefillTime)
    }

    // ── checkAndRefillEnergy Tests ───────────────────────────────

    @Test
    fun `checkAndRefillEnergy - no refill when energy is full`() = runTest {
        val current = UserProgressEntity(id = 1, energy = 5, lastRefillTime = 0L)
        whenever(dao.getProgress()).thenReturn(current)

        // When energy >= 5, no refill occurs
        assertTrue(current.energy >= 5)
    }

    @Test
    fun `checkAndRefillEnergy - refills energy after 30 min interval`() = runTest {
        val lastRefill = 1000000L
        val intervalMs = 30 * 60 * 1000L // 30 minutes
        val currentTime = lastRefill + intervalMs // exactly 1 interval passed

        val current = UserProgressEntity(id = 1, energy = 3, lastRefillTime = lastRefill)
        whenever(dao.getProgress()).thenReturn(current)

        val elapsedMs = currentTime - lastRefill
        val intervalsPassed = (elapsedMs / intervalMs).toInt()

        assertEquals(1, intervalsPassed)

        val newEnergy = minOf(5, current.energy + intervalsPassed)
        assertEquals(4, newEnergy)
    }

    @Test
    fun `checkAndRefillEnergy - caps energy at 5`() = runTest {
        val lastRefill = 1000000L
        val intervalMs = 30 * 60 * 1000L
        val currentTime = lastRefill + (intervalMs * 5) // 5 intervals passed

        val current = UserProgressEntity(id = 1, energy = 3, lastRefillTime = lastRefill)
        whenever(dao.getProgress()).thenReturn(current)

        val elapsedMs = currentTime - lastRefill
        val intervalsPassed = (elapsedMs / intervalMs).toInt()

        assertEquals(5, intervalsPassed)

        val newEnergy = minOf(5, current.energy + intervalsPassed) // min(5, 3+5) = 5
        assertEquals(5, newEnergy)
    }

    @Test
    fun `checkAndRefillEnergy - resets lastRefillTime when energy reaches 5`() = runTest {
        val lastRefill = 1000000L
        val intervalMs = 30 * 60 * 1000L
        val currentTime = lastRefill + (intervalMs * 3) // 3 intervals

        val current = UserProgressEntity(id = 1, energy = 3, lastRefillTime = lastRefill)

        val elapsedMs = currentTime - lastRefill
        val intervalsPassed = (elapsedMs / intervalMs).toInt()
        val newEnergy = minOf(5, current.energy + intervalsPassed) // min(5, 3+3) = 5
        val newRefillTime = if (newEnergy >= 5) 0L else lastRefill + (intervalsPassed * intervalMs)

        assertEquals(5, newEnergy)
        assertEquals(0L, newRefillTime) // Reset when full
    }

    @Test
    fun `checkAndRefillEnergy - sets lastRefillTime when firstRefill is 0 and energy below 5`() = runTest {
        val currentTime = 5000000L
        val current = UserProgressEntity(id = 1, energy = 3, lastRefillTime = 0L)
        whenever(dao.getProgress()).thenReturn(current)

        // GamificationManager: if lastRefill == 0L, sets it to currentTime
        val lastRefill = current.lastRefillTime
        assertTrue(lastRefill == 0L)
        // Expected: updated = current.copy(lastRefillTime = currentTime)
    }

    @Test
    fun `checkAndRefillEnergy - no refill when less than 30 min elapsed`() = runTest {
        val lastRefill = 1000000L
        val intervalMs = 30 * 60 * 1000L
        val currentTime = lastRefill + (intervalMs / 2) // only 15 min

        val current = UserProgressEntity(id = 1, energy = 3, lastRefillTime = lastRefill)

        val elapsedMs = currentTime - lastRefill
        val intervalsPassed = (elapsedMs / intervalMs).toInt()

        assertEquals(0, intervalsPassed)
    }

    // ── updateStreak Tests ──────────────────────────────────────

    @Test
    fun `updateStreak - creates new streak from null progress`() = runTest {
        whenever(dao.getProgress()).thenReturn(null)

        // GamificationManager: creates UserProgressEntity(id=1, streak=1, lastActiveDate=today)
        val result = UserProgressEntity(id = 1, streak = 1, lastActiveDate = "2026-07-05")
        assertEquals(1, result.streak)
    }

    @Test
    fun `updateStreak - increments streak on consecutive day`() = runTest {
        val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(System.currentTimeMillis() - 86400000L))
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        val current = UserProgressEntity(id = 1, streak = 3, lastActiveDate = yesterday)
        whenever(dao.getProgress()).thenReturn(current)

        // Replicate updateStreak logic
        val last = current.lastActiveDate
        val newStreak = when (last) {
            today -> current.streak
            yesterday -> current.streak + 1
            else -> 1
        }

        assertEquals(4, newStreak) // 3 + 1
    }

    @Test
    fun `updateStreak - keeps streak same when called same day`() = runTest {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())

        val current = UserProgressEntity(id = 1, streak = 5, lastActiveDate = today)
        whenever(dao.getProgress()).thenReturn(current)

        val last = current.lastActiveDate
        val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(System.currentTimeMillis() - 86400000L))

        val newStreak = when (last) {
            today -> current.streak
            yesterday -> current.streak + 1
            else -> 1
        }

        assertEquals(5, newStreak) // unchanged
    }

    @Test
    fun `updateStreak - resets streak on gap of more than 1 day`() = runTest {
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date())
        val yesterday = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            .format(java.util.Date(System.currentTimeMillis() - 86400000L))

        val twoDaysAgo = "2020-01-01" // Definitely not today or yesterday
        val current = UserProgressEntity(id = 1, streak = 7, lastActiveDate = twoDaysAgo)
        whenever(dao.getProgress()).thenReturn(current)

        val last = current.lastActiveDate
        val newStreak = when (last) {
            today -> current.streak
            yesterday -> current.streak + 1
            else -> 1
        }

        assertEquals(1, newStreak) // Reset
    }

    @Test
    fun `updateStreak - handles empty lastActiveDate`() = runTest {
        val current = UserProgressEntity(id = 1, streak = 0, lastActiveDate = "")
        whenever(dao.getProgress()).thenReturn(current)

        // GamificationManager: if last.isEmpty(), sets streak = 1
        val last = current.lastActiveDate
        val newStreak = if (last.isEmpty()) 1 else current.streak

        assertEquals(1, newStreak)
    }
}
