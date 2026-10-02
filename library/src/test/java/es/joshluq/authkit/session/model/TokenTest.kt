package es.joshluq.authkit.session.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TokenTest {
    @Test
    fun `token access preserves value but redacts toString`() {
        val secret = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.secretPayload"
        val token = Token.Access(secret)

        assertEquals(secret, token.value)
        assertEquals("Token.Access(value=***REDACTED***)", token.toString())
        assertFalse(token.toString().contains(secret))
    }

    @Test
    fun `token refresh preserves value but redacts toString`() {
        val secret = "refresh-secret-123456"
        val token = Token.Refresh(secret)

        assertEquals(secret, token.value)
        assertEquals("Token.Refresh(value=***REDACTED***)", token.toString())
        assertFalse(token.toString().contains(secret))
    }

    @Test
    fun `token custom preserves value but redacts toString`() {
        val secret = "custom-secret-xyz"
        val token = Token.Custom("id_token", secret)

        assertEquals("id_token", token.name)
        assertEquals(secret, token.value)
        assertEquals("Token.Custom(name=id_token, value=***REDACTED***)", token.toString())
        assertFalse(token.toString().contains(secret))
    }

    @Test
    fun `token equality and hashCode remain based on value`() {
        val secret = "shared-secret"
        val token1 = Token.Access(secret)
        val token2 = Token.Access(secret)
        val token3 = Token.Access("different-secret")

        assertEquals(token1, token2)
        assertEquals(token1.hashCode(), token2.hashCode())
        assertNotEquals(token1, token3)
    }

    @Test
    fun `tokenHolder clear removes all tokens`() {
        val holder = TokenHolder.withTokens(
            Token.Access("access-token"),
            Token.Refresh("refresh-token"),
            Token.Custom("custom", "custom-token"),
        )

        assertFalse(holder.isEmpty())
        assertTrue(holder.hasAccessToken())
        assertTrue(holder.hasRefreshToken())
        assertTrue(holder.hasCustomToken("custom"))

        holder.clear()

        assertTrue(holder.isEmpty())
        assertFalse(holder.hasAccessToken())
        assertFalse(holder.hasRefreshToken())
        assertFalse(holder.hasCustomToken("custom"))
    }
}
