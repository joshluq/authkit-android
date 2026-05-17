package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.SessionData
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput

internal class SaveSessionDataUseCase(
    private val repository: TokenRepository,
    private val logger: Loggerkit
) : UseCase<SaveSessionDataUseCase.Input<*>, NoneOutput> {

    override suspend fun invoke(input: Input<*>): Result<NoneOutput> {
        return runCatching {
            val data = input.data

            @Suppress("UNCHECKED_CAST")
            val clazz = input.clazz as Class<SessionData>

            repository.saveSessionData(data, clazz)
            NoneOutput
        }.onFailure {
            logger.e("SaveSessionDataUseCase", "Error saving session data", it)
        }
    }

    data class Input<T : SessionData>(val data: T, val clazz: Class<T>) : UseCaseInput
}
