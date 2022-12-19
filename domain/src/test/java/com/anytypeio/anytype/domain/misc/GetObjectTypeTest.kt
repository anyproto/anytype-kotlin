package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class GetObjectTypeTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var usecase: GetObjectTypes

    private val defaultParams = GetObjectTypes.Params(
        filters = emptyList(),
        keys = listOf(Relations.ID),
        sorts = emptyList(),
        limit = 0,
        offset = 0,
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        usecase = GetObjectTypes(repo = repo)
    }

    @Test
    fun `should return all object types`() {

        val type1 = ObjectWrapper.Type(
            mapOf(Relations.ID to MockDataFactory.randomUuid())
        )

        val type2 = ObjectWrapper.Type(
            mapOf(Relations.ID to MockDataFactory.randomUuid())
        )

        val type3 = ObjectWrapper.Type(
            mapOf(Relations.ID to MockDataFactory.randomUuid())
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1, type2, type3))

            val firstTimeResult = usecase.invoke(params = defaultParams)
            firstTimeResult.either(
                { Assert.fail() },
                { results ->
                    assertEquals(
                        expected = listOf(type1, type2, type3),
                        actual = results
                    )
                }
            )
            val secondTimeResult = usecase.invoke(params = defaultParams)
            assertEquals(firstTimeResult, secondTimeResult)

            verify(repo, times(2)).searchObjects(
                filters = defaultParams.filters,
                keys = defaultParams.keys,
                sorts = defaultParams.sorts,
                limit = defaultParams.limit,
                offset = defaultParams.offset,
                fulltext = ""
            )
        }
    }

    private fun stubGetObjectTypes(types: List<ObjectWrapper.Type>) {
        usecase.stub {
            onBlocking {
                repo.searchObjects(
                    filters = emptyList(),
                    keys = listOf(Relations.ID),
                    sorts = emptyList(),
                    limit = 0,
                    offset = 0,
                    fulltext = ""
                )
            } doReturn types.map { it.map }
        }
    }
}