package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.core_models.UrlBuilder
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
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.widgets.CreateWidget
import com.anytypeio.anytype.domain.widgets.DeleteWidget
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import javax.inject.Inject

class ObjectTypeVMFactory @Inject constructor(
    private val vmParams: ObjectTypeVmParams,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val fieldParser: FieldParser,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val deleteObjects: DeleteObjects,
    private val setObjectDetails: SetObjectDetails,
    private val stringResourceProvider: StringResourceProvider,
    private val createTemplate: CreateTemplate,
    private val duplicateObjects: DuplicateObjects,
    private val getObjectTypeConflictingFields: GetObjectTypeConflictingFields,
    private val objectTypeSetRecommendedFields: SetObjectTypeRecommendedFields,
    private val setDataViewProperties: SetDataViewProperties,
    private val dispatcher: Dispatcher<Payload>,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val createWidget: CreateWidget,
    private val deleteWidget: DeleteWidget,
    private val spaceManager: SpaceManager,
    private val getObject: GetObject,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
    private val updateText: UpdateText,
    private val searchObjects: SearchObjects
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ObjectTypeViewModel(
            vmParams = vmParams,
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
            updateText = updateText,
            searchObjects = searchObjects
        ) as T
}