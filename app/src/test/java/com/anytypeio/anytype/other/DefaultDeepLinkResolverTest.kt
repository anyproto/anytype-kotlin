package com.anytypeio.anytype.other

import android.os.Build
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class DefaultDeepLinkResolverTest {

    private val deepLinkResolver = DefaultDeepLinkResolver

    @Test
    fun `resolve returns Import Experience for import experience deep links`() {
        // Given
        val deeplink = "anytype://main/import/?type=experience123&source=source321"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Import.Experience(type = "experience123", source = "source321"), result)
    }

    @Test
    fun `resolve returns Invite with deeplink for invite deep links`() {
        // Given
        val deeplink = "https://invite.any.coop/bafybeibqdqtd65nlaey3mnkf24prpeq#DsESM2H2xi7Fs96XSp6YcaKaXASX"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assert(result is DeepLinkResolver.Action.Invite)
        assertEquals(deeplink, (result as DeepLinkResolver.Action.Invite).link)
    }

    @Test
    fun `resolve returns Unknown for unrecognized deep links`() {
        // Given
        val deeplink = "anytype://some_random_path"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Unknown, result)
    }
}