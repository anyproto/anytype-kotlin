package com.anytypeio.anytype.di.common

import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.di.feature.CreateBookmarkModule
import com.anytypeio.anytype.di.feature.CreateObjectModule
import com.anytypeio.anytype.di.feature.DaggerAllContentComponent
import com.anytypeio.anytype.di.feature.DaggerAppPreferencesComponent
import com.anytypeio.anytype.di.feature.DaggerBacklinkOrAddToObjectComponent
import com.anytypeio.anytype.di.feature.DaggerCreateObjectFeatureComponent
import com.anytypeio.anytype.di.feature.DaggerCreateObjectTypeComponent
import com.anytypeio.anytype.di.feature.DaggerDateObjectComponent
import com.anytypeio.anytype.di.feature.DaggerEditTypePropertiesComponent
import com.anytypeio.anytype.di.feature.DaggerLinkToObjectComponent
import com.anytypeio.anytype.di.feature.DaggerMediaComponent
import com.anytypeio.anytype.di.feature.DaggerMoveToComponent
import com.anytypeio.anytype.di.feature.DaggerMySitesComponent
import com.anytypeio.anytype.di.feature.DaggerObjectTypeComponent
import com.anytypeio.anytype.di.feature.DaggerPublishToWebComponent
import com.anytypeio.anytype.di.feature.DaggerSpacePropertiesComponent
import com.anytypeio.anytype.di.feature.DaggerSpaceTypesComponent
import com.anytypeio.anytype.di.feature.DaggerSplashComponent
import com.anytypeio.anytype.di.feature.DebugSettingsModule
import com.anytypeio.anytype.di.feature.DefaultComponentParam
import com.anytypeio.anytype.di.feature.EditorSessionModule
import com.anytypeio.anytype.di.feature.EditorUseCaseModule
import com.anytypeio.anytype.di.feature.KeychainPhraseModule
import com.anytypeio.anytype.di.feature.LinkToObjectOrWebModule
import com.anytypeio.anytype.di.feature.MainEntryModule
import com.anytypeio.anytype.di.feature.ModifyViewerSortModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceIconModule
import com.anytypeio.anytype.di.feature.ObjectAppearancePreviewLayoutModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceSettingModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerBaseModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectMenuModule
import com.anytypeio.anytype.di.feature.ObjectMenuModuleBase
import com.anytypeio.anytype.di.feature.ObjectRelationListModule
import com.anytypeio.anytype.di.feature.ObjectSetCreateBookmarkRecordModule
import com.anytypeio.anytype.di.feature.ObjectSetIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectSetMenuModule
import com.anytypeio.anytype.di.feature.ObjectSetModule
import com.anytypeio.anytype.di.feature.ObjectSetRecordModule
import com.anytypeio.anytype.di.feature.ObjectSetSettingsModule
import com.anytypeio.anytype.di.feature.RelationDataViewDateValueModule
import com.anytypeio.anytype.di.feature.RelationDataViewTextValueModule
import com.anytypeio.anytype.di.feature.RelationDateValueModule
import com.anytypeio.anytype.di.feature.RelationTextValueModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectSetModule
import com.anytypeio.anytype.di.feature.SelectSortRelationModule
import com.anytypeio.anytype.di.feature.TextBlockIconPickerModule
import com.anytypeio.anytype.di.feature.ViewerFilterModule
import com.anytypeio.anytype.di.feature.ViewerSortModule
import com.anytypeio.anytype.di.feature.auth.DaggerDeletedAccountComponent
import com.anytypeio.anytype.di.feature.chats.DaggerChatComponent
import com.anytypeio.anytype.di.feature.chats.DaggerChatReactionComponent
import com.anytypeio.anytype.di.feature.chats.DaggerSelectChatIconComponent
import com.anytypeio.anytype.di.feature.chats.DaggerSelectChatReactionComponent
import com.anytypeio.anytype.di.feature.cover.UnsplashModule
import com.anytypeio.anytype.di.feature.gallery.DaggerGalleryInstallationComponent
import com.anytypeio.anytype.di.feature.home.DaggerHomeScreenComponent
import com.anytypeio.anytype.di.feature.membership.DaggerMembershipComponent
import com.anytypeio.anytype.di.feature.membership.DaggerMembershipUpdateComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerRequestJoinSpaceComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerShareSpaceComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerSpaceJoinRequestComponent
import com.anytypeio.anytype.di.feature.notifications.DaggerNotificationComponent
import com.anytypeio.anytype.di.feature.notifications.DaggerPushContentComponent
import com.anytypeio.anytype.di.feature.objects.DaggerSelectObjectTypeComponent
import com.anytypeio.anytype.di.feature.onboarding.DaggerOnboardingComponent
import com.anytypeio.anytype.di.feature.onboarding.DaggerOnboardingStartComponent
import com.anytypeio.anytype.di.feature.onboarding.login.DaggerOnboardingMnemonicLoginComponent
import com.anytypeio.anytype.di.feature.onboarding.signup.DaggerOnboardingMnemonicComponent
import com.anytypeio.anytype.di.feature.onboarding.signup.DaggerOnboardingSoulCreationComponent
import com.anytypeio.anytype.di.feature.participant.DaggerParticipantComponent
import com.anytypeio.anytype.di.feature.relations.DaggerRelationCreateFromLibraryComponent
import com.anytypeio.anytype.di.feature.relations.LimitObjectTypeModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectBlockModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectModule
import com.anytypeio.anytype.di.feature.search.DaggerGlobalSearchComponent
import com.anytypeio.anytype.di.feature.sets.CreateFilterModule
import com.anytypeio.anytype.di.feature.sets.ModifyFilterModule
import com.anytypeio.anytype.di.feature.sets.PickConditionModule
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationModule
import com.anytypeio.anytype.di.feature.settings.DaggerAboutAppComponent
import com.anytypeio.anytype.di.feature.settings.DaggerAppearanceComponent
import com.anytypeio.anytype.di.feature.settings.DaggerDebugComponent
import com.anytypeio.anytype.di.feature.settings.DaggerFilesStorageComponent
import com.anytypeio.anytype.di.feature.settings.DaggerSpacesStorageComponent
import com.anytypeio.anytype.di.feature.settings.LogoutWarningModule
import com.anytypeio.anytype.di.feature.settings.ProfileModule
import com.anytypeio.anytype.di.feature.sharing.DaggerSharingComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerCreateSpaceComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerManageSectionsComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerSpaceListComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerSpaceSettingsComponent
import com.anytypeio.anytype.di.feature.templates.DaggerTemplateBlankComponent
import com.anytypeio.anytype.di.feature.templates.DaggerTemplateSelectComponent
import com.anytypeio.anytype.di.feature.vault.DaggerVaultComponent
import com.anytypeio.anytype.di.feature.widgets.DaggerCreateChatObjectComponent
import com.anytypeio.anytype.di.feature.widgets.DaggerSelectWidgetSourceComponent
import com.anytypeio.anytype.di.feature.widgets.DaggerSelectWidgetTypeComponent
import com.anytypeio.anytype.di.main.MainComponent
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatReactionViewModel
import com.anytypeio.anytype.feature_chats.presentation.ChatViewModel
import com.anytypeio.anytype.feature_chats.presentation.SelectChatReactionViewModel
import com.anytypeio.anytype.feature_create_object.presentation.NewCreateObjectViewModel
import com.anytypeio.anytype.feature_date.viewmodel.DateObjectVmParams
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.viewmodel.CreateTypeVmParams
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesViewModel
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.presentation.editor.EditorViewModel
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel
import com.anytypeio.anytype.presentation.home.HomeScreenVmParams
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModel
import com.anytypeio.anytype.presentation.moving.MoveToViewModel
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel
import com.anytypeio.anytype.presentation.objects.ObjectTypeChangeViewModel
import com.anytypeio.anytype.presentation.objects.SelectObjectTypeViewModel
import com.anytypeio.anytype.presentation.profile.ParticipantViewModel
import com.anytypeio.anytype.presentation.publishtoweb.MySitesViewModel
import com.anytypeio.anytype.presentation.publishtoweb.PublishToWebViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddViewModelBase
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.option.CreateOrEditOptionViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModel
import com.anytypeio.anytype.presentation.search.GlobalSearchViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.settings.SpacesStorageViewModel
import com.anytypeio.anytype.presentation.spaces.CreateSpaceViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.types.SpaceTypesViewModel
import com.anytypeio.anytype.presentation.widgets.CreateChatObjectViewModel
import com.anytypeio.anytype.presentation.widgets.collection.CollectionViewModel
import com.anytypeio.anytype.ui.widgets.collection.DaggerCollectionComponent
import timber.log.Timber

