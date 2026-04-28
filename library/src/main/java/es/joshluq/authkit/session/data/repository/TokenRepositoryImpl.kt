package es.joshluq.authkit.session.data.repository

import es.joshluq.authkit.session.domain.repository.TokenRepository
import es.joshluq.authkit.session.model.TokenHolder
import es.joshluq.foundationkit.log.Loggerkit
import es.joshluq.foundationkit.provider.StorageProvider
import es.joshluq.foundationkit.provider.read
import es.joshluq.foundationkit.provider.save

internal class TokenRepositoryImpl(
    private val storage: StorageProvider,
    private val logger: Loggerkit
) : TokenRepository {

    companion object {
        private const val TAG = "TokenHolder"
        private const val TOKENS_KEY = "TOKENS_KEY"
    }

    override fun getTokens(): TokenHolder {
        logger.d(TAG, "Reading tokens from storage")
        return requireNotNull(storage.read<TokenHolder>(TOKENS_KEY)) { "No tokens found in storage" }
    }

    override fun saveTokens(tokens: TokenHolder) {
        logger.d(TAG, "Saving tokens to storage")
        storage.save<TokenHolder>(TOKENS_KEY, tokens)
    }

    override fun clearAll() {
        logger.d(TAG, "Clearing all tokens")
        storage.clear()
    }
}