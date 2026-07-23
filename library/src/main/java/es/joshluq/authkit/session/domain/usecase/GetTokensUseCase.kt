package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.usecase.NoneInput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class GetTokensUseCase(
    private val repository: TokenRepository,
    private val logger: LoggerKit
) : UseCase<NoneInput, GetTokensUseCase.Output> {

    companion object {
        private const val TAG = "GetTokenUseCase"
    }

    override suspend fun invoke(input: NoneInput): Result<Output> {
        return runCatching {
            val token = repository.getTokens()
            Output(tokens = token)
        }.onFailure { throwable ->
            logger.e(TAG, "Error retrieving tokens ${throwable.message}")
        }
    }

    data class Output(val tokens: TokenHolder) : UseCaseOutput
}
