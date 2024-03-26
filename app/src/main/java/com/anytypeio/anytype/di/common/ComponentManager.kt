package com.anytypeio.anytype.di.common

import android.content.Context
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.di.feature.AddDataViewRelationObjectValueModule
import com.anytypeio.anytype.di.feature.AddDataViewRelationOptionValueModule
import com.anytypeio.anytype.di.feature.AddFileRelationModule
import com.anytypeio.anytype.di.feature.AddObjectRelationModule
import com.anytypeio.anytype.di.feature.AddObjectRelationValueModule
import com.anytypeio.anytype.di.feature.CreateBookmarkModule
import com.anytypeio.anytype.di.feature.CreateDataViewViewerModule
import com.anytypeio.anytype.di.feature.CreateObjectModule
import com.anytypeio.anytype.di.feature.DaggerAppPreferencesComponent
import com.anytypeio.anytype.di.feature.DaggerBacklinkOrAddToObjectComponent
import com.anytypeio.anytype.di.feature.DaggerSplashComponent
import com.anytypeio.anytype.di.feature.DataViewRelationValueModule
import com.anytypeio.anytype.di.feature.DebugSettingsModule
import com.anytypeio.anytype.di.feature.EditDataViewViewerModule
import com.anytypeio.anytype.di.feature.EditorSessionModule
import com.anytypeio.anytype.di.feature.EditorUseCaseModule
import com.anytypeio.anytype.di.feature.KeychainPhraseModule
import com.anytypeio.anytype.di.feature.LinkToObjectModule
import com.anytypeio.anytype.di.feature.LinkToObjectOrWebModule
import com.anytypeio.anytype.di.feature.MainEntryModule
import com.anytypeio.anytype.di.feature.ManageViewerModule
import com.anytypeio.anytype.di.feature.ModifyViewerSortModule
import com.anytypeio.anytype.di.feature.MoveToModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceIconModule
import com.anytypeio.anytype.di.feature.ObjectAppearancePreviewLayoutModule
import com.anytypeio.anytype.di.feature.ObjectAppearanceSettingModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerBaseModule
import com.anytypeio.anytype.di.feature.ObjectIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectLayoutModule
import com.anytypeio.anytype.di.feature.ObjectMenuModule
import com.anytypeio.anytype.di.feature.ObjectMenuModuleBase
import com.anytypeio.anytype.di.feature.ObjectObjectRelationValueModule
import com.anytypeio.anytype.di.feature.ObjectRelationListModule
import com.anytypeio.anytype.di.feature.ObjectSearchModule
import com.anytypeio.anytype.di.feature.ObjectSetCreateBookmarkRecordModule
import com.anytypeio.anytype.di.feature.ObjectSetIconPickerModule
import com.anytypeio.anytype.di.feature.ObjectSetMenuModule
import com.anytypeio.anytype.di.feature.ObjectSetModule
import com.anytypeio.anytype.di.feature.ObjectSetRecordModule
import com.anytypeio.anytype.di.feature.ObjectSetSettingsModule
import com.anytypeio.anytype.di.feature.ObjectTypeChangeModule
import com.anytypeio.anytype.di.feature.PersonalizationSettingsModule
import com.anytypeio.anytype.di.feature.RelationDataViewDateValueModule
import com.anytypeio.anytype.di.feature.RelationDataViewTextValueModule
import com.anytypeio.anytype.di.feature.RelationDateValueModule
import com.anytypeio.anytype.di.feature.RelationTextValueModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectModule
import com.anytypeio.anytype.di.feature.SelectCoverObjectSetModule
import com.anytypeio.anytype.di.feature.SelectSortRelationModule
import com.anytypeio.anytype.di.feature.SetOrCollectionRelationValueModule
import com.anytypeio.anytype.di.feature.TextBlockIconPickerModule
import com.anytypeio.anytype.di.feature.ViewerFilterModule
import com.anytypeio.anytype.di.feature.ViewerSortModule
import com.anytypeio.anytype.di.feature.auth.DaggerDeletedAccountComponent
import com.anytypeio.anytype.di.feature.cover.UnsplashModule
import com.anytypeio.anytype.di.feature.gallery.DaggerGalleryInstallationComponent
import com.anytypeio.anytype.di.feature.home.DaggerHomeScreenComponent
import com.anytypeio.anytype.di.feature.library.DaggerLibraryComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerRequestJoinSpaceComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerShareSpaceComponent
import com.anytypeio.anytype.di.feature.multiplayer.DaggerSpaceJoinRequestComponent
import com.anytypeio.anytype.di.feature.objects.DaggerSelectObjectTypeComponent
import com.anytypeio.anytype.di.feature.onboarding.DaggerOnboardingComponent
import com.anytypeio.anytype.di.feature.onboarding.DaggerOnboardingStartComponent
import com.anytypeio.anytype.di.feature.onboarding.login.DaggerOnboardingMnemonicLoginComponent
import com.anytypeio.anytype.di.feature.onboarding.signup.DaggerOnboardingMnemonicComponent
import com.anytypeio.anytype.di.feature.onboarding.signup.DaggerOnboardingSoulCreationComponent
import com.anytypeio.anytype.di.feature.payments.DaggerPaymentsComponent
import com.anytypeio.anytype.di.feature.relations.DaggerRelationCreateFromLibraryComponent
import com.anytypeio.anytype.di.feature.relations.DaggerRelationEditComponent
import com.anytypeio.anytype.di.feature.relations.LimitObjectTypeModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectBlockModule
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectModule
import com.anytypeio.anytype.di.feature.sets.CreateFilterModule
import com.anytypeio.anytype.di.feature.sets.ModifyFilterModule
import com.anytypeio.anytype.di.feature.sets.PickConditionModule
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerCardSizeSelectModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerImagePreviewSelectModule
import com.anytypeio.anytype.di.feature.settings.DaggerAboutAppComponent
import com.anytypeio.anytype.di.feature.settings.DaggerAppearanceComponent
import com.anytypeio.anytype.di.feature.settings.DaggerFilesStorageComponent
import com.anytypeio.anytype.di.feature.settings.DaggerSpacesStorageComponent
import com.anytypeio.anytype.di.feature.settings.LogoutWarningModule
import com.anytypeio.anytype.di.feature.settings.ProfileModule
import com.anytypeio.anytype.di.feature.sharing.DaggerAddToAnytypeComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerCreateSpaceComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerSelectSpaceComponent
import com.anytypeio.anytype.di.feature.spaces.DaggerSpaceSettingsComponent
import com.anytypeio.anytype.di.feature.templates.DaggerTemplateBlankComponent
import com.anytypeio.anytype.di.feature.templates.DaggerTemplateSelectComponent
import com.anytypeio.anytype.di.feature.types.DaggerCreateObjectTypeComponent
import com.anytypeio.anytype.di.feature.types.DaggerTypeEditComponent
import com.anytypeio.anytype.di.feature.types.DaggerTypeIconPickComponent
import com.anytypeio.anytype.di.feature.update.DaggerMigrationErrorComponent
import com.anytypeio.anytype.di.feature.wallpaper.WallpaperSelectModule
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetSourceModule
import com.anytypeio.anytype.di.feature.widgets.SelectWidgetTypeModule
import com.anytypeio.anytype.di.main.MainComponent
import com.anytypeio.anytype.gallery_experience.viewmodel.GalleryInstallationViewModel
import com.anytypeio.anytype.presentation.library.LibraryViewModel
import com.anytypeio.anytype.presentation.multiplayer.RequestJoinSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.ShareSpaceViewModel
import com.anytypeio.anytype.presentation.multiplayer.SpaceJoinRequestViewModel
import com.anytypeio.anytype.presentation.objects.SelectObjectTypeViewModel
import com.anytypeio.anytype.presentation.relations.option.CreateOrEditOptionViewModel
import com.anytypeio.anytype.presentation.relations.value.attachment.AttachmentValueViewModel
import com.anytypeio.anytype.presentation.relations.value.`object`.ObjectValueViewModel
import com.anytypeio.anytype.presentation.relations.value.tagstatus.TagOrStatusValueViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.ui.main.MainActivity
import com.anytypeio.anytype.ui.relations.RelationEditParameters
import com.anytypeio.anytype.ui.types.edit.TypeEditParameters
import com.anytypeio.anytype.ui.widgets.collection.DaggerCollectionComponent
import timber.log.Timber

