package com.anytypeio.anytype.domain.block.interactor

import com.anytypeio.anytype.core_models.CoroutineTestRule
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.GetCompatibleObjectTypes
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.stub
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

class GetCompatibleObjectTypesTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    var rule = CoroutineTestRule()

    @Mock
    lateinit var repository: BlockRepository

    lateinit var getCompatibleObjectTypes: GetCompatibleObjectTypes

    @Before
    fun before() {
        MockitoAnnotations.initMocks(this)
        getCompatibleObjectTypes = GetCompatibleObjectTypes(repository)
    }

    @Test
    fun shouldFilterResultBySmartBlockType() {

        val type1 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = "AAA",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.WORKSPACE),
            isArchived = true,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.DATE),
            isArchived = false,
            isReadOnly = false
        )

        val type4 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.SET),
            isArchived = false,
            isReadOnly = false
        )

        val type5 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = "ZZZ",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ARCHIVE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        repository.stub {
            onBlocking { getObjectTypes() } doReturn listOf(type1, type2, type3, type4, type5)
        }

        runBlocking {

            val params = GetCompatibleObjectTypes.Params(smartBlockType = SmartBlockType.PAGE)

            val expected = listOf(type1, type5)
            getCompatibleObjectTypes.run(params).process(
                failure = {},
                success = {
                    assertEquals(expected, it)
                }
            )
        }
    }

    @Test
    fun shouldSortResultByUrl() {

        val type1 = ObjectType(
            url = ObjectType.TEMPLATE_URL,
            name = "QName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = ObjectType.SET_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type4 = ObjectType(
            url = ObjectType.TASK_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type5 = ObjectType(
            url = ObjectType.NOTE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ARCHIVE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type6 = ObjectType(
            url = ObjectType.AUDIO_URL,
            name = "NName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ARCHIVE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type7 = ObjectType(
            url = ObjectType.RELATION_URL,
            name = "XName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ANYTYPE_PROFILE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type8 = ObjectType(
            url = "_customType",
            name = "AName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.CUSTOM_OBJECT_TYPE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        repository.stub {
            onBlocking { getObjectTypes() } doReturn listOf(
                type1,
                type2,
                type3,
                type4,
                type5,
                type6,
                type7,
                type8
            )
        }

        runBlocking {

            val params = GetCompatibleObjectTypes.Params(smartBlockType = SmartBlockType.PAGE)

            val expected = listOf(type3, type5, type2, type4, type8, type6, type1, type7)

            getCompatibleObjectTypes.run(params).process(
                failure = {},
                success = {
                    assertEquals(expected, it)
                }
            )
        }
    }

    @Test
    fun shouldSortResultByTypeUrlAndArchived() {

        val type1 = ObjectType(
            url = ObjectType.TEMPLATE_URL,
            name = "QName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = true,
            isReadOnly = false
        )

        val type2 = ObjectType(
            url = ObjectType.SET_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type3 = ObjectType(
            url = ObjectType.PAGE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type4 = ObjectType(
            url = ObjectType.TASK_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.PAGE),
            isArchived = true,
            isReadOnly = false
        )

        val type5 = ObjectType(
            url = ObjectType.NOTE_URL,
            name = MockDataFactory.randomString(),
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ARCHIVE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type6 = ObjectType(
            url = ObjectType.AUDIO_URL,
            name = "NName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ARCHIVE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        val type7 = ObjectType(
            url = ObjectType.RELATION_URL,
            name = "XName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.ANYTYPE_PROFILE),
            isArchived = false,
            isReadOnly = false
        )

        val type8 = ObjectType(
            url = "_customType",
            name = "AName",
            relations = emptyList(),
            layout = ObjectType.Layout.values().random(),
            emoji = MockDataFactory.randomString(),
            description = MockDataFactory.randomString(),
            isHidden = MockDataFactory.randomBoolean(),
            smartBlockTypes = listOf(SmartBlockType.CUSTOM_OBJECT_TYPE, SmartBlockType.PAGE),
            isArchived = false,
            isReadOnly = false
        )

        repository.stub {
            onBlocking { getObjectTypes() } doReturn listOf(
                type1,
                type2,
                type3,
                type4,
                type5,
                type6,
                type7,
                type8
            )
        }

        runBlocking {

            val params = GetCompatibleObjectTypes.Params(smartBlockType = SmartBlockType.PAGE)

            val expected = listOf(type3, type5, type2, type8, type6)

            getCompatibleObjectTypes.run(params).process(
                failure = {},
                success = {
                    assertEquals(expected, it)
                }
            )
        }
    }
}