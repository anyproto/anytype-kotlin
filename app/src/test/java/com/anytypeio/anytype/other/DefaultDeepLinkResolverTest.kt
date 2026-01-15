package com.anytypeio.anytype.other

import android.os.Build
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertIs
import kotlin.test.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun `resolve link to object deep link`() {
        // Given

        val obj = MockDataFactory.randomUuid()

        val space = MockDataFactory.randomUuid()

        val deeplink = "anytype://object?objectId=$obj&spaceId=$space"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.DeepLinkToObject(
                space = SpaceId(space),
                obj = obj
            ),
            result
        )
    }

    @Test
    fun `resolve link to object deep link with invite`() {

        val obj = "bafyreieaishyk22ik3v4m32zuy3uy2tmiv77fxogtjfv6adqfha4o3k3g4"

        val space = "bafyreid5qfqm4jsujeuebi3c4oca3hfezhoqazmm6jeg6b7rakcxj5jdse.hsqlz8alp6p8"

        val cid = "bafybeibwcymz6tpq7jydnhwrlouwazzjwu4idoq6sreqold47ovdb7pin4"

        val encryption = "8wujCkLqv6PiDGHA6iVWRkHpz5y4wF7iHLcrgkSPJqhb"

        val link = "anytype://object?objectId=$obj&spaceId=$space&cid=$cid&key=$encryption&route=Web"


        // When
        val result = deepLinkResolver.resolve(link)


        // Then
        assertEquals(
            DeepLinkResolver.Action.DeepLinkToObject(
                space = SpaceId(space),
                obj = obj,
                invite = DeepLinkResolver.Action.DeepLinkToObject.Invite(
                    cid = cid,
                    key = encryption
                )
            ),
            result
        )
    }

    @Test
    fun `resolve https deep link to object`() {
        // Given

        val obj = MockDataFactory.randomUuid()

        val space = MockDataFactory.randomUuid()

        val deeplink = "https://object.any.coop/$obj?spaceId=$space"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.DeepLinkToObject(
                space = SpaceId(space),
                obj = obj
            ),
            result
        )
    }

    @Test
    fun `resolve https deep link to object with invite`() {
        // Given

        val obj = MockDataFactory.randomUuid()

        val space = MockDataFactory.randomUuid()

        val cid = MockDataFactory.randomUuid()

        val encryption = MockDataFactory.randomUuid()

        val invite = "$cid#$encryption"

        val deeplink = "https://object.any.coop/$obj?spaceId=$space&inviteId=$invite"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.DeepLinkToObject(
                space = SpaceId(space),
                obj = obj,
                invite = DeepLinkResolver.Action.DeepLinkToObject.Invite(
                    cid = cid,
                    key = encryption
                )
            ),
            result
        )
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

    @Test
    fun `check is deeplink`() {
        val link1 = "https://object.any.coop/bafyreidqrtef4gcqjepl4llsy2xbauedtcdmjkz5xl4stfgt2ippbwveoq?spaceId=bafyreifj6nkvzfowzgfzu5ns4j54gjh5tmcxa3wd6rncbiuk3opq6o4244.1u5jgzo8m1ekc"

        assertTrue(
            deepLinkResolver.isDeepLink(link1)
        )
    }

    @Test
    fun `should resolve invite`() {
        val invite = "anytype://invite/?cid=bafybeibl3c5eqptcom5l5hjj5x6hkids6ayljykujamvt3bt4fcvuuebdy&key=Z7qado84JzbmwZhXBf53dYiCyNAEERyRWwwiHbBgWMv"
        assertTrue(
            deepLinkResolver.isDeepLink(invite)
        )
        assertIs<DeepLinkResolver.Action.Invite>(deepLinkResolver.resolve(invite))
    }

    @Test
    fun `resolve returns InitiateOneToOneChat for valid hi any coop links`() {
        // Given
        val identity = "abc123identity"
        val metadataKey = "xyz789metadatakey"
        val deeplink = "https://hi.any.coop/$identity#$metadataKey"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = metadataKey
            ),
            result
        )
    }

    @Test
    fun `isDeepLink returns true for hi any coop links`() {
        // Given
        val deeplink = "https://hi.any.coop/someidentity#somemetadatakey"

        // When & Then
        assertTrue(deepLinkResolver.isDeepLink(deeplink))
    }

    @Test
    fun `isDeepLink returns falsy for wrong hi any coop links`() {
        // Given
        val deeplink = "https://hii.any.coop/someidentity#somemetadatakey"

        // When & Then
        assertFalse(deepLinkResolver.isDeepLink(deeplink))
    }

    @Test
    fun `resolve returns Unknown for hi any coop link without fragment`() {
        // Given
        val deeplink = "https://hi.any.coop/identity"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Unknown, result)
    }

    @Test
    fun `resolve returns InitiateOneToOneChat for anytype hi deeplink`() {
        // Given
        val identity = "abc123identity"
        val metadataKey = "xyz789metadatakey"
        val deeplink = "anytype://hi/?id=$identity&key=$metadataKey"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = metadataKey
            ),
            result
        )
    }

    @Test
    fun `isDeepLink returns true for anytype hi deeplink`() {
        // Given
        val deeplink = "anytype://hi/?id=someidentity&key=somemetadatakey"

        // When & Then
        assertTrue(deepLinkResolver.isDeepLink(deeplink))
    }

    @Test
    fun `resolve returns Unknown for anytype hi deeplink without key`() {
        // Given
        val deeplink = "anytype://hi/?id=identity"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(DeepLinkResolver.Action.Unknown, result)
    }

    @Test
    fun `resolve correctly decodes URL-encoded metadata key in hi any coop link`() {
        // Given - real-world example with percent-encoded base64 characters
        val identity = "AAWfdD5bD5EPqfseF3Ze1q7jnZMjNBsH4xYa7DHVnALQxWe8"
        val encodedMetadataKey = "CAISIM%2FbGH7Zx33A%2BVyTVrfhXSjcmN%2F%2B9Z2MBCZ42rZ2%2FgRF"
        val decodedMetadataKey = "CAISIM/bGH7Zx33A+VyTVrfhXSjcmN/+9Z2MBCZ42rZ2/gRF"
        val deeplink = "https://hi.any.coop/$identity#$encodedMetadataKey"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then
        assertEquals(
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = decodedMetadataKey
            ),
            result
        )
    }

    @Test
    fun `isDeepLink returns true for URL-encoded hi any coop link`() {
        // Given - real-world example with percent-encoded characters
        val deeplink = "https://hi.any.coop/AAWfdD5bD5EPqfseF3Ze1q7jnZMjNBsH4xYa7DHVnALQxWe8#CAISIM%2FbGH7Zx33A%2BVyTVrfhXSjcmN%2F%2B9Z2MBCZ42rZ2%2FgRF"

        // When & Then
        assertTrue(deepLinkResolver.isDeepLink(deeplink))
    }

    @Test
    fun `resolve preserves literal plus in iOS format hi any coop link`() {
        // Given - iOS format with literal '+' (not percent-encoded)
        val identity = "A8QyNvSpUiw8iPcXbkdJBLNUVDzrf1MvfP2wG5Y32G6A4QF4"
        val metadataKey = "CAISIEnCArW+34o89A+CoMStH9OPKBML4SWLbxChduxnIypA"
        val deeplink = "https://hi.any.coop/$identity#$metadataKey"

        // When
        val result = deepLinkResolver.resolve(deeplink)

        // Then - '+' should be preserved, not converted to space
        assertEquals(
            DeepLinkResolver.Action.InitiateOneToOneChat(
                identity = identity,
                metadataKey = metadataKey
            ),
            result
        )
    }
}