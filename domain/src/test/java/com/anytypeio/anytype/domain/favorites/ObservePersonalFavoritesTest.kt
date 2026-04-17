package com.anytypeio.anytype.domain.favorites

import com.anytypeio.anytype.core_models.primitives.SpaceId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.given
import java.util.UUID

class ObservePersonalFavoritesTest {

    @Mock
    lateinit var repo: PersonalFavoritesRepository

    private lateinit var usecase: ObservePersonalFavorites

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        usecase = ObservePersonalFavorites(repo)
    }

    @Test
    fun `emits repo flow values`() = runTest {
        val space = SpaceId(UUID.randomUUID().toString())
        val order = listOf("a", "b")
        given(repo.observe(space)).willReturn(flowOf(order))

        val result = usecase.build(ObservePersonalFavorites.Params(space)).first()

        assertEquals(order, result)
    }
}
