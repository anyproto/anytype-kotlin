package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerModal
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.relations.ObjectRelationListViewModelFactory
import com.anytypeio.anytype.presentation.relations.providers.RelationListProvider
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.relations.ObjectRelationListFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [ObjectRelationListModule::class])
@PerModal
interface ObjectRelationListComponent {

    @Subcomponent.Builder
    interface Builder {
        fun module(module: ObjectRelationListModule): Builder
        fun build(): ObjectRelationListComponent
    }

    fun inject(fragment: ObjectRelationListFragment)
}

@Module
object ObjectRelationListModule {
    @JvmStatic
    @Provides
    @PerModal
    fun factory(
        lockedStateProvider: LockedStateProvider,
        relationListProvider: RelationListProvider,
        urlBuilder: UrlBuilder,
        dispatcher: Dispatcher<Payload>,
        updateDetail: UpdateDetail,
        addToFeaturedRelations: AddToFeaturedRelations,
        removeFromFeaturedRelations: RemoveFromFeaturedRelations,
        deleteRelationFromObject: DeleteRelationFromObject,
        analytics: Analytics,
        storeOfRelations: StoreOfRelations,
        addRelationToObject: AddRelationToObject
    ): ObjectRelationListViewModelFactory {
        return ObjectRelationListViewModelFactory(
            lockedStateProvider = lockedStateProvider,
            relationListProvider = relationListProvider,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            addToFeaturedRelations = addToFeaturedRelations,
            removeFromFeaturedRelations = removeFromFeaturedRelations,
            deleteRelationFromObject = deleteRelationFromObject,
            analytics = analytics,
            storeOfRelations = storeOfRelations,
            addRelationToObject = addRelationToObject
        )
    }

    @JvmStatic
    @Provides
    @PerModal
    fun addToFeaturedRelations(repo: BlockRepository): AddToFeaturedRelations =
        AddToFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun removeFromFeaturedRelations(repo: BlockRepository): RemoveFromFeaturedRelations =
        RemoveFromFeaturedRelations(repo)

    @JvmStatic
    @Provides
    @PerModal
    fun deleteRelationFromObject(repo: BlockRepository): DeleteRelationFromObject =
        DeleteRelationFromObject(repo)
}