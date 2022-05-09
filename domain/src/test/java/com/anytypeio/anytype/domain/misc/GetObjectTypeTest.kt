package com.anytypeio.anytype.domain.misc

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class GetObjectTypeTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    private lateinit var usecase: GetObjectTypes

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        usecase = GetObjectTypes(repo = repo)
    }

    @Test
    fun `should call repo only once if the first call was successful`() {

        val type = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        runBlocking {

            usecase.stub {
                onBlocking { repo.getObjectTypes() } doReturn listOf(type)
            }

            val params = GetObjectTypes.Params(filterArchivedObjects = true)

            val firstTimeResult = usecase.invoke(params = params)
            firstTimeResult.either(
                { Assert.fail() },
                { results ->
                    assertEquals(
                        expected = listOf(type),
                        actual = results
                    )
                }
            )
            val secondTimeResult = usecase.invoke(params = params)
            assertEquals(firstTimeResult, secondTimeResult)

            verify(repo, times(1)).getObjectTypes()
        }
    }

    @Test
    fun `should return not filtered object types`() {

        val type1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = true,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        runBlocking {

            usecase.stub {
                onBlocking { repo.getObjectTypes() } doReturn listOf(type1, type2, type3)
            }

            val params = GetObjectTypes.Params(filterArchivedObjects = true)

            val firstTimeResult = usecase.invoke(params = params)
            firstTimeResult.either(
                { Assert.fail() },
                { results ->
                    assertEquals(
                        expected = listOf(type1, type3),
                        actual = results
                    )
                }
            )
            val secondTimeResult = usecase.invoke(params = params)
            assertEquals(firstTimeResult, secondTimeResult)

            verify(repo, times(1)).getObjectTypes()
        }
    }

    @Test
    fun `should return all object types`() {

        val type1 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = true,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        runBlocking {

            usecase.stub {
                onBlocking { repo.getObjectTypes() } doReturn listOf(type1, type2, type3)
            }

            val params = GetObjectTypes.Params(filterArchivedObjects = false)

            val firstTimeResult = usecase.invoke(params = params)
            firstTimeResult.either(
                { Assert.fail() },
                { results ->
                    assertEquals(
                        expected = listOf(type1, type2, type3),
                        actual = results
                    )
                }
            )
            val secondTimeResult = usecase.invoke(params = params)
            assertEquals(firstTimeResult, secondTimeResult)

            verify(repo, times(1)).getObjectTypes()
        }
    }
}