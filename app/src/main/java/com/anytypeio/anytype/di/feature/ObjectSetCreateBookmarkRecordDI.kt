package com.anytypeio.anytype.di.feature;

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerDialog
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.sets.ObjectSetCreateBookmarkRecordViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.modals.SetObjectCreateBookmarkRecordFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ObjectSetCreateBookmarkRecordModule::class])
@PerDialog
interface ObjectSetCreateBookmarkRecordSubComponent {
    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectSetCreateBookmarkRecordModule): Builder
        fun build(): ObjectSetCreateBookmarkRecordSubComponent
    }

    fun inject(fragment: SetObjectCreateBookmarkRecordFragment)
}

@Module
object ObjectSetCreateBookmarkRecordModule {

    @JvmStatic
    @Provides
    @PerDialog
    fun provideObjectSetRecordViewModelFactory(
        createBookmarkObject: CreateBookmarkObject,
        urlValidator: UrlValidator,
        spaceManager: SpaceManager,
        objectState: MutableStateFlow<ObjectState>,
        dispatcher: Dispatcher<Payload>,
        addObjectToCollection: AddObjectToCollection,
        session: ObjectSetSession,
        storeOfRelations: StoreOfRelations,
        dateProvider: DateProvider
    ) = ObjectSetCreateBookmarkRecordViewModel.Factory(
        createBookmarkObject = createBookmarkObject,
        urlValidator = urlValidator,
        spaceManager = spaceManager,
        objectState = objectState,
        dispatcher = dispatcher,
        addObjectToCollection = addObjectToCollection,
        session = session,
        storeOfRelations = storeOfRelations,
        dateProvider = dateProvider
    )

    @JvmStatic
    @Provides
    @PerDialog
    fun provideCreateBookmarkObjectUseCase(
        repo: BlockRepository
    ): CreateBookmarkObject = CreateBookmarkObject(
        repo = repo
    )
}