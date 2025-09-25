package se.onemanstudio.playaroundwithai.data.etc

import kotlinx.coroutines.delay

class MockedApi {
    suspend fun doFakeTask(task: String): String {
        delay(4000) // Simulate delay of 4 seconds
        return "$task completed."
    }
}