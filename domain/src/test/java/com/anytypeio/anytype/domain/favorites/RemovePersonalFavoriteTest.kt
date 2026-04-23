package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import java.util.UUID

class RemovePersonalFavoriteTest {

    @Mock
    lateinit var repo: PersonalFavoritesRepository

    private lateinit var usecase: RemovePersonalFavorite

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        val dispatcher = UnconfinedTestDispatcher()
        val dispatchers = AppCoroutineDispatchers(
            io = dispatcher,
            computation = dispatcher,
            main = dispatcher
        )
        usecase = RemovePersonalFavorite(repo, dispatchers)
    }

    @Test
    fun `invokes repo remove with params`() = runTest {
        val space = SpaceId(UUID.randomUUID().toString())
        val target = UUID.randomUUID().toString()

        usecase.run(RemovePersonalFavorite.Params(space, target))

        verify(repo).remove(space, target)
    }
}