class ComponentManager(
    private val main: MainComponent,
    private val provider: HasComponentDependencies
) {

    val mainEntryComponent = Component {
        main.mainEntryComponentBuilder().module(MainEntryModule).build()
    }

    val debugSettingsComponent = Component {
        main
            .debugSettingsBuilder()
            .module(DebugSettingsModule())
            .build()
    }

    val debugComponent = Component {
        DaggerDebugComponent
            .factory()
            .create(findComponentDependencies())
    }

    val splashLoginComponent = Component {
       DaggerSplashComponent
           .factory()
           .create(findComponentDependencies())
    }

    val keychainPhraseComponent = Component {
        main
            .keychainPhraseComponentBuilder()
            .keychainPhraseModule(KeychainPhraseModule)
            .build()
    }

    val homeScreenComponent = ComponentWithParams { vmParams: HomeScreenVmParams ->
        DaggerHomeScreenComponent
            .factory()
            .create(
                vmParams = vmParams, dependencies = findComponentDependencies()
            )
    }

    val collectionComponent = ComponentWithParams { vmParams: CollectionViewModel.VmParams ->
        DaggerCollectionComponent
            .factory()
            .create(
                dependencies = findComponentDependencies(),
                vmParams = vmParams
            )
    }

    val selectWidgetSourceSubcomponent =
        ComponentWithParams { params: ObjectSearchViewModel.VmParams ->
            DaggerSelectWidgetSourceComponent.factory()
                .create(params, findComponentDependencies())
        }

    val selectWidgetTypeSubcomponent = Component {
        DaggerSelectWidgetTypeComponent.factory().create(
            findComponentDependencies()
        )
    }

    val createObjectComponent = Component {
        main
            .createObjectComponent()
            .module(CreateObjectModule)
            .build()
    }

    val templateSelectComponent = Component {
        DaggerTemplateSelectComponent.factory().create(findComponentDependencies())
    }

    val editorComponent = ComponentMapWithParam { param: DefaultComponentParam ->
        main
            .editorComponentBuilder()
            .session(EditorSessionModule)
            .usecase(EditorUseCaseModule)
            .withParams(
                EditorViewModel.Params(
                    ctx = param.ctx,
                    space = param.space
                )
            )
            .build()
    }

    val objectIconPickerComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectIconPickerModule)
            .build()
    }

    val textBlockIconPickerComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .textBlockIconPickerComponent()
            .module(TextBlockIconPickerModule)
            .build()
    }

    val objectSetIconPickerComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectSetIconPickerModule)
            .build()
    }

    val objectAppearanceSettingComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectAppearanceSettingComponent()
            .module(ObjectAppearanceSettingModule)
            .build()
    }

    val objectAppearanceIconComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectAppearanceIconComponent()
            .module(ObjectAppearanceIconModule)
            .build()
    }

    val objectAppearancePreviewLayoutComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectAppearancePreviewLayoutComponent()
            .module(ObjectAppearancePreviewLayoutModule)
            .build()
    }

    val objectAppearanceChooseDescriptionComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectAppearanceChooseDescription()
            .build()
    }

    val setTextBlockValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .setBlockTextValueComponent()
            .build()
    }

    val createBookmarkSubComponent = Component {
        main
            .createBookmarkBuilder()
            .createBookmarkModule(CreateBookmarkModule())
            .build()
    }


    val linkToObjectComponent = ComponentWithParams { param: ObjectSearchViewModel.VmParams ->
        DaggerLinkToObjectComponent
            .factory()
            .create(
                params = param,
                dependencies = findComponentDependencies()
            )
    }

    val linkToObjectOrWebComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .linkToObjectOrWebBuilder()
            .withParams(
                params = LinkToObjectOrWebViewModel.VmParams(
                    space = param.space
                )
            )
            .module(LinkToObjectOrWebModule)
            .build()
    }

    val moveToComponent = ComponentWithParams { params: MoveToViewModel.VmParams ->
        DaggerMoveToComponent.factory()
            .create(params, findComponentDependencies())
    }

    val globalSearchComponent = ComponentWithParams { params: GlobalSearchViewModel.VmParams ->
        DaggerGlobalSearchComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val allContentComponent = ComponentWithParams { params: AllContentViewModel.VmParams ->
        DaggerAllContentComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val participantScreenComponent = ComponentWithParams { params: ParticipantViewModel.VmParams ->
        DaggerParticipantComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val dateObjectComponent = ComponentWithParams { params: DateObjectVmParams  ->
        DaggerDateObjectComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val createObjectFeatureComponent = ComponentWithParams { params: NewCreateObjectViewModel.VmParams ->
        DaggerCreateObjectFeatureComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val objectSetComponent = ComponentMapWithParam { param: DefaultComponentParam ->
        main.objectSetComponentBuilder()
            .module(ObjectSetModule)
            .withParams(
                ObjectSetViewModel.Params(
                    ctx = param.ctx,
                    space = param.space
                )
            )
            .build()
    }

    val objectRelationListComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectRelationListComponent()
            .withVmParams(
                RelationListViewModel.VmParams(
                    objectId = param.ctx,
                    spaceId = param.space
                )
            )
            .module(ObjectRelationListModule)
            .build()
    }

    val objectSetRelationListComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectRelationListComponent()
            .withVmParams(
                RelationListViewModel.VmParams(
                    objectId = param.ctx,
                    spaceId = param.space
                )
            )
            .module(ObjectRelationListModule)
            .build()
    }

    val setOrCollectionRelationTextValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val dataViewRelationTextValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationDataViewTextValueComponent()
            .module(RelationDataViewTextValueModule)
            .build()
    }

    val relationTextValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val dataViewRelationDateValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .dataViewRelationDateValueComponent()
            .module(RelationDataViewDateValueModule)
            .build()
    }

    val setOrCollectionRelationDateValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationDateValueComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val objectRelationDateValueComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .editRelationDateComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val viewerFilterComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .viewerFilterBySubComponent()
            .module(ViewerFilterModule)
            .build()
    }

    val objectSetRecordComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetRecordComponent()
            .module(ObjectSetRecordModule)
            .build()
    }

    val objectSetCreateBookmarkRecordComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetCreateBookmarkRecordComponent()
            .module(ObjectSetCreateBookmarkRecordModule)
            .build()
    }

    val objectsSetSettingsComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetSettingsComponent()
            .module(ObjectSetSettingsModule)
            .build()
    }

    val selectSortRelationComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .selectSortRelationComponent()
            .module(SelectSortRelationModule)
            .build()
    }

    val selectFilterRelationComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .selectFilterRelationComponent()
            .module(SelectFilterRelationModule)
            .build()
    }

    val createFilterComponent = DependentComponentMap { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .createFilterComponent()
            .module(CreateFilterModule)
            .build()
    }

    val pickFilterConditionComponentCreate = ComponentWithParams { param: DefaultComponentParam ->
        createFilterComponent
            .get(key = param.ctx, param = param)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val pickFilterConditionComponentModify = ComponentWithParams { param: DefaultComponentParam ->
        modifyFilterComponent
            .get(key = param.ctx, param = param)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val modifyFilterComponent = DependentComponentMap { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .modifyFilterComponent()
            .module(ModifyFilterModule)
            .build()
    }

    val viewerSortComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .viewerSortComponent()
            .module(ViewerSortModule)
            .build()
    }

    val modifyViewerSortComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .modifyViewerSortComponent()
            .module(ModifyViewerSortModule)
            .build()
    }

    val objectCoverComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectCoverComponent()
            .module(SelectCoverObjectModule)
            .build()
    }

    val objectUnsplashComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetUnsplashComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetCoverComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetCoverComponent()
            .module(SelectCoverObjectSetModule)
            .build()
    }

    val objectMenuComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .objectMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectMenuModule)
            .build()
    }

    val objectSetMenuComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .objectSetMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectSetMenuModule)
            .build()
    }

    val relationAddToObjectComponent = ComponentWithParams { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .relationAddToObjectComponent()
            .withVmParams(
                vmParams = RelationAddViewModelBase.VmParams(
                    objectId = param.ctx,
                    space = param.space
                )
            )
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToObjectSetComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationAddToObjectComponent()
            .withVmParams(
                vmParams = RelationAddViewModelBase.VmParams(
                    objectId = param.ctx,
                    space = param.space
                )
            )
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToDataViewComponent = ComponentWithParams { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationAddToDataViewComponent()
            .withVmParams(
                vmParams = RelationAddViewModelBase.VmParams(
                    objectId = param.ctx,
                    space = param.space
                )
            )
            .module(RelationAddToDataViewModule)
            .build()
    }

    val relationCreateFromScratchForObjectComponent = DependentComponentMap { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .relationCreateFromScratchForObjectComponent()
            .module(RelationCreateFromScratchForObjectModule)
            .build()
    }

    val relationCreateFromScratchForObjectSetComponent = DependentComponentMap { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationCreateFromScratchForObjectComponent()
            .module(RelationCreateFromScratchForObjectModule)
            .build()
    }

    val relationCreateFromScratchForObjectBlockComponent = DependentComponentMap { param: DefaultComponentParam ->
        editorComponent
            .get(key = param.ctx, param = param)
            .relationCreateFromScratchForObjectBlockComponent()
            .module(RelationCreateFromScratchForObjectBlockModule)
            .build()
    }

    val relationCreateFromScratchForDataViewComponent = DependentComponentMap { param: DefaultComponentParam ->
        objectSetComponent
            .get(key = param.ctx, param = param)
            .relationCreateFromScratchForDataViewComponent()
            .module(RelationCreateFromScratchForDataViewModule)
            .build()
    }

    val relationFormatPickerObjectComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectComponent
            .get(key = param.ctx, param = param)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerLibraryComponent = ComponentWithParams { ctx: Id ->
        relationCreationFromLibraryComponent
            .get()
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerBlockComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectBlockComponent
            .get(key = param.ctx, param = param)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerDataViewComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForDataViewComponent
            .get(key = param.ctx, param = param)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerSetOrCollectionComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectSetComponent
            .get(key = param.ctx, param = param)
            .relationFormatPickerComponent()
            .build()
    }

    val limitObjectTypeObjectComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectComponent
            .get(key = param.ctx, param = param)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeBlockComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectBlockComponent
            .get(key = param.ctx, param = param)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeDataViewComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForDataViewComponent
            .get(key = param.ctx, param = param)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeSetOrCollectionComponent = ComponentWithParams { param: DefaultComponentParam ->
        relationCreateFromScratchForObjectSetComponent
            .get(key = param.ctx, param = param)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeLibraryComponent = ComponentWithParams { ctx: Id ->
        relationCreationFromLibraryComponent.get()
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val objectTypeChangeComponent = ComponentWithParams { vmParams: ObjectTypeChangeViewModel.VmParams ->
        main
            .objectTypeChangeComponent()
            .create(vmParams)
    }

    val templateBlankComponent = Component {
        DaggerTemplateBlankComponent.factory().create(findComponentDependencies())
    }

    // Settings

    val aboutAppComponent = Component {
        DaggerAboutAppComponent.factory().create(findComponentDependencies())
    }

    val profileComponent = Component {
        main.profileComponent().module(ProfileModule).build()
    }

    val logoutWarningComponent = Component {
        main.logoutWarningComponent().module(LogoutWarningModule).build()
    }

    val filesStorageComponent = Component {
        DaggerFilesStorageComponent.builder()
            .withDependencies(findComponentDependencies())
            .build()
    }

    val spacesStorageComponent = ComponentWithParams { vmParams: SpacesStorageViewModel.VmParams ->
        DaggerSpacesStorageComponent
            .factory()
            .create(
                vmParams = vmParams,
                dependency = findComponentDependencies()
            )
    }

    val appearanceComponent = Component {
        DaggerAppearanceComponent
            .factory()
            .create(findComponentDependencies())
    }

    val backLinkOrAddToObjectComponent = ComponentWithParams { vmParams: ObjectSearchViewModel.VmParams ->
        DaggerBacklinkOrAddToObjectComponent.builder()
            .withParams(vmParams)
            .withDependencies(findComponentDependencies())
            .build()
    }

    val relationCreationFromLibraryComponent = Component {
        DaggerRelationCreateFromLibraryComponent
            .factory()
            .create(findComponentDependencies())
    }

    val deletedAccountComponent = Component {
        DaggerDeletedAccountComponent
            .factory()
            .create(findComponentDependencies())
    }

    val onboardingComponent = Component {
        DaggerOnboardingComponent
            .factory()
            .create(findComponentDependencies())
    }

    val onboardingStartComponent = Component {
        DaggerOnboardingStartComponent
            .factory()
            .create(findComponentDependencies())
    }

    val onboardingMnemonicComponent = Component {
        DaggerOnboardingMnemonicComponent
            .factory()
            .create(findComponentDependencies())
    }

    val onboardingSoulCreationComponent = Component {
        DaggerOnboardingSoulCreationComponent
            .factory()
            .create(findComponentDependencies())
    }

    val onboardingMnemonicLoginComponent = Component {
        DaggerOnboardingMnemonicLoginComponent
            .factory()
            .create(findComponentDependencies())
    }

    val spaceListComponent = Component {
        DaggerSpaceListComponent
            .factory()
            .create(findComponentDependencies())
    }

    val createSpaceComponent = ComponentWithParams { vmParams: CreateSpaceViewModel.VmParams ->
        DaggerCreateSpaceComponent
            .factory()
            .create(vmParams, findComponentDependencies())
    }

    val spaceSettingsComponent = ComponentWithParams { vmParams: SpaceSettingsViewModel.VmParams ->
        DaggerSpaceSettingsComponent
            .factory()
            .create(
                vmParams = vmParams,
                dependencies = findComponentDependencies()
            )
    }

    val manageSectionsComponent = Component {
        DaggerManageSectionsComponent
            .factory()
            .create(findComponentDependencies())
    }

    val selectObjectTypeComponent = ComponentWithParams { params: SelectObjectTypeViewModel.Params ->
        DaggerSelectObjectTypeComponent
            .factory()
            .create(
                params = params,
                dependencies = findComponentDependencies()
            )
    }

    val appPreferencesComponent = Component {
        DaggerAppPreferencesComponent
            .factory()
            .create(findComponentDependencies())
    }

    val sharingComponent = Component {
        DaggerSharingComponent
            .factory()
            .create(findComponentDependencies())
    }

    val notificationsComponent = Component {
        DaggerNotificationComponent
            .factory()
            .create(findComponentDependencies())
    }

    val tagStatusObjectComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        editorComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .tagStatusObjectComponent()
            .params(params)
            .build()
    }

    val tagStatusSetComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .tagStatusSetComponent()
            .params(params)
            .build()
    }

    val tagStatusDataViewComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .tagStatusDataViewComponent()
            .params(params)
            .build()
    }

    val optionObjectComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        editorComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .optionObjectComponent()
            .params(params)
            .build()
    }

    val optionSetComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .optionSetComponent()
            .params(params)
            .build()
    }

    val optionDataViewComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .optionDataViewComponent()
            .params(params)
            .build()
    }

    val objectValueObjectComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        editorComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .objectValueComponent()
            .params(params)
            .build()
    }

    val objectValueSetComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .objectValueSetComponent()
            .params(params)
            .build()
    }

    val objectValueDataViewComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        objectSetComponent
            .get(
                key = params.ctx,
                param = DefaultComponentParam(
                    ctx = params.ctx,
                    space = params.space
                )
            )
            .objectValueDataViewComponent()
            .params(params)
            .build()
    }

    val shareSpaceComponent = ComponentWithParams { space: SpaceId ->
        DaggerShareSpaceComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params = ShareSpaceViewModel.VmParams(space))
            .build()
    }

    val spaceJoinRequestComponent = ComponentMapWithParam { params: SpaceJoinRequestViewModel.VmParams ->
        DaggerSpaceJoinRequestComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params = params)
            .build()
    }

    val requestToJoinSpaceComponent = ComponentWithParams { params: RequestJoinSpaceViewModel.Params ->
        DaggerRequestJoinSpaceComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params = params)
            .build()
    }

    val membershipComponent = Component {
        DaggerMembershipComponent.factory().create(findComponentDependencies())
    }

    val membershipUpgradeComponent = Component {
        DaggerMembershipUpdateComponent.factory().create(findComponentDependencies())
    }

    val galleryInstallationsComponent =
        ComponentWithParams { params: GalleryInstallationViewModel.ViewModelParams ->
            DaggerGalleryInstallationComponent.builder()
                .withDependencies(findComponentDependencies())
                .withParams(params)
                .build()
        }

    val versionHistoryComponent =
        ComponentWithParams { vmParams: VersionHistoryViewModel.VmParams ->
            editorComponent.get(
                key = vmParams.objectId,
                param = DefaultComponentParam(
                    ctx = vmParams.objectId,
                    space = vmParams.spaceId
                )
            ).versionHistoryComponent()
                .vmParams(vmParams)
                .build()
        }

    val chatComponent = ComponentMapWithParam { params: ChatViewModel.Params.Default ->
        DaggerChatComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params)
            .build()
    }

    val selectChatReactionComponent = ComponentMapWithParam { params: SelectChatReactionViewModel.Params ->
        DaggerSelectChatReactionComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params)
            .build()
    }

    val chatReactionComponent = ComponentMapWithParam { params: ChatReactionViewModel.Params ->
        DaggerChatReactionComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params)
            .build()
    }

    val selectChatIconComponent = Component {
        DaggerSelectChatIconComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .build()
    }

    val vaultComponent = Component {
        DaggerVaultComponent
            .factory()
            .create(findComponentDependencies())
    }

    val objectTypeComponent = ComponentWithParams { params: ObjectTypeVmParams ->
        DaggerObjectTypeComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val editTypePropertiesComponent = ComponentWithParams { params: EditTypePropertiesVmParams ->
        DaggerEditTypePropertiesComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val createObjectTypeComponent = ComponentWithParams { params: CreateTypeVmParams ->
        DaggerCreateObjectTypeComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val spaceTypesComponent = ComponentWithParams { params: SpaceTypesViewModel.VmParams ->
        DaggerSpaceTypesComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val spacePropertiesComponent = ComponentWithParams { params: SpacePropertiesViewModel.VmParams ->
        DaggerSpacePropertiesComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val pushContentComponent = Component {
        DaggerPushContentComponent
            .factory()
            .create(findComponentDependencies())
    }

    val publishToWebComponent = ComponentWithParams { params: PublishToWebViewModel.Params ->
        DaggerPublishToWebComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val mySitesComponent = ComponentWithParams { params: MySitesViewModel.VmParams ->
        DaggerMySitesComponent
            .factory()
            .create(params, findComponentDependencies())
    }

    val mediaComponent = Component {
        DaggerMediaComponent
            .factory()
            .create(findComponentDependencies())
    }

    val createChatObjectComponent = ComponentWithParams { vmParams: CreateChatObjectViewModel.VmParams ->
        DaggerCreateChatObjectComponent
            .factory()
            .create(vmParams, findComponentDependencies())
    }

    class Component<T>(private val builder: () -> T) {

        private var instance: T? = null

        fun get() = instance ?: builder().also { instance = it }

        fun new() = builder().also { instance = it }

        fun release() {
            instance = null
        }

        fun isInitialized() = instance != null

        override fun toString(): String {
            return if (BuildConfig.DEBUG) {
                instance?.toString().orEmpty()
            } else {
                super.toString()
            }
        }
    }

    class ComponentMap<T>(private val builder: () -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: String) = map[id] ?: builder().also { map[id] = it }

        fun new(id: String) = builder().also { map[id] = it }

        fun release(id: String) {
            map.remove(id)
        }

        fun isInitialized() = map.isNotEmpty()

        override fun toString(): String {
            return if (BuildConfig.DEBUG) {
                map.toString()
            } else {
                super.toString()
            }
        }
    }

    class ComponentMapWithParam<out T, in PARAMETER>(private val builder: (PARAMETER) -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(key: String, param: PARAMETER) = map[key] ?: builder(param).also { map[key] = it }

        fun new(id: String, param: PARAMETER) = builder(param).also { map[id] = it }

        fun release(id: String) {
            map.remove(id)
        }

        fun isInitialized() = map.isNotEmpty()

        override fun toString(): String {
            return if (BuildConfig.DEBUG) {
                map.toString()
            } else {
                super.toString()
            }
        }
    }

    class DependentComponentMap<out T, in PARAMETER>(private val builder: (PARAMETER) -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(key: String, param: PARAMETER) = map[key] ?: builder(param).also { map[key] = it }

        fun new(key: String, param: PARAMETER) = builder(param).also { map[key] = it }

        fun release(id: Id) {
            map.remove(id)
        }

        fun isInitialized() = map.isNotEmpty()

        override fun toString(): String {
            return if (BuildConfig.DEBUG) {
                map.toString()
            } else {
                super.toString()
            }
        }
    }

    class ComponentWithParams<out T, in PARAMETER>(private val builder: (PARAMETER) -> T) {

        private var instance: T? = null

        fun get(params: PARAMETER) = instance ?: builder(params).also { instance = it }

        fun new(params: PARAMETER) = builder(params).also { instance = it }

        fun release() {
            instance = null
        }
    }

    private inline fun <reified T : ComponentDependencies> findComponentDependencies(): T {
        return provider.dependencies[T::class.java] as T
    }

    /**
     * Can be used for debugging and tracing unreleased components.
     */
    fun logUnreleasedComponents() {
        if (BuildConfig.DEBUG) {
            javaClass.declaredFields.forEach { field ->
                val component = field.get(this)
                if (component != null) {
                    when (component) {
                        is Component<*> -> {
                            if (component.isInitialized()) {
                                Timber.d("Unreleased component: $component")
                            }
                        }
                        is DependentComponentMap<*, *> -> {
                            if (component.isInitialized()) {
                                Timber.d("Unreleased component: $component")
                            }
                        }
                        is ComponentMap<*> -> {
                            if (component.isInitialized()) {
                                Timber.d("Unreleased component: $component")
                            }
                        }
                    }
                }
            }
        }
    }
}