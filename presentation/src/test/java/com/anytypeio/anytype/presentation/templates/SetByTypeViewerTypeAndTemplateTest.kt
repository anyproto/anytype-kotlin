package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.StubRelationObject
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking

@OptIn(ExperimentalCoroutinesApi::class)
class SetByTypeViewerTypeAndTemplateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var closable: AutoCloseable

    private val customType1Id = "customType1-${RandomString.make()}"

    private val template1 = StubObject(
        id = "template1-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = customType1Id
    )

    private val template2 = StubObject(
        id = "template2-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = customType1Id
    )

    private val setObjDetails = mapOf(
        Relations.ID to root,
        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
        Relations.SET_OF to listOf(customType1Id)
    )

    @Before
    fun setup() {
        closable = MockitoAnnotations.openMocks(this)
        viewModel = givenViewModel()
    }

    @After
    fun after() {
        rule.advanceTime()
        closable.close()
    }

    /**
     * Set by type,
     * SetOf: Type1
     * Type1 default template : empty
     * View default type : empty
     * View default template : empty
     */
    @Test
    fun `set by type, type default template is empty, views settings are empty`() = runTest {
        val workspaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = StubRelationObject()
        val dvViewerRelation1 =
            StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
        val relationLink1 = StubRelationLink(relationObject1.key)
        val viewer = StubDataViewView(
            viewerRelations = listOf(dvViewerRelation1),
            defaultObjectType = null,
            defaultTemplateId = null
        )
        val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
        val dvKeys = listOf(relationObject1.key)
        val customType1Map = mapOf(
            Relations.ID to customType1Id,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
            Relations.NAME to "name-$customType1Id",
            Relations.DEFAULT_TEMPLATE_ID to null
        )
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                customType1Id to Block.Fields(map = customType1Map)
            )
        )

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv),
            details = details
        )

        stubTemplatesForTemplatesContainer(
            type = customType1Id,
            templates = listOf(template1, template2)
        )
        with(storeOfObjectTypes) {
            set(customType1Id, customType1Map)
        }

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(customType1Id),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.hasTemplates)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByType(
                filters = listOf(),
                type = customType1Id,
                template = null,
            )
            async(params)
        }
    }

    /**
     * Set by type,
     * SetOf: Type1
     * Type1 default template : Blank
     * View default type : empty
     * View default template : empty
     */
    @Test
    fun `set by type, type default template is blank, views settings are empty`() = runTest {
        val workspaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = StubRelationObject()
        val dvViewerRelation1 =
            StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
        val relationLink1 = StubRelationLink(relationObject1.key)
        val viewer = StubDataViewView(
            viewerRelations = listOf(dvViewerRelation1),
            defaultObjectType = null,
            defaultTemplateId = null
        )
        val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
        val dvKeys = listOf(relationObject1.key)
        val customType1Map = mapOf(
            Relations.ID to customType1Id,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
            Relations.NAME to "name-$customType1Id",
            Relations.DEFAULT_TEMPLATE_ID to DEFAULT_TEMPLATE_ID_BLANK
        )
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                customType1Id to Block.Fields(map = customType1Map)
            )
        )

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv),
            details = details
        )

        stubTemplatesForTemplatesContainer(
            type = customType1Id,
            templates = listOf(template1, template2)
        )
        with(storeOfObjectTypes) {
            set(customType1Id, customType1Map)
        }

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(customType1Id),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.hasTemplates)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByType(
                filters = listOf(),
                type = customType1Id,
                template = null,
            )
            async(params)
        }
    }

    /**
     * Set by type,
     * SetOf: Type1
     * Type1 default template : Template2
     * View default type : empty
     * View default template : empty
     */
    @Test
    fun `set by type, type default template is not blank, views settings are empty`() = runTest {
        val workspaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = StubRelationObject()
        val dvViewerRelation1 =
            StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
        val relationLink1 = StubRelationLink(relationObject1.key)
        val viewer = StubDataViewView(
            viewerRelations = listOf(dvViewerRelation1),
            defaultObjectType = null,
            defaultTemplateId = null
        )
        val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
        val dvKeys = listOf(relationObject1.key)
        val customType1Map = mapOf(
            Relations.ID to customType1Id,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
            Relations.NAME to "name-$customType1Id",
            Relations.DEFAULT_TEMPLATE_ID to template2.id
        )
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                customType1Id to Block.Fields(map = customType1Map)
            )
        )

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv),
            details = details
        )

        stubTemplatesForTemplatesContainer(
            type = customType1Id,
            templates = listOf(template1, template2)
        )
        with(storeOfObjectTypes) {
            set(customType1Id, customType1Map)
        }

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(customType1Id),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.hasTemplates)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByType(
                filters = listOf(),
                type = customType1Id,
                template = template2.id,
            )
            async(params)
        }
    }

    /**
     * Set by type,
     * SetO f: Type1
     * Type1 default template : Template2
     * View default type : Type2
     * View default template : Template1
     * In that case we should ignore VIEW default type but use VIEW default template
     * But this case should not happen in real life, because it's not possible to set to VIEW a different type other then setOf
     */
    @Test
    fun `set by type, type default template is not blank, views settings has different type and template`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val dvKeys = listOf(relationObject1.key)
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "name-$customType1Id",
                Relations.DEFAULT_TEMPLATE_ID to template2.id
            )
            val customType2Id = "customType2-${RandomString.make()}"
            val customType2Map = mapOf(
                Relations.ID to customType2Id,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to RandomString.make(),
                Relations.DEFAULT_TEMPLATE_ID to template1.id
            )
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType2Id,
                defaultTemplateId = template1.id
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val details = Block.Details(
                details = mapOf(
                    root to Block.Fields(map = setObjDetails),
                    customType1Id to Block.Fields(map = customType1Map),
                    customType2Id to Block.Fields(map = customType2Map)
                )
            )

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv),
                details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id,
                templates = listOf(template1, template2)
            )
            with(storeOfObjectTypes) {
                set(customType1Id, customType1Map)
            }

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(customType1Id),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            viewModel.onStart(ctx = root)

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.hasTemplates)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByType(
                    filters = listOf(),
                    type = customType1Id,
                    template = template1.id,
                )
                async(params)
            }
        }

    /**
     * Set by type,
     * SetOf: Type1
     * Type1 default template : Template2
     * View default type : empty
     * View default template : Template1
     */
    @Test
    fun `set by type, type default template is not blank, view template is different`() = runTest {
        val workspaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = StubRelationObject()
        val dvViewerRelation1 =
            StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
        val relationLink1 = StubRelationLink(relationObject1.key)
        val viewer = StubDataViewView(
            viewerRelations = listOf(dvViewerRelation1),
            defaultObjectType = null,
            defaultTemplateId = template1.id
        )
        val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
        val dvKeys = listOf(relationObject1.key)
        val customType1Map = mapOf(
            Relations.ID to customType1Id,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
            Relations.NAME to "name-$customType1Id",
            Relations.DEFAULT_TEMPLATE_ID to template2.id
        )
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                customType1Id to Block.Fields(map = customType1Map)
            )
        )

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv),
            details = details
        )

        stubTemplatesForTemplatesContainer(
            type = customType1Id,
            templates = listOf(template1, template2)
        )
        with(storeOfObjectTypes) {
            set(customType1Id, customType1Map)
        }

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(customType1Id),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.hasTemplates)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByType(
                filters = listOf(),
                type = customType1Id,
                template = template1.id,
            )
            async(params)
        }
    }

    /**
     * Set by type,
     * SetOf: Type1
     * Type1 default template : Template2
     * View default type : empty
     * View default template : Blank
     */
    @Test
    fun `set by type, type default template is not blank, view template is blank`() = runTest {
        val workspaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = StubRelationObject()
        val dvViewerRelation1 =
            StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
        val relationLink1 = StubRelationLink(relationObject1.key)
        val viewer = StubDataViewView(
            viewerRelations = listOf(dvViewerRelation1),
            defaultObjectType = null,
            defaultTemplateId = DEFAULT_TEMPLATE_ID_BLANK
        )
        val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
        val dvKeys = listOf(relationObject1.key)
        val customType1Map = mapOf(
            Relations.ID to customType1Id,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
            Relations.NAME to "name-$customType1Id",
            Relations.DEFAULT_TEMPLATE_ID to template2.id
        )
        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                customType1Id to Block.Fields(map = customType1Map)
            )
        )

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv),
            details = details
        )

        stubTemplatesForTemplatesContainer(
            type = customType1Id,
            templates = listOf(template1, template2)
        )
        with(storeOfObjectTypes) {
            set(customType1Id, customType1Map)
        }

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(customType1Id),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        viewModel.onStart(ctx = root)

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.hasTemplates)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByType(
                filters = listOf(),
                type = customType1Id,
                template = null
            )
            async(params)
        }
    }
}