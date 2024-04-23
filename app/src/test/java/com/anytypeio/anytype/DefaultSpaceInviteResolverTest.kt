package com.anytypeio.anytype

import android.os.Build
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class DefaultSpaceInviteResolverTest {

    @Test
    fun `should parse file key and content id from deeplink`() {

        val fileKeyValue = "ae62abf2a19e"
        val cidValue = "75538c6c50351eca"

        val link =  "invite.any.coop/${cidValue}#${fileKeyValue}"

        assertEquals(
            expected = cidValue,
            actual = DefaultSpaceInviteResolver.parseContentId(link)
        )

        assertEquals(
            expected = fileKeyValue,
            actual = DefaultSpaceInviteResolver.parseFileKey(link)
        )
    }

    @Test
    fun `should parse file key and content id from deeplink with https`() {

        val fileKeyValue = "ae62abf2a19e"
        val cidValue = "75538c6c50351eca"

        val link =  "https://invite.any.coop/${cidValue}#${fileKeyValue}"

        assertEquals(
            expected = cidValue,
            actual = DefaultSpaceInviteResolver.parseContentId(link)
        )

        assertEquals(
            expected = fileKeyValue,
            actual = DefaultSpaceInviteResolver.parseFileKey(link)
        )
    }

    @Test
    fun `should parse file key and content id from deeplink with http`() {

        val fileKeyValue = "ae62abf2a19e"
        val cidValue = "75538c6c50351eca"

        val link =  "http://invite.any.coop/${cidValue}#${fileKeyValue}"

        assertEquals(
            expected = cidValue,
            actual = DefaultSpaceInviteResolver.parseContentId(link)
        )

        assertEquals(
            expected = fileKeyValue,
            actual = DefaultSpaceInviteResolver.parseFileKey(link)
        )
    }

    @Test
    fun `should parse file key and content id`() {
        val fileKeyValue = "ae62abf2a19e"
        val cidValue = "75538c6c50351eca"

        val link = "anytype://invite/?cid=$cidValue&key=$fileKeyValue"

        assertEquals(
            expected = cidValue,
            actual = DefaultSpaceInviteResolver.parseContentId(link)
        )

        assertEquals(
            expected = fileKeyValue,
            actual = DefaultSpaceInviteResolver.parseFileKey(link)
        )
    }

}