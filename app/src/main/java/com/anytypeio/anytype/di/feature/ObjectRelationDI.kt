package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.ObjectRelationList
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.page.Editor
import com.anytypeio.anytype.presentation.page.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.RelationListFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [DocumentRelationModule::class])
@PerModal
interface DocumentRelationSubComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: DocumentRelationModule): Builder
        fun build(): DocumentRelationSubComponent
    }

    fun inject(fragment: RelationListFragment)
}

@Module
object DocumentRelationModule {
    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectRelationViewModelFactory(
        stores: Editor.Storage,
        urlBuilder: UrlBuilder,
        objectRelationList: ObjectRelationList,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
        detailModificationManager: DetailModificationManager
    ): ObjectRelationListViewModelFactory {
        return ObjectRelationListViewModelFactory(
            stores = stores,
            urlBuilder = urlBuilder,
            objectRelationList = objectRelationList,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            detailModificationManager = detailModificationManager
        )
    }

    @JvmStatic
    @Provides
    @PerModal
    fun provideObjectRelationListUseCase(
        repository: BlockRepository
    ) : ObjectRelationList = ObjectRelationList(repository)
}