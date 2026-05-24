package es.joshluq.authkit.session.domain.usecase

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.foundationkit.log.LoggerKit
import es.joshluq.foundationkit.usecase.NoneInput
import es.joshluq.foundationkit.usecase.NoneOutput
import es.joshluq.foundationkit.usecase.UseCase

internal class ClearSessionUseCase(
    private val repository: TokenRepository,
    private val logger: LoggerKit
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
