package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.usecase.NoneInput
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase
import es.joshluq.foundationkit.usecase.UseCaseInput
import es.joshluq.foundationkit.usecase.UseCaseOutput

internal class ClearSessionUseCase(
    private val repository: TokenRepository,
    private val logger: Loggerkit
) : UseCase<NoneInput, NoneOutput> {

    companion object {
        private const val TAG = "ClearSessionUseCase"
    }

    override suspend fun invoke(input: NoneInput): Result<NoneOutput> {
        return runCatching {
            repository.clearAll()
            NoneOutput
        }.onFailure { throwable ->
            logger.e(TAG, "Error clearing session", throwable)
        }
    }
}