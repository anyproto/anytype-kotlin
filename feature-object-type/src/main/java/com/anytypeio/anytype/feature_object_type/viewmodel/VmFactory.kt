package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Command.ObjectTypeSetRecommendedFields
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.GetObjectTypeConflictingFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
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
    private val createObject: CreateObject,
    private val fieldParser: FieldParser,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val userSettingsRepository: UserSettingsRepository,
    private val deleteObjects: DeleteObjects,
    private val setObjectDetails: SetObjectDetails,
    private val createObjectSet: CreateObjectSet,
    private val stringResourceProvider: StringResourceProvider,
    private val createTemplate: CreateTemplate,
    private val duplicateObjects: DuplicateObjects,
    private val getObjectTypeConflictingFields: GetObjectTypeConflictingFields,
    private val objectTypeSetRecommendedFields: SetObjectTypeRecommendedFields
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
            createObject = createObject,
            fieldParser = fieldParser,
            templatesContainer = templatesContainer,
            coverImageHashProvider = coverImageHashProvider,
            userSettingsRepository = userSettingsRepository,
            deleteObjects = deleteObjects,
            setObjectDetails = setObjectDetails,
            createObjectSet = createObjectSet,
            stringResourceProvider = stringResourceProvider,
            createTemplate = createTemplate,
            duplicateObjects = duplicateObjects,
            getObjectTypeConflictingFields = getObjectTypeConflictingFields,
            objectTypeSetRecommendedFields = objectTypeSetRecommendedFields
        ) as T
}