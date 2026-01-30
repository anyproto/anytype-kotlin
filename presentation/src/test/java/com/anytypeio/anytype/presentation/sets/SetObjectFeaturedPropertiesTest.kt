package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubFeatured
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.GetDateObjectByTimestamp
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.FieldParserImpl
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.objects.ConflictResolutionStrategy
import com.anytypeio.anytype.presentation.objects.toFeaturedPropertiesViews
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class SetObjectFeaturedPropertiesTest {

    @Mock
    lateinit var urlBuilder: UrlBuilder

    lateinit var fieldParser: FieldParser

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var getDateObjectByTimestamp: GetDateObjectByTimestamp

    @Mock
    lateinit var stringResourceProvider: StringResourceProvider

    @Mock
    lateinit var logger: Logger

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }

    private val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val storeOfObjectTypes: StoreOfObjectTypes = DefaultStoreOfObjectTypes()

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        fieldParser =
            FieldParserImpl(dateProvider, logger, getDateObjectByTimestamp, stringResourceProvider)
        urlBuilder.stub {
            on { large(any()) } doReturn "any/url"
        }
    }

    @Test
    fun `case when without conflict`() = runTest {

        val propertyObjectType = StubRelationObject(
            id = "propertyObjectType_id",
            name = "Object type",
        )
        val propertyTag = StubRelationObject(
            id = "propertyTag_id",
            name = "Tag",
        )
        val propertyBacklinks = StubRelationObject(
            id = "propertyBacklinks_id",
            name = "Backlinks",
        )
        val propertyDescription = StubRelationObject(
            id = "propertyDescription_id",
            name = "Description",
            key = Relations.DESCRIPTION
        )

        storeOfRelations.merge(
            relations = listOf(
                propertyObjectType,
                propertyTag,
                propertyBacklinks,
                propertyDescription
            )
        )

        val objType = StubObjectType(
            name = "Query",
            uniqueKey = ObjectTypeIds.SET,
            recommendedLayout = ObjectType.Layout.SET.code.toDouble(),
            recommendedFeaturedRelations = listOf(
                propertyObjectType.id,
                propertyTag.id,
                propertyBacklinks.id,
                propertyDescription.id
            )
        )

        storeOfObjectTypes.merge(
            types = listOf(objType)
        )

        val objectSet = StubObject(
            id = "id",
            name = "Pages",
            description = "This the description of Pages Set",
            objectType = objType.id,
            extraFields = mapOf(
                Relations.FEATURED_RELATIONS to propertyDescription.key
            )
        )

        val featuredBlock = StubFeatured()

        val objectState = ObjectState.DataView.Set(
            root = objectSet.id,
            blocks = listOf(featuredBlock),
            details = ObjectViewDetails(
                details = mapOf(
                    objectSet.id to objectSet.map,
                    objType.id to objType.map,
                )
            )
        )

        val featuredPropertiesBlock = toFeaturedPropertiesViews(
            objectId = objectSet.id,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            details = objectState.details,
            blocks = objectState.blocks,
            participantCanEdit = true
        )

        assertEquals(
            expected = listOf(propertyObjectType.key, propertyTag.key, propertyBacklinks.key),
            actual = featuredPropertiesBlock!!.relations.map { it.key })

        assertEquals(
            expected = false,
            actual = featuredPropertiesBlock.hasFeaturePropertiesConflict
        )
    }

    @Test
    fun `case when with conflict, using default Strategy - OBJECT_ONLY`() = runTest {

        val propertyObjectType = StubRelationObject(
            id = "propertyObjectType_id",
            name = "Object type",
            key = Relations.TYPE
        )
        val propertyTag = StubRelationObject(
            id = "propertyTag_id",
            name = "Tag",
            key = "key-tag"
        )
        val propertyBacklinks = StubRelationObject(
            id = "propertyBacklinks_id",
            name = "Backlinks",
            key = "key-backlinks"
        )
        val propertyDescription = StubRelationObject(
            id = "propertyDescription_id",
            name = "Description",
            key = Relations.DESCRIPTION
        )

        val propertyAuthor = StubRelationObject(
            id = "propertyAuthor_id",
            name = "Author",
            key = "key-author"
        )

        storeOfRelations.merge(
            relations = listOf(
                propertyObjectType,
                propertyTag,
                propertyBacklinks,
                propertyDescription,
                propertyAuthor
            )
        )

        val objType = StubObjectType(
            name = "Query",
            uniqueKey = ObjectTypeIds.SET,
            recommendedLayout = ObjectType.Layout.SET.code.toDouble(),
            recommendedFeaturedRelations = listOf(
                propertyObjectType.id,
                propertyTag.id,
                propertyBacklinks.id,
                propertyDescription.id
            )
        )

        storeOfObjectTypes.merge(
            types = listOf(objType)
        )

        val objectSet = StubObject(
            id = "id",
            name = "Pages",
            description = "This the description of Pages Set",
            objectType = objType.id,
            extraFields = mapOf(
                Relations.FEATURED_RELATIONS to listOf(
                    propertyBacklinks.key,
                    propertyAuthor.key,
                    propertyDescription.key
                )
            )
        )

        val featuredBlock = StubFeatured()

        val objectState = ObjectState.DataView.Set(
            root = objectSet.id,
            blocks = listOf(featuredBlock),
            details = ObjectViewDetails(
                details = mapOf(
                    objectSet.id to objectSet.map,
                    objType.id to objType.map,
                )
            )
        )

        val featuredPropertiesBlock = toFeaturedPropertiesViews(
            objectId = objectSet.id,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            details = objectState.details,
            blocks = objectState.blocks,
            participantCanEdit = true
        )

        assertEquals(
            expected = true,
            actual = featuredPropertiesBlock!!.hasFeaturePropertiesConflict
        )

        assertEquals(
            expected = listOf(
                propertyBacklinks.key,
                propertyAuthor.key
            ),
            actual = featuredPropertiesBlock.relations.map { it.key })
    }

    @Test
    fun `case when with conflict, using Strategy - MERGE`() = runTest {

        val propertyObjectType = StubRelationObject(
            id = "propertyObjectType_id",
            name = "Object type",
            key = Relations.TYPE
        )
        val propertyTag = StubRelationObject(
            id = "propertyTag_id",
            name = "Tag",
            key = "key-tag"
        )
        val propertyBacklinks = StubRelationObject(
            id = "propertyBacklinks_id",
            name = "Backlinks",
            key = "key-backlinks"
        )
        val propertyDescription = StubRelationObject(
            id = "propertyDescription_id",
            name = "Description",
            key = Relations.DESCRIPTION
        )

        val propertyAuthor = StubRelationObject(
            id = "propertyAuthor_id",
            name = "Author",
            key = "key-author"
        )

        storeOfRelations.merge(
            relations = listOf(
                propertyObjectType,
                propertyTag,
                propertyBacklinks,
                propertyDescription,
                propertyAuthor
            )
        )

        val objType = StubObjectType(
            name = "Query",
            uniqueKey = ObjectTypeIds.SET,
            recommendedLayout = ObjectType.Layout.SET.code.toDouble(),
            recommendedFeaturedRelations = listOf(
                propertyObjectType.id,
                propertyTag.id,
                propertyBacklinks.id,
                propertyDescription.id
            )
        )

        storeOfObjectTypes.merge(
            types = listOf(objType)
        )

        val objectSet = StubObject(
            id = "id",
            name = "Pages",
            description = "This the description of Pages Set",
            objectType = objType.id,
            extraFields = mapOf(
                Relations.FEATURED_RELATIONS to listOf(
                    propertyBacklinks.key,
                    propertyAuthor.key,
                    propertyDescription.key
                )
            )
        )

        val featuredBlock = StubFeatured()

        val objectState = ObjectState.DataView.Set(
            root = objectSet.id,
            blocks = listOf(featuredBlock),
            details = ObjectViewDetails(
                details = mapOf(
                    objectSet.id to objectSet.map,
                    objType.id to objType.map,
                )
            )
        )

        val featuredPropertiesBlock = toFeaturedPropertiesViews(
            objectId = objectSet.id,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            details = objectState.details,
            blocks = objectState.blocks,
            participantCanEdit = true,
            conflictResolutionStrategy = ConflictResolutionStrategy.MERGE
        )

        assertEquals(
            expected = true,
            actual = featuredPropertiesBlock!!.hasFeaturePropertiesConflict
        )

        assertEquals(
            expected = listOf(
                propertyObjectType.key,
                propertyTag.key,
                propertyBacklinks.key,
                propertyAuthor.key
            ),
            actual = featuredPropertiesBlock!!.relations.map { it.key })
    }

    @Test
    fun `should not has a conflict, when all featured are presented in type recommended`() =
        runTest {

            val propertyObjectType = StubRelationObject(
                id = "propertyObjectType_id",
                name = "Object type",
                key = Relations.TYPE
            )
            val propertyTag = StubRelationObject(
                id = "propertyTag_id",
                name = "Tag",
                key = "key-tag"
            )
            val propertyBacklinks = StubRelationObject(
                id = "propertyBacklinks_id",
                name = "Backlinks",
                key = "key-backlinks"
            )
            val propertyDescription = StubRelationObject(
                id = "propertyDescription_id",
                name = "Description",
                key = Relations.DESCRIPTION
            )

            val propertyAuthor = StubRelationObject(
                id = "propertyAuthor_id",
                name = "Author",
                key = "key-author"
            )

            storeOfRelations.merge(
                relations = listOf(
                    propertyObjectType,
                    propertyTag,
                    propertyBacklinks,
                    propertyDescription,
                    propertyAuthor
                )
            )

            val objType = StubObjectType(
                name = "Query",
                uniqueKey = ObjectTypeIds.SET,
                recommendedLayout = ObjectType.Layout.SET.code.toDouble(),
                recommendedFeaturedRelations = listOf(
                    propertyBacklinks.id,
                    propertyAuthor.id,
                    propertyObjectType.id,
                    propertyTag.id,
                    propertyDescription.id
                )
            )

            storeOfObjectTypes.merge(
                types = listOf(objType)
            )

            val objectSet = StubObject(
                id = "id",
                name = "Pages",
                description = "This the description of Pages Set",
                objectType = objType.id,
                extraFields = mapOf(
                    Relations.FEATURED_RELATIONS to listOf(
                        propertyBacklinks.key,
                        propertyDescription.key,
                        propertyAuthor.key,
                    )
                )
            )

            val featuredBlock = StubFeatured()

            val objectState = ObjectState.DataView.Set(
                root = objectSet.id,
                blocks = listOf(featuredBlock),
                details = ObjectViewDetails(
                    details = mapOf(
                        objectSet.id to objectSet.map,
                        objType.id to objType.map,
                    )
                )
            )

            val featuredPropertiesBlock = toFeaturedPropertiesViews(
                objectId = objectSet.id,
                storeOfRelations = storeOfRelations,
                storeOfObjectTypes = storeOfObjectTypes,
                urlBuilder = urlBuilder,
                fieldParser = fieldParser,
                details = objectState.details,
                blocks = objectState.blocks,
                participantCanEdit = true,
                conflictResolutionStrategy = ConflictResolutionStrategy.MERGE
            )

            assertEquals(
                expected = listOf(
                    propertyBacklinks.key,
                    propertyAuthor.key,
                    propertyObjectType.key,
                    propertyTag.key,
                ),
                actual = featuredPropertiesBlock!!.relations.map { it.key }
            )

            assertEquals(
                expected = false,
                actual = featuredPropertiesBlock.hasFeaturePropertiesConflict
            )
        }
}