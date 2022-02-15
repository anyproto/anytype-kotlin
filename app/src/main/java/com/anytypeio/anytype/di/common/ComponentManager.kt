package com.anytypeio.anytype.di.common

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.di.feature.*
import com.anytypeio.anytype.di.feature.relations.*
import com.anytypeio.anytype.di.feature.sets.CreateFilterModule
import com.anytypeio.anytype.di.feature.sets.ModifyFilterModule
import com.anytypeio.anytype.di.feature.sets.PickConditionModule
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerCardSizeSelectModule
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerImagePreviewSelectModule
import com.anytypeio.anytype.di.feature.wallpaper.WallpaperSelectModule
import com.anytypeio.anytype.di.main.MainComponent

class ComponentManager(private val main: MainComponent) {

    val mainComponent = main

    val mainEntryComponent = Component {
        main.mainEntryComponentBuilder().module(MainEntryModule).build()
    }

    private val authComponent = Component {
        main.authComponentBuilder().authModule(AuthModule).build()
    }

    val startLoginComponent = Component {
        authComponent
            .get()
            .startLoginComponentBuilder()
            .startLoginModule(StartLoginModule)
            .build()
    }

    val createAccountComponent = Component {
        authComponent
            .get()
            .createAccountComponentBuilder()
            .createAccountModule(CreateAccountModule)
            .build()
    }

    val setupNewAccountComponent = Component {
        authComponent
            .get()
            .setupNewAccountComponentBuilder()
            .setupNewAccountModule(SetupNewAccountModule)
            .build()
    }

    val setupSelectedAccountComponent = Component {
        authComponent
            .get()
            .setupSelectedAccountComponentBuilder()
            .setupSelectedAccountModule(SetupSelectedAccountModule)
            .build()
    }

    val selectAccountComponent = Component {
        authComponent
            .get()
            .selectAccountComponentBuilder()
            .selectAccountModule(SelectAccountModule)
            .build()
    }

    val keychainLoginComponent = Component {
        authComponent
            .get()
            .keychainLoginComponentBuilder()
            .keychainLoginModule(KeychainLoginModule)
            .build()
    }

    val profileComponent = Component {
        main
            .profileComponentBuilder()
            .profileModule(ProfileModule)
            .build()
    }

    val debugSettingsComponent = Component {
        main
            .debugSettingsBuilder()
            .module(DebugSettingsModule())
            .build()
    }

    val splashLoginComponent = Component {
        main
            .splashComponentBuilder()
            .module(SplashModule)
            .build()
    }

    val keychainPhraseComponent = Component {
        main
            .keychainPhraseComponentBuilder()
            .keychainPhraseModule(KeychainPhraseModule)
            .build()
    }

