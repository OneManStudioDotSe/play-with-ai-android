package se.onemanstudio.playaroundwithai.data.etc

class ActionRepository(private val api: MockedApi = MockedApi()) {
    suspend fun simulateApi(task: String): String = api.doFakeTask(task)
}