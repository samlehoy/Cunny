package com.eleonorez.cunny

import com.eleonorez.cunny.data.response.LearningMaterial
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testNeuronSandboxLogic() {
        val wRed = 3
        val wSpots = 3
        val threshold = 5

        val testCases = listOf(
            Triple(0, 0, 0),
            Triple(1, 0, 0),
            Triple(0, 1, 0),
            Triple(1, 1, 1)
        )

        for ((red, spots, expected) in testCases) {
            val sum = red * wRed + spots * wSpots
            val pred = if (sum >= threshold) 1 else 0
            assertEquals("Failed for red=$red, spots=$spots", expected, pred)
        }
    }

    @Test
    fun testLiveApiCategoryCourses() {
        val apiService = com.eleonorez.cunny.data.retrofit.ApiConfig.getApiService()
        val response = kotlinx.coroutines.runBlocking {
            try {
                apiService.getCategoryCourses("ai", "id")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        assertNotNull("Response should not be null", response)
        assertFalse("Response should not have error", response?.error ?: true)
        assertNotNull("Courses should not be null", response?.courses)
        assertTrue("Courses should not be empty", response?.courses?.isNotEmpty() ?: false)
    }

    @org.junit.Ignore("Skipped: hits live API, flaky in CI/offline — not a real unit test")
    @Test
    fun testLiveRepositoryCategoryCourses() {
        val apiService = com.eleonorez.cunny.data.retrofit.ApiConfig.getApiService()
        val repo = com.eleonorez.cunny.data.repository.CourseRepository(apiService)
        val result = kotlinx.coroutines.runBlocking {
            repo.getCategoryCourses("ai", "id")
        }
        assertTrue("Repository call should succeed", result.isSuccess)
        val list = result.getOrNull()
        assertNotNull("Courses list should not be null", list)
        assertTrue("Courses list should not be empty", list?.isNotEmpty() ?: false)
    }
}
