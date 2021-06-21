package com.anytypeio.anytype.domain.dashboard

import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.domain.common.MockDataFactory
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.domain.dashboard.interactor.OpenDashboard
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class OpenDashboardTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var usecase: OpenDashboard

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        usecase = OpenDashboard(repo = repo)
    }

    @Test
    fun `should open a dashboard based on the given params if these are present`() =
        runBlockingTest {

            val params = OpenDashboard.Param(
                contextId = MockDataFactory.randomUuid(),
                id = MockDataFactory.randomUuid()
            )

            usecase.run(params)

            verify(repo, never()).getConfig()
            verify(repo, times(1)).openDashboard(contextId = params.contextId, id = params.id)
            verifyNoMoreInteractions(repo)
        }

    @Test
    fun `should open a home dashboard if there are no params`() = runBlockingTest {

        val config = Config(
            home = MockDataFactory.randomUuid(),
            gateway = MockDataFactory.randomString(),
            profile = MockDataFactory.randomUuid()
        )

        repo.stub {
            onBlocking { getConfig() } doReturn config
        }

        usecase.run(null)

        verify(repo, times(1)).getConfig()
        verify(repo, times(1)).openDashboard(
            contextId = config.home,
            id = config.home
        )
        verifyNoMoreInteractions(repo)
    }
}