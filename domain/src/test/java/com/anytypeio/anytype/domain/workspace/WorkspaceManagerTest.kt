package com.anytypeio.anytype.domain.workspace

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import com.anytypeio.anytype.domain.debugging.Logger
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

class WorkspaceManagerTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var logger: Logger

    private lateinit var spaceManager: SpaceManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        spaceManager = SpaceManager.Impl(
            repo = repo,
            dispatchers = dispatchers,
            logger = logger
        )
    }

    @Test
    fun `should log space not ready errors as warning`() = runTest {
        val space = "space-id"
        val error = RuntimeException("space is not ready: check your internet connection and try again later")
        repo.stub {
            onBlocking { spaceOpen(space, false) } doThrow error
        }

        val result = spaceManager.set(space)

        assertTrue(result.isFailure)
        verify(logger).logWarning(
            "SPACE MANAGER: space is not ready: $space, reason: ${error.message}"
        )
        verify(logger, never()).logException(any<Throwable>())
        verify(logger, never()).logException(any<Throwable>(), any())
    }

    @Test
    fun `should log unexpected space open errors as exception`() = runTest {
        val space = "space-id"
        val error = IllegalStateException("unexpected")
        repo.stub {
            onBlocking { spaceOpen(space, false) } doThrow error
        }

        val result = spaceManager.set(space)

        assertTrue(result.isFailure)
        verify(logger).logException(error, "SPACE MANAGER: failed to open space: $space")
        verify(logger, never()).logWarning(
            "SPACE MANAGER: space is not ready: $space, reason: ${error.message}"
        )
    }
}
