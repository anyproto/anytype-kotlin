package com.anytypeio.anytype.presentation.templates

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubDataView
import com.anytypeio.anytype.core_models.StubDataViewView
import com.anytypeio.anytype.core_models.StubDataViewViewRelation
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubRelationLink
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.presentation.sets.DataViewViewState
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.main.ObjectSetViewModelTestSetup
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.templates.TemplateView.Companion.DEFAULT_TEMPLATE_ID_BLANK
import com.anytypeio.anytype.test_utils.MockDataFactory
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
        Relations.LAYOUT to ObjectType.Layout.RELATION.code.toDouble(),
        Relations.RELATION_KEY to setByRelationKey
    )

    val pageTypeId = "pageId-${RandomString.make()}"
    val pageTypeKey = TypeKey(ObjectState.VIEW_DEFAULT_OBJECT_TYPE)
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

    val customType1Id = "customTypeId-${RandomString.make()}"
    val customType1Key = TypeKey("customTypeKey-${RandomString.make()}")

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
        stubNetworkMode()
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
        val spaceId = RandomString.make()
        val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
        val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
            Relations.UNIQUE_KEY to pageTypeKey.key,
            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

        stubSpaceManager(spaceId)
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
            spaceId = spaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        proceedWithStartingViewModel()

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.isCreateObjectAllowed)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeKey,
                template = null,
                prefilled = mapOf(relationLink1.key to null)
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
        val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
            Relations.UNIQUE_KEY to pageTypeKey.key,
            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

        stubSpaceManager(workspaceId)
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
            spaceId = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        proceedWithStartingViewModel()

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.isCreateObjectAllowed)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeKey,
                template = DEFAULT_TEMPLATE_ID_BLANK,
                prefilled = mapOf(relationLink1.key to null)
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
        val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
            Relations.UNIQUE_KEY to pageTypeKey.key,
            Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

        stubSpaceManager(workspaceId)
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
            spaceId = workspaceId,
            storeOfRelations = storeOfRelations,
            keys = dvKeys,
            sources = listOf(setByRelationId),
            objects = listOf()
        )
        stubCreateDataViewObject()

        //TESTING
        proceedWithStartingViewModel()

        advanceUntilIdle()

        val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

        // ASSERT NEW + BUTTON
        assertTrue(uiState.isCreateObjectAllowed)

        viewModel.proceedWithDataViewObjectCreate()

        advanceUntilIdle()

        // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
        verifyBlocking(createDataViewObject, times(1)) {
            val params = CreateDataViewObject.Params.SetByRelation(
                filters = listOf(),
                type = pageTypeKey,
                template = pageTemplate2.id,
                prefilled = mapOf(relationLink1.key to null)
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
            val spaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
                Relations.UNIQUE_KEY to pageTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.UNIQUE_KEY to customType1Key.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

            stubSpaceManager(spaceId)
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
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.isCreateObjectAllowed)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Key,
                    template = template1.id,
                    prefilled = mapOf(relationLink1.key to null)
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
            val spaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
                Relations.UNIQUE_KEY to pageTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.UNIQUE_KEY to customType1Key.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

            stubSpaceManager(spaceId)
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
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.isCreateObjectAllowed)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Key,
                    template = null,
                    prefilled = mapOf(relationLink1.key to null)
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
            val spaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
                Relations.UNIQUE_KEY to pageTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.UNIQUE_KEY to customType1Key.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

            stubSpaceManager(spaceId)
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
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.isCreateObjectAllowed)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Key,
                    template = DEFAULT_TEMPLATE_ID_BLANK,
                    prefilled = mapOf(relationLink1.key to null)
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
            val spaceId = RandomString.make()
            val subscriptionId = DefaultDataViewSubscription.getSubscriptionId(root)
            val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
                Relations.UNIQUE_KEY to pageTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.UNIQUE_KEY to customType1Key.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

            stubSpaceManager(spaceId)
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
                spaceId = spaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.isCreateObjectAllowed)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Key,
                    template = DEFAULT_TEMPLATE_ID_BLANK,
                    prefilled = mapOf(relationLink1.key to null)
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
            val relationObject1 = ObjectWrapper.Relation(map = setByRelationMap)
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
                Relations.UNIQUE_KEY to pageTypeKey.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                Relations.RECOMMENDED_LAYOUT to ObjectType.Layout.BASIC.code.toDouble(),
                Relations.NAME to MockDataFactory.randomString(),
                Relations.DEFAULT_TEMPLATE_ID to pageTemplate1.id
            )
            val customType1Map = mapOf(
                Relations.ID to customType1Id,
                Relations.UNIQUE_KEY to customType1Key.key,
                Relations.LAYOUT to ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
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

            stubSpaceManager(workspaceId)
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
                spaceId = workspaceId,
                storeOfRelations = storeOfRelations,
                keys = dvKeys,
                sources = listOf(setByRelationId),
                objects = listOf()
            )
            stubCreateDataViewObject()

            //TESTING
            proceedWithStartingViewModel()

            advanceUntilIdle()

            val uiState = viewModel.currentViewer.value as DataViewViewState.Set.NoItems

            // ASSERT NEW + BUTTON
            assertTrue(uiState.isCreateObjectAllowed)

            viewModel.proceedWithDataViewObjectCreate()

            advanceUntilIdle()

            // ASSERT OBJECT CREATION ON NEW BUTTON CLICK
            verifyBlocking(createDataViewObject, times(1)) {
                val params = CreateDataViewObject.Params.SetByRelation(
                    filters = listOf(),
                    type = customType1Key,
                    template = template2.id,
                    prefilled = mapOf(relationLink1.key to null)
                )
                async(params)
            }
        }

    private fun proceedWithStartingViewModel() {
        viewModel.onStart(ctx = root, space = defaultSpace)
    }
}