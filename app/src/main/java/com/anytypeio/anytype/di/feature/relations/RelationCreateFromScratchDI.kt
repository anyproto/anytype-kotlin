package com.anytypeio.anytype.di.feature.relations

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_utils.di.scope.CreateFromScratch
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.relations.AddNewRelationToObject
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForDataViewViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectBlockViewModel
import com.anytypeio.anytype.presentation.relations.RelationCreateFromScratchForObjectViewModel
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForDataViewFragment
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectBlockFragment
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchForObjectFragment
import com.anytypeio.anytype.ui.relations.RelationCreateFromScratchFormatPickerFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.StateFlow

@Subcomponent(modules = [RelationCreateFromScratchForObjectModule::class])
@CreateFromScratch
interface RelationCreateFromScratchForObjectSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchForObjectModule): Builder
        fun build(): RelationCreateFromScratchForObjectSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchForObjectFragment)

    fun relationFormatPickerComponent(): RelationFormatPickerSubcomponent.Builder
    fun limitObjectTypeComponent() : LimitObjectTypeSubComponent.Builder
}

@Module
object RelationCreateFromScratchForObjectModule {

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideViewModelFactory(
        addNewRelationToObject: AddNewRelationToObject,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        createFromScratchState: StateHolder<CreateFromScratchState>
    ): RelationCreateFromScratchForObjectViewModel.Factory =
        RelationCreateFromScratchForObjectViewModel.Factory(
            addNewRelationToObject = addNewRelationToObject,
            dispatcher = dispatcher,
            analytics = analytics,
            createFromScratchState = createFromScratchState
        )

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideAddNewRelationToObjectUseCase(
        repo: BlockRepository
    ): AddNewRelationToObject = AddNewRelationToObject(repo)

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideState(): StateHolder<CreateFromScratchState> = StateHolder(
        initial = CreateFromScratchState(
            format = RelationFormat.SHORT_TEXT,
            limitObjectTypes = emptyList()
        )
    )
}

@Subcomponent(modules = [RelationCreateFromScratchForDataViewModule::class])
@CreateFromScratch
interface RelationCreateFromScratchForDataViewSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchForDataViewModule): Builder
        fun build(): RelationCreateFromScratchForDataViewSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchForDataViewFragment)

    fun relationFormatPickerComponent(): RelationFormatPickerSubcomponent.Builder
    fun limitObjectTypeComponent() : LimitObjectTypeSubComponent.Builder
}

@Module
object RelationCreateFromScratchForDataViewModule {
    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideViewModelFactory(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession,
        updateDataViewViewer: UpdateDataViewViewer,
        addNewRelationToDataView: AddNewRelationToDataView,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        createFromScratchState: StateHolder<CreateFromScratchState>
    ): RelationCreateFromScratchForDataViewViewModel.Factory =
        RelationCreateFromScratchForDataViewViewModel.Factory(
            addNewRelationToDataView = addNewRelationToDataView,
            dispatcher = dispatcher,
            state = state,
            session = session,
            updateDataViewViewer = updateDataViewViewer,
            analytics = analytics,
            createFromScratchState = createFromScratchState
        )

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideState(): StateHolder<CreateFromScratchState> = StateHolder(
        initial = CreateFromScratchState(
            format = RelationFormat.SHORT_TEXT,
            limitObjectTypes = emptyList()
        )
    )
}

@Subcomponent(modules = [RelationCreateFromScratchForObjectBlockModule::class])
@CreateFromScratch
interface RelationCreateFromScratchForObjectBlockSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: RelationCreateFromScratchForObjectBlockModule): Builder
        fun build(): RelationCreateFromScratchForObjectBlockSubComponent
    }

    fun inject(fragment: RelationCreateFromScratchForObjectBlockFragment)

    fun relationFormatPickerComponent(): RelationFormatPickerSubcomponent.Builder
    fun limitObjectTypeComponent() : LimitObjectTypeSubComponent.Builder
}

@Module
object RelationCreateFromScratchForObjectBlockModule {
    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideViewModelFactory(
        addNewRelationToObject: AddNewRelationToObject,
        dispatcher: Dispatcher<Payload>,
        analytics: Analytics,
        createFromScratchState: StateHolder<CreateFromScratchState>
    ): RelationCreateFromScratchForObjectBlockViewModel.Factory =
        RelationCreateFromScratchForObjectBlockViewModel.Factory(
            addNewRelationToObject = addNewRelationToObject,
            dispatcher = dispatcher,
            analytics = analytics,
            createFromScratchState = createFromScratchState
        )

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideAddNewRelationToObjectUseCase(
        repo: BlockRepository
    ): AddNewRelationToObject = AddNewRelationToObject(repo)

    @JvmStatic
    @Provides
    @CreateFromScratch
    fun provideState(): StateHolder<CreateFromScratchState> = StateHolder(
        initial = CreateFromScratchState(
            format = RelationFormat.SHORT_TEXT,
            limitObjectTypes = emptyList()
        )
    )
}

@Subcomponent
@PerDialog
interface RelationFormatPickerSubcomponent {
    @Subcomponent.Builder
    interface Builder {
        fun build(): RelationFormatPickerSubcomponent
    }

    fun inject(fragment: RelationCreateFromScratchFormatPickerFragment)
}

