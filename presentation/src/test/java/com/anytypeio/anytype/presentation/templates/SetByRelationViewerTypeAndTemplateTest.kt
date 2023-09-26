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
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
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
class SetByRelationViewerTypeAndTemplateTest : ObjectSetViewModelTestSetup() {

    private lateinit var viewModel: ObjectSetViewModel
    private lateinit var closable: AutoCloseable

    private val setByRelationId = "setByRelationId-${RandomString.make()}"
    private val setByRelationKey = "setByRelationKey-${RandomString.make()}"
    val setByRelationMap = mapOf(
        Relations.ID to setByRelationId,
        Relations.TYPE to ObjectTypeIds.RELATION,
        Relations.RELATION_KEY to setByRelationKey
    )

    val pageTypeId = ObjectState.VIEW_DEFAULT_OBJECT_TYPE
    val pageTemplate1 = StubObject(
        id = "pagetemplate1-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = pageTypeId
    )

    val pageTemplate2 = StubObject(
        id = "pagetemplate2-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = pageTypeId
    )

    val customType1Id = "customType1-${RandomString.make()}"

    val template1 = StubObject(
        id = "template1-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = customType1Id
    )

    val template2 = StubObject(
        id = "template2-${RandomString.make()}",
        objectType = ObjectTypeIds.TEMPLATE,
        targetObjectType = customType1Id
    )

    val setObjDetails = mapOf(
        Relations.ID to root,
        Relations.LAYOUT to ObjectType.Layout.SET.code.toDouble(),
        Relations.SET_OF to listOf(setByRelationId),
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
     * Set by relation,
     * SetOf: Relation1
     * View default type : PAGE
     * View default template : empty
     * PAGE type default template : empty
     */
    @Test
    fun `set by relation, view type and template are empty, page template empty`() = runTest {
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
        val pageTypeMap = mapOf(
            Relations.ID to pageTypeId,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString(),
            Relations.DEFAULT_TEMPLATE_ID to null
        )

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                setByRelationId to Block.Fields(map = setByRelationMap),
                pageTypeId to Block.Fields(map = pageTypeMap)
            )
        )

