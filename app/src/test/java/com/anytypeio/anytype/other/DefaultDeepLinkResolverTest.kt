package com.anytypeio.anytype.other

import android.os.Build
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertIs
import kotlin.test.assertTrue
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
}