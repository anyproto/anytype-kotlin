package com.anytypeio.anytype.other

import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultDeepLinkResolverTest {

    private val deepLinkResolver = DefaultDeepLinkResolver

    @Test
    fun `resolve returns Import Experience for import experience deeplinks`() {
        // Given
        val deeplink = "anytype://main/import/?type=experience"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Import.Experience, result)
    }

    @Test
    fun `resolve returns Invite with deeplink for invite deeplinks`() {
        // Given
        val deeplink = "anytype://invite/some_unique_identifier"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assert(result is DeepLinkResolver.Action.Invite)
        assertEquals(deeplink, (result as DeepLinkResolver.Action.Invite).link)
    }

    @Test
    fun `resolve returns Unknown for unrecognized deeplinks`() {
        // Given
        val deeplink = "anytype://some_random_path"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Unknown, result)
    }
}