class ComponentManager(
    private val main: MainComponent,
    private val provider: HasComponentDependencies
) {

    fun mainEntryComponent(activity: MainActivity) = Component {
        main.mainEntryComponentBuilder()
            .activity(activity)
            .module(MainEntryModule).build()
    }

    val debugSettingsComponent = Component {
        main
            .debugSettingsBuilder()
            .module(DebugSettingsModule())
            .build()
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

    val homeScreenComponent = Component {
        DaggerHomeScreenComponent
            .factory()
            .create(findComponentDependencies())
    }

    val collectionComponent = Component {
        DaggerCollectionComponent
            .factory()
            .create(findComponentDependencies())
    }

    val selectWidgetSourceSubcomponent = Component {
        homeScreenComponent
            .get()
            .selectWidgetSourceBuilder()
            .module(SelectWidgetSourceModule)
            .build()
    }

    val selectWidgetTypeSubcomponent = Component {
        homeScreenComponent
            .get()
            .selectWidgetTypeBuilder()
            .module(SelectWidgetTypeModule)
            .build()
    }

    val wallpaperSelectComponent = Component {
        main
            .wallpaperSelectComponent()
            .module(WallpaperSelectModule)
            .build()
    }

    val createObjectComponent = Component {
        main
            .createObjectComponent()
            .module(CreateObjectModule)
            .build()
    }

    val templateComponent = Component {
        main.templateComponentFactory().create()
    }

    val templateSelectComponent = Component {
        DaggerTemplateSelectComponent.factory().create(findComponentDependencies())
    }

    val editorComponent = ComponentMap {
        main
            .editorComponentBuilder()
            .session(EditorSessionModule)
            .usecase(EditorUseCaseModule)
            .build()
    }

    val objectIconPickerComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectIconPickerModule)
            .build()
    }

    val textBlockIconPickerComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .textBlockIconPickerComponent()
            .module(TextBlockIconPickerModule)
            .build()
    }

    val objectSetIconPickerComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectSetIconPickerModule)
            .build()
    }

    val objectLayoutComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectLayoutComponent()
            .module(ObjectLayoutModule)
            .build()
    }

    val objectAppearanceSettingComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectAppearanceSettingComponent()
            .module(ObjectAppearanceSettingModule)
            .build()
    }

    val objectAppearanceIconComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectAppearanceIconComponent()
            .module(ObjectAppearanceIconModule)
            .build()
    }

    val objectAppearancePreviewLayoutComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectAppearancePreviewLayoutComponent()
            .module(ObjectAppearancePreviewLayoutModule)
            .build()
    }

    val objectAppearanceChooseDescriptionComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectAppearanceChooseDescription()
            .build()
    }

    val setTextBlockValueComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .setBlockTextValueComponent()
            .build()
    }

    val createBookmarkSubComponent = Component {
        main
            .createBookmarkBuilder()
            .createBookmarkModule(CreateBookmarkModule())
            .build()
    }

    val personalizationSettingsComponent = Component {
        main.personalizationSettingsComponentBuilder()
            .module(PersonalizationSettingsModule)
            .build()
    }

    val linkToObjectComponent = Component {
        main.linkToObjectBuilder()
            .module(LinkToObjectModule)
            .build()
    }

    val linkToObjectOrWebComponent = ComponentWithParams { id: Id ->
        editorComponent.get(id)
            .linkToObjectOrWebBuilder()
            .module(LinkToObjectOrWebModule)
            .build()
    }

    val moveToComponent = Component {
        main
            .moveToBuilder()
            .module(MoveToModule)
            .build()
    }

    val objectSearchComponent = Component {
        main.objectSearchComponentBuilder()
            .module(ObjectSearchModule)
            .build()
    }

    val objectSetComponent = ComponentMap {
        main.objectSetComponentBuilder()
            .module(ObjectSetModule)
            .build()
    }

    val objectRelationListComponent = ComponentWithParams { id: Id ->
        editorComponent
            .get(id)
            .objectRelationListComponent()
            .module(ObjectRelationListModule)
            .build()
    }

    val objectSetRelationListComponent = ComponentWithParams { id: Id ->
        objectSetComponent
            .get(id)
            .objectRelationListComponent()
            .module(ObjectRelationListModule)
            .build()
    }

    val setOrCollectionRelationTextValueComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val dataViewRelationTextValueComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .relationDataViewTextValueComponent()
            .module(RelationDataViewTextValueModule)
            .build()
    }

    val relationTextValueComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val dataViewRelationDateValueComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .dataViewRelationDateValueComponent()
            .module(RelationDataViewDateValueModule)
            .build()
    }

    val setOrCollectionRelationDateValueComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .relationDateValueComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val objectRelationDateValueComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .editRelationDateComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val viewerFilterComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .viewerFilterBySubComponent()
            .module(ViewerFilterModule)
            .build()
    }

    val objectSetRecordComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetRecordComponent()
            .module(ObjectSetRecordModule)
            .build()
    }

    val objectSetCreateBookmarkRecordComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetCreateBookmarkRecordComponent()
            .module(ObjectSetCreateBookmarkRecordModule)
            .build()
    }

    val createDataViewViewerComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .createDataViewViewerSubComponent()
            .module(CreateDataViewViewerModule)
            .build()
    }

    val editDataViewViewerComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .editDataViewViewerComponent()
            .module(EditDataViewViewerModule)
            .build()
    }

    val dataViewRelationValueComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .dataViewObjectRelationValueComponent()
            .module(DataViewRelationValueModule)
            .build()
    }

    val setOrCollectionRelationValueComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .setOrCollectionRelationValueComponent()
            .module(SetOrCollectionRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationValueComponent = ComponentWithParams { ctx: Id ->
        dataViewRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val addDataViewObjectRelationValueComponent = ComponentWithParams { ctx: Id ->
        dataViewRelationValueComponent
            .get(ctx)
            .addDataViewRelationOptionValueComponent()
            .module(AddDataViewRelationOptionValueModule)
            .build()
    }

    val objectObjectRelationValueComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .editDocRelationComponent()
            .module(ObjectObjectRelationValueModule)
            .build()
    }

    val addObjectObjectRelationValueComponent = ComponentWithParams { ctx: Id ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationObjectValueComponent = ComponentWithParams { ctx: Id ->
        dataViewRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationModule)
            .build()
    }

    val addDataViewRelationObjectValueComponent = ComponentWithParams { ctx: Id ->
        dataViewRelationValueComponent
            .get(ctx)
            .addDataViewRelationObjectValueComponent()
            .module(AddDataViewRelationObjectValueModule)
            .build()
    }

    val addObjectRelationObjectValueComponent = ComponentWithParams { ctx: Id ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationModule)
            .build()
    }

    val relationFileValueComponent = ComponentWithParams { ctx: Id ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(AddFileRelationModule)
            .build()
    }

    val relationFileValueDVComponent = ComponentWithParams { ctx: Id ->
        dataViewRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(AddFileRelationModule)
            .build()
    }

    val manageViewerComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .manageViewerComponent()
            .module(ManageViewerModule)
            .build()
    }

    val objectsSetSettingsComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetSettingsComponent()
            .module(ObjectSetSettingsModule)
            .build()
    }

    val viewerCardSizeSelectComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .viewerCardSizeSelectComponent()
            .module(ViewerCardSizeSelectModule)
            .build()
    }

    val viewerImagePreviewSelectComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .viewerImagePreviewSelectComponent()
            .module(ViewerImagePreviewSelectModule)
            .build()
    }

    val selectSortRelationComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .selectSortRelationComponent()
            .module(SelectSortRelationModule)
            .build()
    }

    val selectFilterRelationComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .selectFilterRelationComponent()
            .module(SelectFilterRelationModule)
            .build()
    }

    val createFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .createFilterComponent()
            .module(CreateFilterModule)
            .build()
    }

    val pickFilterConditionComponentCreate = ComponentWithParams { ctx: Id ->
        createFilterComponent
            .get(ctx)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val pickFilterConditionComponentModify = ComponentWithParams { ctx: Id ->
        modifyFilterComponent
            .get(ctx)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val modifyFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .modifyFilterComponent()
            .module(ModifyFilterModule)
            .build()
    }

    val viewerSortComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .viewerSortComponent()
            .module(ViewerSortModule)
            .build()
    }

    val modifyViewerSortComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .modifyViewerSortComponent()
            .module(ModifyViewerSortModule)
            .build()
    }

    val objectCoverComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectCoverComponent()
            .module(SelectCoverObjectModule)
            .build()
    }

    val objectUnsplashComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetUnsplashComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectUnsplashComponent()
            .module(UnsplashModule)
            .build()
    }

    val objectSetCoverComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetCoverComponent()
            .module(SelectCoverObjectSetModule)
            .build()
    }

    val objectMenuComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .objectMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectMenuModule)
            .build()
    }

    val objectSetMenuComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .objectSetMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectSetMenuModule)
            .build()
    }

    val relationAddToObjectComponent = ComponentWithParams { ctx: Id ->
        editorComponent
            .get(ctx)
            .relationAddToObjectComponent()
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToObjectSetComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .relationAddToObjectComponent()
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToDataViewComponent = ComponentWithParams { ctx: Id ->
        objectSetComponent
            .get(ctx)
            .relationAddToDataViewComponent()
            .module(RelationAddToDataViewModule)
            .build()
    }

    val relationCreateFromScratchForObjectComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationCreateFromScratchForObjectComponent()
            .module(RelationCreateFromScratchForObjectModule)
            .build()
    }

    val relationCreateFromScratchForObjectSetComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationCreateFromScratchForObjectComponent()
            .module(RelationCreateFromScratchForObjectModule)
            .build()
    }

    val relationCreateFromScratchForObjectBlockComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationCreateFromScratchForObjectBlockComponent()
            .module(RelationCreateFromScratchForObjectBlockModule)
            .build()
    }

    val relationCreateFromScratchForDataViewComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationCreateFromScratchForDataViewComponent()
            .module(RelationCreateFromScratchForDataViewModule)
            .build()
    }

    val relationFormatPickerObjectComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerLibraryComponent = ComponentWithParams { ctx: Id ->
        relationCreationFromLibraryComponent
            .get()
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerBlockComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectBlockComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerDataViewComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForDataViewComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val relationFormatPickerSetOrCollectionComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectSetComponent
            .get(ctx)
            .relationFormatPickerComponent()
            .build()
    }

    val limitObjectTypeObjectComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeBlockComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectBlockComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeDataViewComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForDataViewComponent.get(ctx)
            .limitObjectTypeComponent()
            .module(LimitObjectTypeModule)
            .build()
    }

    val limitObjectTypeSetOrCollectionComponent = ComponentWithParams { ctx: Id ->
        relationCreateFromScratchForObjectSetComponent.get(ctx)
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

    val objectTypeChangeComponent = Component {
        main
            .objectTypeChangeComponent()
            .module(ObjectTypeChangeModule)
            .build()
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

    val spacesStorageComponent = Component {
        DaggerSpacesStorageComponent.builder()
            .withDependencies(findComponentDependencies())
            .build()
    }

    val appearanceComponent = Component {
        DaggerAppearanceComponent
            .factory()
            .create(findComponentDependencies())
    }

    val libraryComponent = ComponentWithParams { (ctx, params) : Pair<Context, LibraryViewModel.Params> ->
        DaggerLibraryComponent.builder()
            .withContext(ctx)
            .withParams(params)
            .withDependencies(findComponentDependencies())
            .build()
    }

    val backLinkOrAddToObjectComponent = ComponentWithParams { ctx: Id ->
        DaggerBacklinkOrAddToObjectComponent.builder()
            .withContext(ctx)
            .withDependencies(findComponentDependencies())
            .build()
    }

    val createObjectTypeComponent = Component {
        DaggerCreateObjectTypeComponent
            .factory()
            .create(findComponentDependencies())
    }

    val typeEditComponent = ComponentWithParams { params: TypeEditParameters ->
        DaggerTypeEditComponent.builder()
            .withId(params.id)
            .withName(params.name)
            .withIcon(params.icon)
            .withDependencies(findComponentDependencies())
            .build()
    }

    val relationEditComponent = ComponentWithParams { params: RelationEditParameters ->
        DaggerRelationEditComponent.builder()
            .withId(params.id)
            .withName(params.name)
            .withIcon(params.icon)
            .withDependencies(findComponentDependencies())
            .build()
    }

    val typeIconPickComponent = Component {
        DaggerTypeIconPickComponent
            .factory()
            .create(findComponentDependencies())
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

    val migrationErrorComponent = Component {
        DaggerMigrationErrorComponent
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

    val selectSpaceComponent = Component {
        DaggerSelectSpaceComponent
            .factory()
            .create(findComponentDependencies())
    }

    val createSpaceComponent = Component {
        DaggerCreateSpaceComponent
            .factory()
            .create(findComponentDependencies())
    }

    val spaceSettingsComponent = ComponentWithParams { space: SpaceId ->
        DaggerSpaceSettingsComponent
            .builder()
            .withParams(SpaceSettingsViewModel.Params(space))
            .withDependencies(findComponentDependencies())
            .build()
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

    val addToAnytypeComponent = Component {
        DaggerAddToAnytypeComponent
            .factory()
            .create(findComponentDependencies())
    }

    val tagStatusObjectComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        editorComponent.get(params.ctx)
            .tagStatusObjectComponent()
            .params(params)
            .build()
    }

    val tagStatusSetComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .tagStatusSetComponent()
            .params(params)
            .build()
    }

    val tagStatusDataViewComponent = ComponentWithParams { params: TagOrStatusValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .tagStatusDataViewComponent()
            .params(params)
            .build()
    }

    val optionObjectComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        editorComponent.get(params.ctx)
            .optionObjectComponent()
            .params(params)
            .build()
    }

    val optionSetComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .optionSetComponent()
            .params(params)
            .build()
    }

    val optionDataViewComponent = ComponentWithParams { params: CreateOrEditOptionViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .optionDataViewComponent()
            .params(params)
            .build()
    }

    val objectValueObjectComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        editorComponent.get(params.ctx)
            .objectValueComponent()
            .params(params)
            .build()
    }

    val objectValueSetComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .objectValueSetComponent()
            .params(params)
            .build()
    }

    val objectValueDataViewComponent = ComponentWithParams { params: ObjectValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .objectValueDataViewComponent()
            .params(params)
            .build()
    }

    val attachmentObjectComponent = ComponentWithParams { params: AttachmentValueViewModel.ViewModelParams ->
        editorComponent.get(params.ctx)
            .attachmentValueObjectComponent()
            .params(params)
            .build()
    }

    val attachmentSetComponent = ComponentWithParams { params: AttachmentValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .attachmentSetComponent()
            .params(params)
            .build()
    }

    val attachmentDataViewComponent = ComponentWithParams { params: AttachmentValueViewModel.ViewModelParams ->
        objectSetComponent.get(params.ctx)
            .attachmentDataViewComponent()
            .params(params)
            .build()
    }

    val shareSpaceComponent = ComponentWithParams { space: SpaceId ->
        DaggerShareSpaceComponent
            .builder()
            .withDependencies(findComponentDependencies())
            .withParams(params = ShareSpaceViewModel.Params(space))
            .build()
    }

    val spaceJoinRequestComponent = ComponentWithParams { params: SpaceJoinRequestViewModel.Params ->
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

    val paymentsComponent = Component {
        DaggerPaymentsComponent.factory().create(findComponentDependencies())
    }

    val galleryInstallationsComponent =
        ComponentWithParams { params: GalleryInstallationViewModel.ViewModelParams ->
            DaggerGalleryInstallationComponent.builder()
                .withDependencies(findComponentDependencies())
                .withParams(params)
                .build()
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

    class DependentComponentMap<T>(private val builder: (Id) -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: Id) = map[id] ?: builder(id).also { map[id] = it }

        fun new(id: Id) = builder(id).also { map[id] = it }

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
                        is DependentComponentMap<*> -> {
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