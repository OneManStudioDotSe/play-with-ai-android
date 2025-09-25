package se.onemanstudio.playaroundwithai.data.etc


class DoUseCase(private val repository: ActionRepository = ActionRepository()) {
    suspend fun execute(): String = repository.simulateApi("Suggesting activity")
}

class SeeUseCase(private val repository: ActionRepository = ActionRepository()) {
    suspend fun execute(): String = repository.simulateApi("Identifying plant")
}

class EatUseCase(private val repository: ActionRepository = ActionRepository()) {
    suspend fun execute(): String = repository.simulateApi("Finding food")
}