        with(storeOfObjectTypes) {
            set(pageTypeId, pageTypeMap)
        }

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv), details = details
        )

        stubTemplatesForTemplatesContainer(
            type = pageTypeId, templates = listOf(pageTemplate1, pageTemplate2)
        )

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
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
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeId,
                template = null,
                relations = listOf(setByRelationId),
            )
            async(params)
        }
    }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : PAGE
     * View default template : empty
     * PAGE type default template : Blank
     */
    @Test
    fun `set by relation, view type and template are empty, page template blank`() = runTest {
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
        val pageTypeMap = mapOf(
            Relations.ID to pageTypeId,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString(),
            Relations.DEFAULT_TEMPLATE_ID to DEFAULT_TEMPLATE_ID_BLANK
        )

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                setByRelationId to Block.Fields(map = setByRelationMap),
                pageTypeId to Block.Fields(map = pageTypeMap)
            )
        )

        with(storeOfObjectTypes) {
            set(pageTypeId, pageTypeMap)
        }

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv), details = details
        )

        stubTemplatesForTemplatesContainer(
            type = pageTypeId, templates = listOf(pageTemplate1, pageTemplate2)
        )

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
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
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeId,
                template = null,
                relations = listOf(setByRelationId),
            )
            async(params)
        }
    }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : PAGE
     * View default template : empty
     * PAGE type default template : Custom
     */
    @Test
    fun `set by relation, view type and template are empty, page template custom`() = runTest {
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
        val pageTypeMap = mapOf(
            Relations.ID to pageTypeId,
            Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
            Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
            Relations.NAME to MockDataFactory.randomString(),
            Relations.DEFAULT_TEMPLATE_ID to pageTemplate2.id
        )

        val details = Block.Details(
            details = mapOf(
                root to Block.Fields(map = setObjDetails),
                setByRelationId to Block.Fields(map = setByRelationMap),
                pageTypeId to Block.Fields(map = pageTypeMap)
            )
        )

        with(storeOfObjectTypes) {
            set(pageTypeId, pageTypeMap)
        }

        stubWorkspaceManager(workspaceId)
        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObject(
            doc = listOf(dv), details = details
        )

        stubTemplatesForTemplatesContainer(
            type = pageTypeId, templates = listOf(pageTemplate1, pageTemplate2)
        )

        stubSubscriptionResults(
            subscription = subscriptionId,
            workspace = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
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
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeId,
                template = pageTemplate2.id,
                relations = listOf(setByRelationId),
            )
            async(params)
        }
    }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : CUSTOM
     * View default template : empty
     * CUSTOM type default template : template1
     */
    @Test
    fun `set by relation, view type is custom and template is empty, custom type template is not empty`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType1Id,
                defaultTemplateId = null
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val dvKeys = listOf(relationObject1.key)
            val pageTypeMap = mapOf(
                Relations.ID to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "name-$customType1Id",
                Relations.DEFAULT_TEMPLATE_ID to template1.id
            )

            val details = Block.Details(
                details = mapOf(
                    root to Block.Fields(map = setObjDetails),
                    setByRelationId to Block.Fields(map = setByRelationMap),
                    pageTypeId to Block.Fields(map = pageTypeMap),
                    customType1Id to Block.Fields(map = customType1Map)
                )
            )

            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
            }

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv), details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id, templates = listOf(template2, template1)
            )

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
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
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Id,
                    template = template1.id,
                    relations = listOf(setByRelationId),
                )
                async(params)
            }
        }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : CUSTOM
     * View default template : empty
     * CUSTOM type default template : empty
     */
    @Test
    fun `set by relation, view type is custom and template is empty, custom type template is empty`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType1Id,
                defaultTemplateId = null
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val dvKeys = listOf(relationObject1.key)
            val pageTypeMap = mapOf(
                Relations.ID to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
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
                    setByRelationId to Block.Fields(map = setByRelationMap),
                    pageTypeId to Block.Fields(map = pageTypeMap),
                    customType1Id to Block.Fields(map = customType1Map)
                )
            )

            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
            }

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv), details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id, templates = listOf(template2, template1)
            )

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
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
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Id,
                    template = null,
                    relations = listOf(setByRelationId),
                )
                async(params)
            }
        }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : CUSTOM
     * View default template : empty
     * CUSTOM type default template : blank
     */
    @Test
    fun `set by relation, view type is custom and template is empty, custom type template is blank`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType1Id,
                defaultTemplateId = null
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val dvKeys = listOf(relationObject1.key)
            val pageTypeMap = mapOf(
                Relations.ID to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
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
                    setByRelationId to Block.Fields(map = setByRelationMap),
                    pageTypeId to Block.Fields(map = pageTypeMap),
                    customType1Id to Block.Fields(map = customType1Map)
                )
            )

            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
            }

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv), details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id, templates = listOf(template2, template1)
            )

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
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
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Id,
                    template = null,
                    relations = listOf(setByRelationId),
                )
                async(params)
            }
        }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : CUSTOM
     * View default template : Blank
     * CUSTOM type default template : Template1
     */
    @Test
    fun `set by relation, view type is custom and template is blank, custom type template is not empty`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType1Id,
                defaultTemplateId = DEFAULT_TEMPLATE_ID_BLANK
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val dvKeys = listOf(relationObject1.key)
            val pageTypeMap = mapOf(
                Relations.ID to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "name-$customType1Id",
                Relations.DEFAULT_TEMPLATE_ID to template1.id
            )

            val details = Block.Details(
                details = mapOf(
                    root to Block.Fields(map = setObjDetails),
                    setByRelationId to Block.Fields(map = setByRelationMap),
                    pageTypeId to Block.Fields(map = pageTypeMap),
                    customType1Id to Block.Fields(map = customType1Map)
                )
            )

            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
            }

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv), details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id, templates = listOf(template2, template1)
            )

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
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
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Id,
                    template = null,
                    relations = listOf(setByRelationId),
                )
                async(params)
            }
        }

    /**
     * Set by relation,
     * SetOf: Relation1
     * View default type : CUSTOM
     * View default template : Template2
     * CUSTOM type default template : Template1
     */
    @Test
    fun `set by relation, view type is custom and template is not empty, custom type template is different`() =
        runTest {
            val workspaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = StubRelationObject()
            val dvViewerRelation1 =
                StubDataViewViewRelation(key = relationObject1.key, isVisible = true)
            val relationLink1 = StubRelationLink(relationObject1.key)
            val viewer = StubDataViewView(
                viewerRelations = listOf(dvViewerRelation1),
                defaultObjectType = customType1Id,
                defaultTemplateId = template2.id
            )
            val dv = StubDataView(views = listOf(viewer), relationLinks = listOf(relationLink1))
            val dvKeys = listOf(relationObject1.key)
            val pageTypeMap = mapOf(
                Relations.ID to pageTypeId,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.TYPE to ObjectTypeIds.OBJECT_TYPE,
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.TODO.code.toDouble(),
                Relations.NAME to "name-$customType1Id",
                Relations.DEFAULT_TEMPLATE_ID to template1.id
            )

            val details = Block.Details(
                details = mapOf(
                    root to Block.Fields(map = setObjDetails),
                    setByRelationId to Block.Fields(map = setByRelationMap),
                    pageTypeId to Block.Fields(map = pageTypeMap),
                    customType1Id to Block.Fields(map = customType1Map)
                )
            )

            with(storeOfObjectTypes) {
                set(pageTypeId, pageTypeMap)
                set(customType1Id, customType1Map)
            }

            stubWorkspaceManager(workspaceId)
            stubInterceptEvents()
            stubInterceptThreadStatus()
            stubOpenObject(
                doc = listOf(dv), details = details
            )

            stubTemplatesForTemplatesContainer(
                type = customType1Id, templates = listOf(template2, template1)
            )

            stubSubscriptionResults(
                subscription = subscriptionId,
                workspace = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
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
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Id,
                    template = template2.id,
                    relations = listOf(setByRelationId),
                )
                async(params)
            }
        }
}