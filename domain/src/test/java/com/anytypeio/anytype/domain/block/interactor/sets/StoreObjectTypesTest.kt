package com.anytypeio.anytype.domain.block.interactor.sets

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.test_utils.MockDataFactory
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

class StoreObjectTypesTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var objectTypesProvider: ObjectTypesProvider

    private lateinit var storeObjectTypes: StoreObjectTypes

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        storeObjectTypes = StoreObjectTypes(
            repo = repo,
            objectTypesProvider = objectTypesProvider
        )
    }

    @Test
    fun `should save list of not filtered object types`() {

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
            storeObjectTypes.stub {
                onBlocking { repo.getObjectTypes() } doReturn listOf(type1, type2, type3)
            }

            val result = storeObjectTypes.run(Unit)

            result.either(
                fnL = { Assert.fail() },
                fnR = {}
            )

            val expected = listOf(type1, type2, type3)

            verify(objectTypesProvider, times(1)).set(expected)
        }
    }
}