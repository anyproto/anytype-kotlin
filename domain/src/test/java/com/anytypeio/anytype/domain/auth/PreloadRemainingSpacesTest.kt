package com.anytypeio.anytype.domain.auth

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.common.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PreloadRemainingSpacesTest {

    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @Mock lateinit var repo: AuthRepository

    private lateinit var dispatchers: AppCoroutineDispatchers
    private lateinit var useCase: PreloadRemainingSpaces

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        useCase = PreloadRemainingSpaces(dispatchers, repo)
    }

    @Test
    fun `delegates to repository on success`() = runTest {
        useCase.async(Unit)
        verify(repo).preloadRemainingSpaces()
    }

    @Test
    fun `surfaces repository failure as Resultat Failure`() = runTest {
        repo.stub { onBlocking { preloadRemainingSpaces() } doThrow RuntimeException("ACCOUNT_IS_NOT_RUNNING") }
        val result = useCase.async(Unit)
        assertTrue(result is Resultat.Failure)
    }
}
