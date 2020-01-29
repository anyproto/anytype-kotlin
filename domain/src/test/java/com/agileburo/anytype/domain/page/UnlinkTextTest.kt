package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.common.CoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals


class UnlinkTextTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    lateinit var checkForUnlink: CheckForUnlink

    @Before
    fun setup() {
        checkForUnlink = CheckForUnlink()
    }

    @Test
    fun `should return NothingToUnlinkException when url is empty`() {
        runBlocking {
            val params = CheckForUnlink.Params(link = "")

            val result = checkForUnlink.run(params)

            result.either(
                { throwable ->
                    assertEquals("No text to unlink", throwable.localizedMessage)
                },
                { b: Boolean ->
                    Assert.fail()
                })
        }
    }

    @Test
    fun `should return NothingToUnlinkException when url is null`() {
        runBlocking {
            val params = CheckForUnlink.Params( link = null)

            val result = checkForUnlink.run(params)

            result.either(
                { throwable ->
                    assertEquals("No text to unlink", throwable.localizedMessage)
                },
                { b: Boolean ->
                    Assert.fail()
                })
        }
    }
}