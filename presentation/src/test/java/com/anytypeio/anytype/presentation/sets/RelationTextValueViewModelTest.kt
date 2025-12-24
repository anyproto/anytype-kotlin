package com.anytypeio.anytype.presentation.sets

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.`object`.ReloadObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RelationTextValueViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock
    lateinit var values: ObjectValueProvider

    @Mock
    lateinit var reloadObject: ReloadObject

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var storeOfRelations: StoreOfRelations

    private val ctx: Id = MockDataFactory.randomUuid()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should set isBookmarkSourceRelation to true when relation is SOURCE and object type is BOOKMARK`() = runTest {
        // SETUP
        val objectId = MockDataFactory.randomUuid()
        val typeId = MockDataFactory.randomUuid()
        val urlValue = "https://anytype.io"

        val relation = ObjectWrapper.Relation(
            map = mapOf<String, Any?>(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.RELATION_KEY to Relations.SOURCE,
                Relations.NAME to "Source",
                Relations.RELATION_FORMAT to RelationFormat.URL.code.toDouble()
            )
        )

        val objectTypeWrapper = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.ID to typeId,
                Relations.UNIQUE_KEY to ObjectTypeUniqueKeys.BOOKMARK
            )
        )

        val objectValues = mapOf<String, Any?>(
            Relations.ID to objectId,
            Relations.TYPE to listOf(typeId),
            Relations.SOURCE to urlValue
        )

        whenever(storeOfRelations.getByKey(Relations.SOURCE)).doReturn(relation)
        whenever(values.get(ctx = ctx, target = objectId)).doReturn(objectValues)
        whenever(storeOfObjectTypes.get(typeId)).doReturn(objectTypeWrapper)

        val vm = buildViewModel()

        // TESTING
        vm.actions.test {
            vm.onStart(
                ctx = ctx,
                relationKey = Relations.SOURCE,
                objectId = objectId,
                isLocked = false
            )

            advanceUntilIdle()

            val actions = expectMostRecentItem()

            // Verify Reload action is present
            val reloadAction = actions.filterIsInstance<RelationValueAction.Url.Reload>()
            assertEquals(1, reloadAction.size)
            assertEquals(urlValue, reloadAction.first().url)

            // Verify shouldReloadOnUrlEdit returns true
            assertTrue(vm.shouldReloadOnUrlEdit())
        }
    }

    @Test
    fun `should set isBookmarkSourceRelation to false when relation is SOURCE but object type is not BOOKMARK`() = runTest {
        // SETUP
        val objectId = MockDataFactory.randomUuid()
        val typeId = MockDataFactory.randomUuid()
        val urlValue = "https://anytype.io"

        val relation = ObjectWrapper.Relation(
            map = mapOf<String, Any?>(
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.RELATION_KEY to Relations.SOURCE,
                Relations.NAME to "Source",
                Relations.RELATION_FORMAT to RelationFormat.URL.code.toDouble()
            )
        )

        // Using PAGE type instead of BOOKMARK
        val objectTypeWrapper = ObjectWrapper.Type(
            map = mapOf<String, Any?>(
                Relations.ID to typeId,
                Relations.UNIQUE_KEY to ObjectTypeUniqueKeys.PAGE
            )
        )

        val objectValues = mapOf<String, Any?>(
            Relations.ID to objectId,
            Relations.TYPE to listOf(typeId),
            Relations.SOURCE to urlValue
        )

        whenever(storeOfRelations.getByKey(Relations.SOURCE)).doReturn(relation)
        whenever(values.get(ctx = ctx, target = objectId)).doReturn(objectValues)
        whenever(storeOfObjectTypes.get(typeId)).doReturn(objectTypeWrapper)

        val vm = buildViewModel()

        // TESTING
        vm.actions.test {
            vm.onStart(
                ctx = ctx,
                relationKey = Relations.SOURCE,
                objectId = objectId,
                isLocked = false
            )

            advanceUntilIdle()

            val actions = expectMostRecentItem()

            // Verify Reload action is NOT present
            val reloadAction = actions.filterIsInstance<RelationValueAction.Url.Reload>()
            assertTrue(reloadAction.isEmpty())

            // Verify Browse and Copy actions are present
            val browseAction = actions.filterIsInstance<RelationValueAction.Url.Browse>()
            val copyAction = actions.filterIsInstance<RelationValueAction.Url.Copy>()
            assertEquals(1, browseAction.size)
            assertEquals(1, copyAction.size)

            // Verify shouldReloadOnUrlEdit returns false
            assertFalse(vm.shouldReloadOnUrlEdit())
        }
    }

    private fun buildViewModel(): RelationTextValueViewModel = RelationTextValueViewModel(
        values = values,
        reloadObject = reloadObject,
        analytics = analytics,
        storeOfObjectTypes = storeOfObjectTypes,
        storeOfRelations = storeOfRelations
    )
}
