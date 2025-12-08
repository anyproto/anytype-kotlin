package com.anytypeio.anytype.feature_object_type.viewmodel


import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.CreateObjectResult
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubConfig
import com.anytypeio.anytype.core_models.StubObjectType
import com.anytypeio.anytype.core_models.StubObjectView
import com.anytypeio.anytype.core_models.multiplayer.P2PStatusUpdate
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_models.primitives.ParsedProperties
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.GetObjectTypeConflictingFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_object_type.DefaultCoroutineTestRule
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.feature_object_type.ui.create.UiTypeSetupTitleAndIconState
import com.anytypeio.anytype.feature_object_type.ui.menu.ObjectTypeMenuEvent
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.util.Dispatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.times
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class ObjectTypeViewModelTest {


    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()
    @Mock lateinit var analytics: Analytics
    @Mock lateinit var urlBuilder: UrlBuilder
    @Mock lateinit var analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    @Mock lateinit var userPermissionProvider: UserPermissionProvider
    @Mock lateinit var spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider
    @Mock lateinit var fieldParser: FieldParser
    @Mock lateinit var coverImageHashProvider: CoverImageHashProvider
    @Mock lateinit var deleteObjects: DeleteObjects
    @Mock lateinit var setObjectDetails: SetObjectDetails
    @Mock lateinit var stringResourceProvider: StringResourceProvider
    @Mock lateinit var createTemplate: CreateTemplate
    @Mock lateinit var duplicateObjects: DuplicateObjects
    @Mock lateinit var getObjectTypeConflictingFields: GetObjectTypeConflictingFields
    @Mock lateinit var objectTypeSetRecommendedFields: SetObjectTypeRecommendedFields
    @Mock lateinit var setDataViewProperties: SetDataViewProperties
    @Mock lateinit var setObjectListIsArchived: SetObjectListIsArchived
    @Mock lateinit var createWidget: CreateWidget
    @Mock lateinit var deleteWidget: DeleteWidget
    @Mock lateinit var spaceManager: SpaceManager
    @Mock lateinit var getObject: GetObject
    @Mock lateinit var addToFeaturedRelations: AddToFeaturedRelations
    @Mock lateinit var removeFromFeaturedRelations: RemoveFromFeaturedRelations
    @Mock lateinit var updateText: UpdateText
    @Mock lateinit var dispatcher: Dispatcher<Payload>
    @Mock lateinit var storelessSubscriptionContainer: StorelessSubscriptionContainer
    @Mock lateinit var storeOfRelations: StoreOfRelations
    @Mock lateinit var storeOfObjectTypes: StoreOfObjectTypes

    val objectId = "test-objectId"
    val spaceId = SpaceId("test-spaceId")

    val stubConfig = StubConfig()


    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun `should start subscription and check for pinned types on start`() = runTest {

        val params = GetObject.Params(
            target = stubConfig.widgets,
            space = spaceId,
            saveAsLastOpened = false
        )

        mockInit()

        whenever(spaceManager.getConfig(spaceId)).thenReturn(stubConfig)
        whenever(getObject.async(params)).thenReturn(Resultat.Success(StubObjectView(objectId)))


        val vm = createViewModel()

        vm.onStart()

        advanceUntilIdle()

        verifyBlocking(spaceManager, times(1)) { getConfig(spaceId) }
        verifyBlocking(getObject, times(1)) { async(params) }
    }

    @Test
    fun `should observe sync state, object type and getObjectTypeConflictingFields on init`()  = runTest {
        mockInit()

        val vm = createViewModel()
        advanceUntilIdle()

        verifyBlocking(spaceSyncAndP2PStatusProvider, times(1)) { observe() }
    }


    @Test
    fun `stop subscription when onStop called`()  = runTest {
        mockInit()

        val vm = createViewModel()

        vm.onStop()

        advanceUntilIdle()

        verifyBlocking(storelessSubscriptionContainer, times(1)) { unsubscribe(any()) }
    }

    @Test
    fun `should change values on menu event`() = runTest {

        mockInit()
        val vm = createViewModel()

        // Event onDismiss
        vm.onMenuEvent(ObjectTypeMenuEvent.OnDismiss)
        vm.uiMenuState.test {
            val item = awaitItem()
            assertFalse(item.isVisible)
        }

        // Event OnIconClick
        vm.onMenuEvent(ObjectTypeMenuEvent.OnIconClick)
        vm.uiMenuState.test {
            val item = awaitItem()
            assertFalse(item.isVisible)
        }
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Visible)
        }

        // Event OnToBinClick
        whenever(setObjectListIsArchived.async(any())).thenReturn(Resultat.Success(Unit))
        vm.onMenuEvent(ObjectTypeMenuEvent.OnToBinClick)
        vm.uiMenuState.test {
            val item = awaitItem()
            assertFalse(item.isVisible)
        }
        advanceUntilIdle()
        verifyBlocking(setObjectListIsArchived, times(1)) { async(any()) }
    }

    @Test
    fun `should not toggle description on menu event if user is not owner or editor`() = runTest {

        mockInit(SpaceMemberPermissions.READER)

        val vm = createViewModel()
        vm.onMenuEvent(ObjectTypeMenuEvent.OnDescriptionClick)

        vm.uiMenuState.test {
            val item = awaitItem()
            assertFalse(item.isVisible)
        }

        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.ShowToast)
            assertEquals((item as ObjectTypeCommand.ShowToast).msg, "Permission denied")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `should add to feature relations when description is not featured on menu event if user is owner or editor`() = runTest {

        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = StubObjectType()
        val objectTypeConflictingFields = listOf<Id>("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )

        whenever(addToFeaturedRelations.async(any())).thenReturn(Resultat.Success(testPayload))
        whenever(userPermissionProvider.get(spaceId)).thenReturn(SpaceMemberPermissions.OWNER)

        val vm = createViewModel()

        vm.onMenuEvent(ObjectTypeMenuEvent.OnDescriptionClick)

        vm.uiMenuState.test {
            val item = awaitItem()
            assertFalse(item.isVisible)
        }

        advanceUntilIdle()

        verifyBlocking(addToFeaturedRelations, times(1)) { async(any()) }
        verifyBlocking(dispatcher, times(1)) { send(testPayload) }
    }


    @Test
    fun `should remove from feature relations when description is featured on menu event if user is owner or editor`() = runTest {

        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = ObjectWrapper.Type(
            mutableMapOf(
                Relations.ID to "object-id",
                Relations.UNIQUE_KEY to "unique-key",
                Relations.FEATURED_RELATIONS to listOf(Relations.DESCRIPTION),
                Relations.RECOMMENDED_FEATURED_RELATIONS to listOf(Relations.DESCRIPTION),
                Relations.DESCRIPTION to "Some description"
            )
        )
        val objectTypeConflictingFields = listOf("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )

        whenever(userPermissionProvider.get(spaceId)).thenReturn(SpaceMemberPermissions.OWNER)
        whenever(storeOfObjectTypes.get(objectId)).thenReturn(objectWrapperType)
        whenever(removeFromFeaturedRelations.async(any())).thenReturn(Resultat.Success(testPayload))
        whenever(fieldParser.getObjectPluralName(any<ObjectWrapper.Type>())).thenReturn("Objects")
        whenever(fieldParser.getObjectName(any<ObjectWrapper.Type>())).thenReturn("Object")
        whenever(fieldParser.getObjectTypeParsedProperties(
            objectType = objectWrapperType,
            objectTypeConflictingPropertiesIds = objectTypeConflictingFields,
            storeOfRelations = storeOfRelations
        )).thenReturn(ParsedProperties())


        val vm = createViewModel()

        advanceUntilIdle()

        vm.onMenuEvent(ObjectTypeMenuEvent.OnDescriptionClick)

        advanceUntilIdle()

        verifyBlocking(removeFromFeaturedRelations, times(1)) { async(any()) }
        verifyBlocking(dispatcher, times(1)) { send(testPayload) }
    }


    @Test
    fun `should emit error message when removing from feature relations fails`() = runTest {

        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = ObjectWrapper.Type(
            mutableMapOf(
                Relations.ID to "object-id",
                Relations.UNIQUE_KEY to "unique-key",
                Relations.FEATURED_RELATIONS to listOf(Relations.DESCRIPTION),
                Relations.RECOMMENDED_FEATURED_RELATIONS to listOf(Relations.DESCRIPTION),
                Relations.DESCRIPTION to "Some description"
            )
        )
        val objectTypeConflictingFields = listOf("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )

        whenever(userPermissionProvider.get(spaceId)).thenReturn(SpaceMemberPermissions.OWNER)
        whenever(storeOfObjectTypes.get(objectId)).thenReturn(objectWrapperType)
        whenever(removeFromFeaturedRelations.async(any())).thenReturn(Resultat.Failure(Exception("error")))
        whenever(fieldParser.getObjectPluralName(any<ObjectWrapper.Type>())).thenReturn("Objects")
        whenever(fieldParser.getObjectName(any<ObjectWrapper.Type>())).thenReturn("Object")
        whenever(fieldParser.getObjectTypeParsedProperties(
            objectType = objectWrapperType,
            objectTypeConflictingPropertiesIds = objectTypeConflictingFields,
            storeOfRelations = storeOfRelations
        )).thenReturn(ParsedProperties())


        val vm = createViewModel()

        advanceUntilIdle()

        vm.onMenuEvent(ObjectTypeMenuEvent.OnDescriptionClick)

        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.ShowToast)
            assertEquals((item as ObjectTypeCommand.ShowToast).msg, "Failed to hide description")
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun `should unpin based on pinned state on pin toggle`() = runTest {

        val stubConfig = StubConfig()
        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val blocks = stubRelatedBlocks(objectId)
        val objectView = StubObjectView(objectId, blocks)

        whenever(spaceManager.getConfig(spaceId)).thenReturn(stubConfig)
        whenever(getObject.async(any())).thenReturn(Resultat.Success(objectView))
        whenever(deleteWidget.async(any())).thenReturn(Resultat.Success(testPayload))
        mockInit()

        val vm = createViewModel()

        vm.onStart()

        advanceUntilIdle()


        vm.uiMenuState.value = vm.uiMenuState.value.copy(isPinned = true, isVisible = true)

        vm.onMenuEvent(ObjectTypeMenuEvent.OnPinToggleClick)

        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.ShowToast)
            assertEquals((item as ObjectTypeCommand.ShowToast).msg, "Widget unpinned")
            cancelAndIgnoreRemainingEvents()
        }

        vm.uiMenuState.test {
            val item2 = awaitItem()
            assertFalse(item2.isVisible)
        }

        advanceUntilIdle()

        verifyBlocking(deleteWidget, times(1)) { async(any()) }
        verifyBlocking(dispatcher, times(1)) { send(testPayload) }

    }

    @Test
    fun `should pin based on pinned state on pin toggle`() = runTest {

        val stubConfig = StubConfig()
        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )

        whenever(spaceManager.getConfig(spaceId)).thenReturn(stubConfig)
        whenever(createWidget.async(any())).thenReturn(Resultat.Success(testPayload))
        whenever(getObject.async(any())).thenReturn(Resultat.Success(StubObjectView(objectId)))
        mockInit()

        val vm = createViewModel()

        vm.onStart()

        advanceUntilIdle()

        vm.onMenuEvent(ObjectTypeMenuEvent.OnPinToggleClick)

        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.ShowToast)
            assertEquals((item as ObjectTypeCommand.ShowToast).msg, "Widget created")
            cancelAndIgnoreRemainingEvents()
        }

        vm.uiMenuState.test {
            val item2 = awaitItem()
            assertFalse(item2.isVisible)
        }

        advanceUntilIdle()

        verifyBlocking(createWidget, times(1)) { async(any()) }
        verifyBlocking(dispatcher, times(1)) { send(testPayload) }

    }


    @Test
    fun `should change values on type event`() = runTest {

        val testPayload =  Payload(
            context = "test-context",
            events = emptyList()
        )

        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = StubObjectType()
        val objectTypeConflictingFields = listOf<Id>("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission","test-param-spaceType"))
        whenever(storeOfObjectTypes.get(objectId)).thenReturn(objectWrapperType)
        whenever(fieldParser.getObjectPluralName(any<ObjectWrapper.Type>())).thenReturn("Objects")
        whenever(fieldParser.getObjectName(any<ObjectWrapper.Type>())).thenReturn("Object")
        whenever(fieldParser.getObjectTypeParsedProperties(
            objectType = objectWrapperType,
            objectTypeConflictingPropertiesIds = objectTypeConflictingFields,
            storeOfRelations = storeOfRelations
        )).thenReturn(ParsedProperties())

        val vm = createViewModel()


        vm.onTypeEvent(TypeEvent.OnPropertiesButtonClick)
        vm.showPropertiesScreen.test {
            val item = awaitItem()
            assertTrue(item)
        }
        advanceUntilIdle()

        vm.onTypeEvent(TypeEvent.OnLayoutButtonClick)

        vm.uiTypeLayoutsState.test {
            val item = awaitItem()
            assert(item is UiLayoutTypeState.Visible)
        }

        advanceUntilIdle()

        vm.onTypeEvent(
            TypeEvent.OnSyncStatusClick(
                SpaceSyncAndP2PStatusState.Success(
                    SpaceSyncUpdate.Initial,
                    P2PStatusUpdate.Initial
                )
            )
        )
        vm.uiSyncStatusWidgetState.test {
            val item = awaitItem()
            assert(item is SyncStatusWidgetState.Success)
        }

        advanceUntilIdle()

        vm.onTypeEvent(TypeEvent.OnSyncStatusDismiss)
        vm.uiSyncStatusWidgetState.test {
            val item = awaitItem()
            assert(item is SyncStatusWidgetState.Hidden)
        }

        val mockCreateObjectResult = CreateObjectResult(
            id = "test-id",
            event = testPayload,
            details = mapOf()
        )
        whenever(createTemplate.async(any())).thenReturn(Resultat.Success(mockCreateObjectResult))
        vm.onTypeEvent(TypeEvent.OnTemplatesAddIconClick)
        advanceUntilIdle()
        verifyBlocking(createTemplate,times(1)) { async(any()) }


        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        vm.onTypeEvent(TypeEvent.OnObjectTypeTitleUpdate("test-title"))
        vm.uiTitleState.test {
            val item = awaitItem()
            assertEquals(item.title, "test-title")
        }
        advanceUntilIdle()
        verifyBlocking(setObjectDetails,times(1)) { async(any()) }

        vm.onTypeEvent(TypeEvent.OnObjectTypeTitleClick)
        vm.uiTitleAndIconUpdateState.test {
            val item = awaitItem()
            assert(item is UiTypeSetupTitleAndIconState.Visible.EditType)
        }

        whenever(updateText(any())).thenReturn(Either.Right(Unit))
        vm.onTypeEvent(TypeEvent.OnDescriptionChanged("test-description"))
        advanceUntilIdle()
        verifyBlocking(updateText, times(1)) { invoke(any()) }

        vm.onTypeEvent(TypeEvent.OnMenuItemDeleteClick)
        vm.uiAlertState.test {
            val item = awaitItem()
            assert(item is UiDeleteAlertState.Show)
            cancelAndConsumeRemainingEvents()
        }

        whenever(deleteObjects.async(any())).thenReturn(Resultat.Success(Unit))
        vm.onTypeEvent(TypeEvent.OnAlertDeleteConfirm)
        vm.uiAlertState.test {
            val item = awaitItem()
            assert(item is UiDeleteAlertState.Hidden)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()
        verifyBlocking(deleteObjects, times(1)) { async(any()) }

        vm.onTypeEvent(TypeEvent.OnAlertDeleteDismiss)
        vm.uiAlertState.test {
            val item = awaitItem()
            assert(item is UiDeleteAlertState.Hidden)
            cancelAndConsumeRemainingEvents()
        }

        vm.onTypeEvent(TypeEvent.OnObjectTypeIconClick)
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Visible)
            cancelAndConsumeRemainingEvents()
        }

        vm.onTypeEvent(TypeEvent.OnLayoutTypeDismiss)
        vm.uiTypeLayoutsState.test {
            val item = awaitItem()
            assert(item is UiLayoutTypeState.Hidden)
            cancelAndConsumeRemainingEvents()
        }

        clearInvocations(setObjectDetails)
        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        vm.onTypeEvent(TypeEvent.OnLayoutTypeItemClick(ObjectType.Layout.OBJECT_TYPE))
        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }

        vm.onTypeEvent(TypeEvent.OnBackClick)
        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.Back)
            cancelAndConsumeRemainingEvents()
        }

        vm.onTypeEvent(TypeEvent.OnTemplatesModalListDismiss)
        vm.uiTemplatesModalListState.test {
            val item = awaitItem()
            assert(item is UiTemplatesModalListState.Hidden)
            cancelAndConsumeRemainingEvents()
        }

        vm.onTypeEvent(TypeEvent.OnIconPickerDismiss)
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
            cancelAndConsumeRemainingEvents()
        }

        clearInvocations(setObjectDetails)
        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        vm.onTypeEvent(TypeEvent.OnIconPickerItemClick("test-icon",null))
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }


        clearInvocations(setObjectDetails)
        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        vm.onTypeEvent(TypeEvent.OnIconPickerRemovedClick)
        vm.uiIconsPickerScreen.test {
            val item = awaitItem()
            assert(item is UiIconsPickerState.Hidden)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }

        vm.onTypeEvent(TypeEvent.OnMenuClick)
        vm.uiMenuState.test {
            val item = awaitItem()
            assertTrue(item.isVisible)
            cancelAndConsumeRemainingEvents()
        }

        vm.onTypeEvent(TypeEvent.OnTemplatesButtonClick)
        advanceUntilIdle()
        vm.uiTemplatesModalListState.test {
            val item = awaitItem()
            assert(item is UiTemplatesModalListState.Visible)
            cancelAndConsumeRemainingEvents()
        }

    }

    @Test
    fun `should proceed with template changes based on the template type`() = runTest {

        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val mockCreateObjectResult = CreateObjectResult(
            id = "test-id",
            event = testPayload,
            details = mapOf()
        )
        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = StubObjectType()
        val objectTypeConflictingFields = listOf<Id>("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )
        whenever(createTemplate.async(any())).thenReturn(Resultat.Success(mockCreateObjectResult))
        whenever(storeOfObjectTypes.get(objectId)).thenReturn(objectWrapperType)
        whenever(fieldParser.getObjectPluralName(any<ObjectWrapper.Type>())).thenReturn("Objects")
        whenever(fieldParser.getObjectName(any<ObjectWrapper.Type>())).thenReturn("Object")
        whenever(fieldParser.getObjectTypeParsedProperties(
            objectType = objectWrapperType,
            objectTypeConflictingPropertiesIds = objectTypeConflictingFields,
            storeOfRelations = storeOfRelations
        )).thenReturn(ParsedProperties())

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onTypeEvent(TypeEvent.OnTemplateItemClick(TemplateView.New(TypeId("test-id"), TypeKey("test-Key"))))

        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.OpenTemplate)
            cancelAndConsumeRemainingEvents()
        }
        advanceUntilIdle()
        verifyBlocking(createTemplate, times(1)) { async(any()) }

        val mockTemplate = TemplateView.Template(
            id = "test-id",
            name = "test-name",
            targetTypeId = TypeId("test-id"),
            targetTypeKey = TypeKey("test-Key")
        )
        vm.onTypeEvent(TypeEvent.OnTemplateItemClick(mockTemplate))
        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.OpenTemplate)
        }

    }

    @Test
    fun `should proceed with template menu click`() = runTest {
        val mockTemplate = TemplateView.Template(
            id = "test-id",
            name = "test-name",
            targetTypeId = TypeId("test-id"),
            targetTypeKey = TypeKey("test-Key")
        )
        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )

        mockInit()
        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))

        val vm = createViewModel()
        vm.onTypeEvent(TypeEvent.OnTemplateMenuClick.SetAsDefault(mockTemplate))

        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }

        whenever(deleteObjects.async(any())).thenReturn(Resultat.Success(Unit))
        vm.onTypeEvent(TypeEvent.OnTemplateMenuClick.Delete(mockTemplate))
        advanceUntilIdle()
        verifyBlocking(deleteObjects, times(1)) { async(any()) }


        whenever(duplicateObjects.async(any())).thenReturn(Resultat.Success(emptyList()))
        vm.onTypeEvent(TypeEvent.OnTemplateMenuClick.Duplicate(mockTemplate))
        advanceUntilIdle()
        verifyBlocking(duplicateObjects, times(1)) { async(any()) }

    }


    @Test
    fun `should change values based on field event`() = runTest {


        mockInit()
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission","test-param-spaceType"))

        var vm = createViewModel()


        vm.onFieldEvent(FieldEvent.OnEditPropertyScreenDismiss)
        vm.uiEditPropertyScreen.test {
            val item = awaitItem()
            assert(item is UiEditPropertyState.Hidden)
        }

        val mockUiFieldListItem = UiFieldsListItem.Item.Local(
            id = "test-id",
            fieldKey = "test-field-key",
            fieldTitle = "test-field-title",
            format = RelationFormat.OBJECT,
            limitObjectTypes = emptyList(),
            isEditableField = false
        )
        whenever(stringResourceProvider.getPropertiesFormatPrettyString(any())).thenReturn("test-format")
        vm.onFieldEvent(FieldEvent.OnFieldItemClick(mockUiFieldListItem))
        advanceUntilIdle()
        vm.uiEditPropertyScreen.test {
            val item = awaitItem()
            assert(item is UiEditPropertyState.Visible.View)
        }



        val storeOfObjectTypesTrackEvent = StoreOfObjectTypes.TrackedEvent.Init
        val objectWrapperType = StubObjectType()
        val objectTypeConflictingFields = listOf<Id>("test-object-fields")
        val storeOfRelationsTrackedEvent = StoreOfRelations.TrackedEvent.Init

        mockInit(
            spaceMemberPermissions = SpaceMemberPermissions.OWNER,
            objectWrapperType = objectWrapperType,
            storeOfObjectTypesTrackEvent = storeOfObjectTypesTrackEvent,
            objectTypeConflictingFields = objectTypeConflictingFields,
            storeOfRelationsTrackedEvent = storeOfRelationsTrackedEvent
        )
        whenever(storeOfObjectTypes.get(objectId)).thenReturn(objectWrapperType)
        whenever(fieldParser.getObjectPluralName(any<ObjectWrapper.Type>())).thenReturn("Objects")
        whenever(fieldParser.getObjectName(any<ObjectWrapper.Type>())).thenReturn("Object")
        whenever(fieldParser.getObjectTypeParsedProperties(
            objectType = objectWrapperType,
            objectTypeConflictingPropertiesIds = objectTypeConflictingFields,
            storeOfRelations = storeOfRelations
        )).thenReturn(ParsedProperties())

        vm = createViewModel()
        advanceUntilIdle()
        vm.onFieldEvent(FieldEvent.OnFieldItemClick(mockUiFieldListItem.copy(isEditableField = true)))
        advanceUntilIdle()
        vm.uiEditPropertyScreen.test {
            val item = awaitItem()
            assert(item is UiEditPropertyState.Visible.Edit)
        }

        vm.onFieldEvent(FieldEvent.FieldLocalInfo.OnDismiss)
        vm.uiFieldLocalInfoState.test {
            val item = awaitItem()
            assert(item is UiLocalsFieldsInfoState.Hidden)
        }

        vm.onFieldEvent(FieldEvent.Section.OnLocalInfoClick)
        vm.uiFieldLocalInfoState.test {
            val item = awaitItem()
            assert(item is UiLocalsFieldsInfoState.Visible)
        }

        vm.onFieldEvent(FieldEvent.Section.OnAddToSidebarIconClick)
        vm.commands.test {
            val item = awaitItem()
            assert(item is ObjectTypeCommand.OpenAddNewPropertyScreen)
        }

        vm.onFieldEvent(FieldEvent.OnDismissScreen)
        vm.showPropertiesScreen.test {
            val item = awaitItem()
            assertFalse(item)
        }
    }

    @Test
    fun `should make changes based on FieldItemMenu events in on field event`() = runTest {
        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val mockUiFieldListItem = UiFieldsListItem.Item.Local(
            id = "test-id",
            fieldKey = "test-field-key",
            fieldTitle = "test-field-title",
            format = RelationFormat.OBJECT,
            limitObjectTypes = emptyList(),
            isEditableField = false
        )

        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission","test-param-spaceType"))
        whenever(setDataViewProperties.async(any())).thenReturn(Resultat.Success(testPayload))
        mockInit()

        val vm = createViewModel()

        vm.onFieldEvent(FieldEvent.FieldItemMenu.OnRemoveFromTypeClick("test-Id"))

        vm.uiEditPropertyScreen.test {
            val item = awaitItem()
            assert(item is UiEditPropertyState.Hidden)
        }

        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }
        verifyBlocking(setDataViewProperties, times(1)) { async(any()) }
        verifyBlocking(dispatcher,times(1)) { send(testPayload) }


        whenever(objectTypeSetRecommendedFields.async(any())).thenReturn(Resultat.Success(Unit))
        vm.onFieldEvent(FieldEvent.FieldItemMenu.OnAddLocalToTypeClick(mockUiFieldListItem))
        advanceUntilIdle()
        verifyBlocking(objectTypeSetRecommendedFields, times(1)) { async(any()) }

        whenever(setObjectListIsArchived.async(any())).thenReturn(Resultat.Success(Unit))
        whenever(getObjectTypeConflictingFields.async(any())).thenReturn(Resultat.Success(emptyList()))
        clearInvocations(getObjectTypeConflictingFields)
        vm.onFieldEvent(FieldEvent.FieldItemMenu.OnMoveToBinClick("test-Id"))
        advanceUntilIdle()
        verifyBlocking(setObjectListIsArchived,times(1)) { async(any()) }
        verifyBlocking(getObjectTypeConflictingFields,times(1)) { async(any()) }
    }


    @Test
    fun `should execute on drag end in on field event`() = runTest {
        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )
        val mockUiFieldListItem = UiFieldsListItem.Item.Local(
            id = "test-id",
            fieldKey = "test-field-key",
            fieldTitle = "test-field-title",
            format = RelationFormat.OBJECT,
            limitObjectTypes = emptyList(),
            isEditableField = false
        )

        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        whenever(analyticSpaceHelperDelegate.provideParams(any())).thenReturn(
            AnalyticSpaceHelperDelegate.Params("test-param-permission", "test-param-spaceType")
        )
        whenever(setDataViewProperties.async(any())).thenReturn(Resultat.Success(testPayload))
        mockInit()

        val vm = createViewModel()

        vm.onFieldEvent(FieldEvent.DragEvent.OnDragEnd)

        advanceUntilIdle()
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }
        verifyBlocking(setDataViewProperties, times(1)) { async(any()) }
        verifyBlocking(dispatcher, times(1)) { send(testPayload) }
    }


    @Test
    fun `should execute EditProperty changes on field event`() = runTest {

        val testPayload = Payload(
            context = "test-context",
            events = emptyList()
        )

        val name = "test-name"
        val initialState = UiEditPropertyState.Visible.New(
            name = "test-name-initial",
            formatName = "test-format-name",
            format = Relation.Format.OBJECT,
            formatIcon = null,
            showLimitTypes = false
        )

        mockInit()
        val vm = createViewModel()
        vm.uiEditPropertyScreen.value = initialState

        vm.onFieldEvent(FieldEvent.EditProperty.OnPropertyNameUpdate(name))
        vm.uiEditPropertyScreen.test {
            val item = awaitItem() as? UiEditPropertyState.Visible.New
            assertEquals(item?.name,name)
        }


        val editState = UiEditPropertyState.Visible.Edit(
            id = "test-Id",
            key = "test-key",
            name = "test-name-initial",
            formatName = "test-format-name",
            format = Relation.Format.OBJECT,
            formatIcon = null,
            showLimitTypes = false,
            isPossibleToUnlinkFromType = false
        )
        vm.uiEditPropertyScreen.value = editState

        vm.onFieldEvent(FieldEvent.EditProperty.OnPropertyNameUpdate(name))
        vm.uiEditPropertyScreen.test {
            val item = awaitItem() as? UiEditPropertyState.Visible.Edit
            assertEquals(item?.name,name)
        }

        val viewState = UiEditPropertyState.Visible.View(
            id = "test-Id",
            key = "test-key",
            name = "test-name-initial",
            formatName = "test-format-name",
            format = Relation.Format.OBJECT,
            formatIcon = null,
            showLimitTypes = false,
            isPossibleToUnlinkFromType = false
        )
        vm.uiEditPropertyScreen.value = viewState

        vm.onFieldEvent(FieldEvent.EditProperty.OnPropertyNameUpdate(name))
        vm.uiEditPropertyScreen.test {
            val item = awaitItem() as? UiEditPropertyState.Visible.View
            assertEquals(item?.name,"test-name-initial")
        }

        whenever(setObjectDetails.async(any())).thenReturn(Resultat.Success(testPayload))
        vm.uiEditPropertyScreen.value = editState

        vm.onFieldEvent(FieldEvent.EditProperty.OnSaveButtonClicked)
        advanceUntilIdle()
        vm.uiEditPropertyScreen.test {
            val item = awaitItem()
            assert(item is UiEditPropertyState.Hidden)
        }
        verifyBlocking(setObjectDetails, times(1)) { async(any()) }

        vm.uiEditPropertyScreen.value = editState

        vm.onFieldEvent(FieldEvent.EditProperty.OnLimitTypesClick)
        vm.uiEditPropertyScreen.test {
            val item = awaitItem() as? UiEditPropertyState.Visible.Edit
            assertTrue(item?.showLimitTypes!!)
        }

        vm.onFieldEvent(FieldEvent.EditProperty.OnLimitTypesDismiss)
        vm.uiEditPropertyScreen.test {
            val item = awaitItem() as? UiEditPropertyState.Visible.Edit
            assertFalse(item?.showLimitTypes!!)
        }
    }



    private fun createViewModel(): ObjectTypeViewModel {

        val showHiddenFields = false
        return ObjectTypeViewModel(
            vmParams = ObjectTypeVmParams(
                objectId = objectId,
                spaceId = spaceId,
                showHiddenFields = showHiddenFields
            ),
            analytics = analytics,
            urlBuilder = urlBuilder,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
            fieldParser = fieldParser,
            coverImageHashProvider = coverImageHashProvider,
            deleteObjects = deleteObjects,
            setObjectDetails = setObjectDetails,
            stringResourceProvider = stringResourceProvider,
            createTemplate = createTemplate,
            duplicateObjects = duplicateObjects,
            getObjectTypeConflictingFields = getObjectTypeConflictingFields,
            objectTypeSetRecommendedFields = objectTypeSetRecommendedFields,
            setDataViewProperties = setDataViewProperties,
            dispatcher = dispatcher,
            setObjectListIsArchived = setObjectListIsArchived,
            createWidget = createWidget,
            deleteWidget = deleteWidget,
            spaceManager = spaceManager,
            getObject = getObject,
            addToFeaturedRelations = addToFeaturedRelations,
            removeFromFeaturedRelations = removeFromFeaturedRelations,
            updateText = updateText
        )
    }

    private fun mockInit(
        spaceMemberPermissions: SpaceMemberPermissions? = null,
        objectWrapperType: ObjectWrapper.Type? = null,
        storeOfObjectTypesTrackEvent: StoreOfObjectTypes.TrackedEvent? = null,
        storeOfRelationsTrackedEvent: StoreOfRelations.TrackedEvent? = null,
        objectTypeConflictingFields: List<Id> = emptyList()
    ) {
        whenever(spaceSyncAndP2PStatusProvider.observe()).thenReturn(flowOf())
        whenever(storeOfObjectTypes.observe(any())).thenReturn(
            if(objectWrapperType != null) flowOf(objectWrapperType) else flowOf()
        )
        whenever(storeOfObjectTypes.trackChanges()).thenReturn(
            if(storeOfObjectTypesTrackEvent != null) flowOf(storeOfObjectTypesTrackEvent) else flowOf()
        )
        whenever(storeOfRelations.trackChanges()).thenReturn(
            if(storeOfRelationsTrackedEvent != null) flowOf(storeOfRelationsTrackedEvent) else flowOf()
        )
        whenever(userPermissionProvider.observe(spaceId)).thenReturn(
            if(spaceMemberPermissions != null) flowOf(spaceMemberPermissions) else flowOf()
        )
        runBlocking {
            whenever(getObjectTypeConflictingFields.async(any())).thenReturn(Resultat.Success(objectTypeConflictingFields))
        }
    }

    private fun stubRelatedBlocks(ctx: Id): List<Block> {
        return listOf(
            // The widget block we want returned
            Block(
                id = "widget-1",
                children = listOf("link-1"),   // FIRST CHILD is the link block
                content = Block.Content.Widget(
                    layout = Block.Content.Widget.Layout.LINK
                ),
                fields = Block.Fields.empty()
            ),

            // The link block pointing to targetCtx
            Block(
                id = "link-1",
                children = emptyList(),
                content = Block.Content.Link(
                    target = ctx,                               // IMPORTANT
                    type = Block.Content.Link.Type.PAGE,
                    iconSize = Block.Content.Link.IconSize.SMALL,
                    cardStyle = Block.Content.Link.CardStyle.CARD,
                    description = Block.Content.Link.Description.NONE
                ),
                fields = Block.Fields.empty()
            )
        )
    }
}