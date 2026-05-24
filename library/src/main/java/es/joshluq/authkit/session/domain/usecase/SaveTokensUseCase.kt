package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput

internal class SaveTokensUseCase(
    private val repository: TokenRepository,
    private val logger: LoggerKit
) : UseCase<SaveTokensUseCase.Input, NoneOutput> {

    companion object {
        private const val TAG = "SaveTokensUseCase"
    }

    override suspend fun invoke(input: Input): Result<NoneOutput> {
        return runCatching {
            val tokens = input.tokens
            require(!tokens.isEmpty()) {
                "Tokens cannot be empty"
            }
            repository.saveTokens(tokens)
            NoneOutput
        }.onFailure { throwable ->
            logger.e(TAG, "Error saving tokens", throwable)
        }
    }

    data class Input(val tokens: TokenHolder) : UseCaseInput
}
