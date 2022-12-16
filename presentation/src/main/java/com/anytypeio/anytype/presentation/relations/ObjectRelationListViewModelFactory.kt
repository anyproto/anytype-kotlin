package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.relations.AddToFeaturedRelations
import com.anytypeio.anytype.domain.relations.DeleteRelationFromObject
import com.anytypeio.anytype.domain.relations.RemoveFromFeaturedRelations
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.editor.editor.DetailModificationManager
import com.anytypeio.anytype.presentation.util.Dispatcher

class ObjectRelationListViewModelFactory(
    private val stores: Editor.Storage,
    private val urlBuilder: UrlBuilder,
    private val dispatcher: Dispatcher<Payload>,
    private val updateDetail: UpdateDetail,
    private val detailModificationManager: DetailModificationManager,
    private val addToFeaturedRelations: AddToFeaturedRelations,
    private val removeFromFeaturedRelations: RemoveFromFeaturedRelations,
    private val deleteRelationFromObject: DeleteRelationFromObject,
    private val analytics: Analytics,
    private val storeOfRelations: StoreOfRelations
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RelationListViewModel(
            stores = stores,
            urlBuilder = urlBuilder,
            dispatcher = dispatcher,
            updateDetail = updateDetail,
            detailModificationManager = detailModificationManager,
            addToFeaturedRelations = addToFeaturedRelations,
            removeFromFeaturedRelations = removeFromFeaturedRelations,
            deleteRelationFromObject = deleteRelationFromObject,
            analytics = analytics,
            storeOfRelations = storeOfRelations
        ) as T
    }
}