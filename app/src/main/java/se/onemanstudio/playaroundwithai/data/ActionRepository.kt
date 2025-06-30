package se.onemanstudio.playaroundwithai.data

class ActionRepository(private val api: MockedApi = MockedApi()) {
    suspend fun simulateApi(task: String): String = api.doFakeTask(task)
}