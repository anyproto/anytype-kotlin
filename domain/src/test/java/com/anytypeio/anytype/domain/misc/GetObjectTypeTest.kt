package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.util.dispatchers
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
        usecase = GetObjectTypes(repo = repo, dispatchers = dispatchers)
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

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertEquals(
                        expected = listOf(type1, type2, type3),
                        actual = results
                    )
                }
            )
            val secondTimeResult = usecase.execute(params = defaultParams)
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

    @Test
    fun `should return source object when Struct returns String`() {

        val sourceObject = MockDataFactory.randomUuid()
        val type1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                Relations.SOURCE_OBJECT to sourceObject
            )
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1))

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertEquals(
                        expected = sourceObject,
                        actual = results.first().sourceObject
                    )
                }
            )
        }
    }

    @Test
    fun `type should has source object when Struct returns list of one element`() {

        val sourceObject = MockDataFactory.randomUuid()
        val type1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                Relations.SOURCE_OBJECT to listOf(sourceObject)
            )
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1))

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertEquals(
                        expected = sourceObject,
                        actual = results.first().sourceObject
                    )
                }
            )
        }
    }

    @Test
    fun `type should has source object when Struct returns list of several elements`() {

        val sourceObject = MockDataFactory.randomUuid()
        val sourceObject2 = MockDataFactory.randomUuid()
        val sourceObject3 = MockDataFactory.randomUuid()
        val type1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                Relations.SOURCE_OBJECT to listOf(sourceObject, sourceObject2, sourceObject3)
            )
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1))

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertEquals(
                        expected = sourceObject,
                        actual = results.first().sourceObject
                    )
                }
            )
        }
    }

    @Test
    fun `type should has no source object when Struct returns empty list of`() {

        val type1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid(),
                Relations.SOURCE_OBJECT to emptyList<String>()
            )
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1))

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertNull(results.first().sourceObject)
                }
            )
        }
    }

    @Test
    fun `type should has no source object when Struct returns null`() {

        val type1 = ObjectWrapper.Type(
            mapOf(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.UNIQUE_KEY to MockDataFactory.randomUuid()
            )
        )

        runBlocking {
            stubGetObjectTypes(types = listOf(type1))

            val firstTimeResult = usecase.execute(params = defaultParams)
            firstTimeResult.fold(
                onFailure = { Assert.fail() },
                onSuccess = { results ->
                    assertNull(results.first().sourceObject)
                }
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