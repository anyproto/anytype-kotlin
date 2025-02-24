package com.anytypeio.anytype.feature_object_type

import android.util.Log
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class TestFieldsMappping {

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    val space = MockDataFactory.randomUuid()

    val field1 = StubRelationObject(
        id = "field1_id",
        key = "field1_key",
        name = "Field 1, text",
        format = RelationFormat.LONG_TEXT,
        spaceId = space
    )

    val field2 = StubRelationObject(
        id = "field2_id",
        key = "field2_key",
        name = "Field 2, number",
        format = RelationFormat.NUMBER,
        spaceId = space
    )

    val field3 = StubRelationObject(
        id = "field3_id",
        key = "field3_key",
        name = "Field 3, date",
        format = RelationFormat.DATE,
        spaceId = space
    )

    val field4 = StubRelationObject(
        id = "field4_id",
        key = "field4_key",
        name = "Field 4, checkbox",
        format = RelationFormat.CHECKBOX,
        spaceId = space
    )

    val field5 = StubRelationObject(
        id = "field5_id",
        key = "field5_key",
        name = "Field 5, Status",
        format = RelationFormat.STATUS,
        spaceId = space
    )

    val fieldCreatedDate = StubRelationObject(
        id = "bafyreihas6lc5knc67lbeohaxjgfjzi3oazs2yvh7gbotcktefjynjqndq",
        key = Relations.CREATED_DATE,
        name = "Field Creation date",
        format = RelationFormat.DATE,
        isHidden = false,
        isReadOnly = true,
        isReadOnlyValue = true,
        spaceId = space,
        sourceObject = "_brcreatedDate"
    )

    val fieldAssigneeObjType1 = StubObjectType()
    val fieldAssigneeObjType2 = StubObjectType()

    val fieldAssignee = StubRelationObject(
        id = "bafyreibrqycr2w5q2db76f5l6hxfljwrgkrpqbulks6ppxsfu4hq5lwmue",
        key = "assignee",
        name = "Field Assignee",
        format = RelationFormat.OBJECT,
        objectTypes = listOf(
            fieldAssigneeObjType1.id,
            fieldAssigneeObjType2.id
        ),
        isHidden = false,
        isReadOnly = false,
        isReadOnlyValue = false,
        spaceId = space,
        sourceObject = "_brassignee"
    )

    val allSpaceRelations =
        listOf(field1, field2, field3, field4, field5, fieldCreatedDate, fieldAssignee)

    val featuredFields = listOf(field1, field2, field5).map { it.id }

    val sidebarFields = listOf(field3, field4, field5).map { it.id }

    val hiddenFields = listOf(field5).map { it.id }

    val testObjectType = StubObjectType(
        id = "test_object_type_id",
        uniqueKey = "test_object_type_unique_key",
        name = "Test custom object type",
        recommendedRelations = sidebarFields,
        recommendedFeaturedRelations = featuredFields,
        recommendedHiddenRelations = hiddenFields,
        layout = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
        recommendedLayout = ObjectType.Layout.TODO.code.toDouble(),
        space = space
    )

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var logger: Logger

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var gateway: Gateway

    lateinit var storeOfRelations: StoreOfRelations

    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    lateinit var fieldParser: FieldParser

    private lateinit var urlBuilder: UrlBuilder

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        storeOfRelations = DefaultStoreOfRelations()
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        urlBuilder = UrlBuilder(gateway)
        fieldParser =
            FieldParserImpl(
                dateProvider = dateProvider,
                logger = logger,
                getDateObjectByTimestamp = getDateObjectByTimestamp,
                stringResourceProvider = stringResourceProvider
            )
    }

    @Test
    fun `should filter featured fields by hidden`() = runTest {

        storeOfRelations.apply {
            merge(allSpaceRelations)
        }

        storeOfObjectTypes.apply {
            merge(listOf(testObjectType, fieldAssigneeObjType2, fieldAssigneeObjType1))
        }

        val parsedFields = fieldParser.getObjectTypeParsedFields(
            objectType = testObjectType,
            storeOfRelations = storeOfRelations,
            objectTypeConflictingFieldsIds = listOf()
        )

        assertEquals(
            expected = listOf(field1, field2),
            actual = parsedFields.featured
        )
    }

    @Test
    fun `should filter sidebar fields by hidden`() = runTest {

        storeOfRelations.apply {
            merge(allSpaceRelations)
        }

        storeOfObjectTypes.apply {
            merge(listOf(testObjectType, fieldAssigneeObjType2, fieldAssigneeObjType1))
        }

        val parsedFields = fieldParser.getObjectTypeParsedFields(
            objectType = testObjectType,
            storeOfRelations = storeOfRelations,
            objectTypeConflictingFieldsIds = listOf()
        )

        assertEquals(
            expected = listOf(field3, field4),
            actual = parsedFields.sidebar
        )
    }

    @Test
    fun `should map hidden fields`() = runTest {

        storeOfRelations.apply {
            merge(allSpaceRelations)
        }

        storeOfObjectTypes.apply {
            merge(listOf(testObjectType, fieldAssigneeObjType2, fieldAssigneeObjType1))
        }

        val parsedFields = fieldParser.getObjectTypeParsedFields(
            objectType = testObjectType,
            storeOfRelations = storeOfRelations,
            objectTypeConflictingFieldsIds = listOf()
        )

        assertEquals(
            expected = listOf(field5),
            actual = parsedFields.hidden
        )
    }
}