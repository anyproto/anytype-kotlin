package com.anytypeio.anytype

import android.os.Build
import com.anytypeio.anytype.other.DefaultSpaceInviteResolver
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
class Test {

    @Test
    fun test() {
        val fileKeyValue = MockDataFactory.randomUuid()
        val cidValue = MockDataFactory.randomUuid()

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