    val dashboardComponent = Component {
        main
            .homeDashboardComponentBuilder()
            .homeDashboardModule(HomeDashboardModule)
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

    val editorComponent = ComponentMap {
        main
            .editorComponentBuilder()
            .session(EditorSessionModule)
            .usecase(EditorUseCaseModule)
            .build()
    }

    val archiveComponent = ComponentMap {
        main.archiveComponentBuilder()
            .module(ArchiveModule)
            .build()
    }

    val linkAddComponent = Component {
        main
            .linkAddComponentBuilder()
            .linkModule(LinkModule())
            .build()
    }

    val objectIconPickerComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectIconPickerModule)
            .build()
    }

    val objectSetIconPickerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetIconPickerComponent()
            .base(ObjectIconPickerBaseModule)
            .module(ObjectSetIconPickerModule)
            .build()
    }

    val objectLayoutComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectLayoutComponent()
            .module(ObjectLayoutModule)
            .build()
    }

    val objectAppearanceSettingComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceSettingComponent()
            .module(ObjectAppearanceSettingModule)
            .build()
    }

    val objectAppearanceIconComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceIconComponent()
            .module(ObjectAppearanceIconModule)
            .build()
    }

    val objectAppearancePreviewLayoutComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearancePreviewLayoutComponent()
            .module(ObjectAppearancePreviewLayoutModule)
            .build()
    }

    val objectAppearanceCoverComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectAppearanceCoverComponent()
            .module(ObjectAppearanceCoverModule)
            .build()
    }

    val createBookmarkSubComponent = Component {
        main
            .createBookmarkBuilder()
            .createBookmarkModule(CreateBookmarkModule())
            .build()
    }

    val navigationComponent = Component {
        main.navigationComponentBuilder()
            .pageNavigationModule(PageNavigationModule)
            .build()
    }

    val otherSettingsComponent = Component {
        main.otherSettingsComponentBuilder()
            .module(OtherSettingsModule)
            .build()
    }

    val linkToObjectComponent = Component {
        main.linkToObjectBuilder()
            .module(LinkToObjectModule)
            .build()
    }

    val linkToObjectOrWebComponent = Component {
        main.linkToObjectOrWebBuilder()
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

    val createSetComponent = Component {
        main.createSetComponentBuilder()
            .module(CreateSetModule)
            .build()
    }

    val createObjectTypeComponent = Component {
        main.createObjectTypeComponentBuilder()
            .module(CreateObjectTypeModule)
            .build()
    }

    val objectSetComponent = ComponentMap {
        main.objectSetComponentBuilder()
            .module(ObjectSetModule)
            .build()
    }

    val documentRelationComponent = DependentComponentMap { id ->
        editorComponent
            .get(id)
            .documentRelationSubComponent()
            .module(DocumentRelationModule)
            .build()
    }

    val viewerSortByComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerSortBySubComponent()
            .module(ViewerSortByModule)
            .build()
    }

    val relationTextValueDVComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val relationTextValueComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationTextValueComponent()
            .module(RelationTextValueModule)
            .build()
    }

    val objectSetObjectRelationDataValueComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .relationDateValueComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val objectObjectRelationDateValueComponet = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .editRelationDateComponent()
            .module(RelationDateValueModule)
            .build()
    }

    val viewerFilterComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerFilterBySubComponent()
            .module(ViewerFilterModule)
            .build()
    }

    val viewerCustomizeComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerCustomizeSubComponent()
            .module(ViewerCustomizeModule)
            .build()
    }

    val objectSetRecordComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetRecordComponent()
            .module(ObjectSetRecordModule)
            .build()
    }

    val createDataViewViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .createDataViewViewerSubComponent()
            .module(CreateDataViewViewerModule)
            .build()
    }

    val editDataViewViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .editDataViewViewerComponent()
            .module(EditDataViewViewerModule)
            .build()
    }

    val objectSetObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectRelationValueComponent()
            .module(ObjectRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val objectObjectRelationValueComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .editDocRelationComponent()
            .module(ObjectRelationValueModule)
            .build()
    }

    val addObjectObjectRelationValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationValueComponent()
            .module(AddObjectRelationValueModule)
            .build()
    }

    val addObjectSetObjectRelationObjectValueComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationObjectValueModule)
            .build()
    }

    val addObjectRelationObjectValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addObjectRelationObjectValueComponent()
            .module(AddObjectRelationObjectValueModule)
            .build()
    }

    val relationFileValueComponent = DependentComponentMap { ctx ->
        objectObjectRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(RelationFileValueAddModule)
            .build()
    }

    val relationFileValueDVComponent = DependentComponentMap { ctx ->
        objectSetObjectRelationValueComponent
            .get(ctx)
            .addRelationFileValueAddComponent()
            .module(RelationFileValueAddModule)
            .build()
    }

    val manageViewerComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .manageViewerComponent()
            .module(ManageViewerModule)
            .build()
    }

    val viewerRelationsComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerRelationsComponent()
            .module(ViewerRelationsModule)
            .build()
    }

    val viewerCardSizeSelectComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerCardSizeSelectComponent()
            .module(ViewerCardSizeSelectModule)
            .build()
    }

    val viewerImagePreviewSelectComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerImagePreviewSelectComponent()
            .module(ViewerImagePreviewSelectModule)
            .build()
    }

    @Deprecated("Legacy")
    val dataviewViewerActionComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .dataviewViewerActionComponent()
            .module(DataViewViewerActionModule)
            .build()
    }

    val selectSortRelationComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .selectSortRelationComponent()
            .module(SelectSortRelationModule)
            .build()
    }

    val selectFilterRelationComponent = DependentComponentMap { ctx ->
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

    val pickFilterConditionComponentCreate = DependentComponentMap { ctx ->
        createFilterComponent
            .get(ctx)
            .createPickConditionComponent()
            .module(PickConditionModule)
            .build()
    }

    val pickFilterConditionComponentModify = DependentComponentMap { ctx ->
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

    val viewerSortComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .viewerSortComponent()
            .module(ViewerSortModule)
            .build()
    }

    val modifyViewerSortComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .modifyViewerSortComponent()
            .module(ModifyViewerSortModule)
            .build()
    }

    val objectCoverComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectCoverComponent()
            .module(SelectCoverObjectModule)
            .build()
    }

    val objectSetCoverComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetCoverComponent()
            .module(SelectCoverObjectSetModule)
            .build()
    }

    val objectMenuComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .objectMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectMenuModule)
            .build()
    }

    val objectSetMenuComponent = DependentComponentMap { ctx ->
        objectSetComponent
            .get(ctx)
            .objectSetMenuComponent()
            .base(ObjectMenuModuleBase)
            .module(ObjectSetMenuModule)
            .build()
    }

    val relationAddToObjectComponent = DependentComponentMap { ctx ->
        editorComponent
            .get(ctx)
            .relationAddToObjectComponent()
            .module(RelationAddToObjectModule)
            .build()
    }

    val relationAddToDataViewComponent = DependentComponentMap { ctx ->
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

    val objectTypeChangeComponent = Component {
        main
            .objectTypeChangeComponent()
            .module(ObjectTypeChangeModule)
            .build()
    }

    class Component<T>(private val builder: () -> T) {

        private var instance: T? = null

        fun get() = instance ?: builder().also { instance = it }

        fun new() = builder().also { instance = it }

        fun release() {
            instance = null
        }
    }

    class ComponentMap<T>(private val builder: () -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: String) = map[id] ?: builder().also { map[id] = it }

        fun new(id: String) = builder().also { map[id] = it }

        fun release(id: String) {
            map.remove(id)
        }
    }

    class DependentComponentMap<T>(private val builder: (Id) -> T) {

        private val map = mutableMapOf<String, T>()

        fun get(id: Id) = map[id] ?: builder(id).also { map[id] = it }

        fun new(id: Id) = builder(id).also { map[id] = it }

        fun release(id: Id) {
            map.remove(id)
        }
    }
}