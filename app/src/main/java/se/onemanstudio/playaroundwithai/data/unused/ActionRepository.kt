package se.onemanstudio.playaroundwithai.data.unused

class ActionRepository(private val api: MockedApi = MockedApi()) {
    suspend fun simulateApi(task: String): String = api.doFakeTask(task)
}