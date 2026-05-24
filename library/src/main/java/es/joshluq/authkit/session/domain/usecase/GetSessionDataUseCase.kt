package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.SessionData
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class GetSessionDataUseCase(
    private val repository: TokenRepository,
    private val logger: LoggerKit
) : UseCase<GetSessionDataUseCase.Input<*>, GetSessionDataUseCase.Output<*>> {

    override suspend fun invoke(input: Input<*>): Result<Output<*>> {
        return runCatching {
            @Suppress("UNCHECKED_CAST")
            val clazz = input.clazz as Class<SessionData>
            val data = repository.getSessionData(clazz)
            Output(data)
        }.onFailure {
            logger.e("GetSessionDataUseCase", "Error retrieving session data", it)
        }
    }

    data class Input<T : SessionData>(val clazz: Class<T>) : UseCaseInput
    data class Output<T : SessionData>(val data: T?) : UseCaseOutput
}
