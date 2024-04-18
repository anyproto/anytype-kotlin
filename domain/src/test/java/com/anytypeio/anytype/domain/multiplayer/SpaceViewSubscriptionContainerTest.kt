package com.anytypeio.anytype.domain.multiplayer

import app.cash.turbine.test
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class SpaceViewSubscriptionContainerTest {

    @Mock
    lateinit var container: SpaceViewSubscriptionContainer

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun `should reach limit if limit is 0`() = runTest {

        val defaultSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        val privateSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.PRIVATE
        )

        container.stub {
            on {
                observe()
            } doReturn flowOf(
                listOf(defaultSpaceView, privateSpaceView)
            )
        }

        container.isSharingLimitReached().test {
            val result = awaitItem()
            assertEquals(
                expected = true,
                actual = result
            )
            awaitComplete()
        }
    }

    @Test
    fun `should not reach limit if limit is 1 and there is one private space and zero shared spaces`() = runTest {

        val defaultSpaceView = StubSpaceView(
            sharedSpaceLimit = 1,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        val privateSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.PRIVATE
        )

        container.stub {
            on {
                observe()
            } doReturn flowOf(
                listOf(defaultSpaceView, privateSpaceView)
            )
        }

        container.isSharingLimitReached().test {
            val result = awaitItem()
            assertEquals(
                expected = false,
                actual = result
            )
            awaitComplete()
        }
    }

    @Test
    fun `should reach limit if limit is 1 and there is one shared space already`() = runTest {

        val defaultSpaceView = StubSpaceView(
            sharedSpaceLimit = 1,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        val privateSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.SHARED
        )

        container.stub {
            on {
                observe()
            } doReturn flowOf(
                listOf(defaultSpaceView, privateSpaceView)
            )
        }

        container.isSharingLimitReached().test {
            val result = awaitItem()
            assertEquals(
                expected = true,
                actual = result
            )
            awaitComplete()
        }
    }

    @Test
    fun `should not reach limit if limit is 2 and there is one shared space already`() = runTest {

        val defaultSpaceView = StubSpaceView(
            sharedSpaceLimit = 2,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        val privateSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.SHARED
        )

        container.stub {
            on {
                observe()
            } doReturn flowOf(
                listOf(defaultSpaceView, privateSpaceView)
            )
        }

        container.isSharingLimitReached().test {
            val result = awaitItem()
            assertEquals(
                expected = false,
                actual = result
            )
            awaitComplete()
        }
    }

    @Test
    fun `should consider share limits from default space is one private space already`() = runTest {

        val defaultSpaceView = StubSpaceView(
            sharedSpaceLimit = 0,
            spaceAccessType = SpaceAccessType.DEFAULT
        )

        val privateSpaceView = StubSpaceView(
            sharedSpaceLimit = 2,
            spaceAccessType = SpaceAccessType.PRIVATE
        )

        container.stub {
            on {
                observe()
            } doReturn flowOf(
                listOf(defaultSpaceView, privateSpaceView)
            )
        }

        container.isSharingLimitReached().test {
            val result = awaitItem()
            assertEquals(
                expected = true,
                actual = result
            )
            awaitComplete()
        }
    }
}