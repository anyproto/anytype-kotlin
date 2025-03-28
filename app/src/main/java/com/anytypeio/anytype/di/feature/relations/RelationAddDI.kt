package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.GetRelations
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.relations.RelationAddToDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddToObjectViewModel
import com.anytypeio.anytype.presentation.relations.RelationAddViewModelBase
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.DATA_VIEW_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.INTRINSIC_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationAddToDataViewFragment
import com.anytypeio.anytype.ui.relations.RelationAddToObjectBlockFragment
import com.anytypeio.anytype.ui.relations.RelationAddToObjectFragment
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [RelationAddToObjectModule::class])
@PerDialog
interface RelationAddToObjectSubComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun withVmParams(vmParams: RelationAddViewModelBase.VmParams) : Builder
        fun module(module: RelationAddToObjectModule): Builder
        fun build(): RelationAddToObjectSubComponent
    }

    fun inject(fragment: RelationAddToObjectFragment)
    fun inject(fragment: RelationAddToObjectBlockFragment)
}

@Module
object RelationAddToObjectModule {
    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        vmParams: RelationAddViewModelBase.VmParams,
        addRelationToObject: AddRelationToObject,
        storeOfRelations: StoreOfRelations,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        @Named(INTRINSIC_PROVIDER_TYPE) relationsProvider: ObjectRelationProvider,
        getRelations: GetRelations,
        appCoroutineDispatchers: AppCoroutineDispatchers,
        addObjectToWorkspace: AddObjectToWorkspace,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ): RelationAddToObjectViewModel.Factory = RelationAddToObjectViewModel.Factory(
        vmParams = vmParams,
        storeOfRelations = storeOfRelations,
        addRelationToObject = addRelationToObject,
        dispatcher = dispatcher,
        analytics = analytics,
        relationsProvider = relationsProvider,
        getRelations = getRelations,
        appCoroutineDispatchers = appCoroutineDispatchers,
        addObjectToWorkspace = addObjectToWorkspace,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun getRelations(repo: BlockRepository) : GetRelations = GetRelations(repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun addObjectToWorkspace(
        repo: BlockRepository,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ) : AddObjectToWorkspace = AddObjectToWorkspace(
        repo = repo,
        dispatchers = appCoroutineDispatchers
    )
}

@Subcomponent(modules = [RelationAddToDataViewModule::class])
@PerDialog
interface RelationAddToDataViewSubComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun withVmParams(vmParams: RelationAddViewModelBase.VmParams) : Builder
        fun module(module: RelationAddToDataViewModule): Builder
        fun build(): RelationAddToDataViewSubComponent
    }

    fun inject(fragment: RelationAddToDataViewFragment)
}

@Module
object RelationAddToDataViewModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideViewModelFactory(
        vmParams: RelationAddViewModelBase.VmParams,
        addRelationToDataView: AddRelationToDataView,
        dispatcher: Dispatcher<Payload>,
        state: MutableStateFlow<ObjectState>,
        updateDataViewViewer: UpdateDataViewViewer,
        analytics: Analytics,
        @Named(DATA_VIEW_PROVIDER_TYPE) relationsProvider: ObjectRelationProvider,
        appCoroutineDispatchers: AppCoroutineDispatchers,
        getRelations: GetRelations,
        addObjectToWorkspace: AddObjectToWorkspace,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        storeOfRelations: StoreOfRelations
    ): RelationAddToDataViewViewModel.Factory = RelationAddToDataViewViewModel.Factory(
        addRelationToDataView = addRelationToDataView,
        dispatcher = dispatcher,
        state = state,
        updateDataViewViewer = updateDataViewViewer,
        analytics = analytics,
        relationsProvider = relationsProvider,
        appCoroutineDispatchers = appCoroutineDispatchers,
        getRelations = getRelations,
        addObjectToWorkspace = addObjectToWorkspace,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        storeOfRelations = storeOfRelations,
        vmParams = vmParams
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideAddRelationToDataViewUseCase(
        repo: BlockRepository
    ): AddRelationToDataView = AddRelationToDataView(repo)

    @JvmStatic
    @Provides
    @PerDialog
    fun getRelations(repo: BlockRepository) : GetRelations = GetRelations(repo)


    @JvmStatic
    @Provides
    @PerDialog
    fun addObjectToWorkspace(
        repo: BlockRepository,
        appCoroutineDispatchers: AppCoroutineDispatchers
    ) : AddObjectToWorkspace = AddObjectToWorkspace(
        repo = repo,
        dispatchers = appCoroutineDispatchers
    )
}