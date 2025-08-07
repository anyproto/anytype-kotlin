package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.publishing.CreatePublishing
import com.anytypeio.anytype.domain.publishing.GetPublishingStatus
import com.anytypeio.anytype.domain.publishing.RemovePublishing
import com.anytypeio.anytype.domain.search.SearchObjects
import javax.inject.Inject

class PublishToWebViewModel(
    private val vmParams: Params,
    private val publish: CreatePublishing,
    private val getStatus: GetPublishingStatus,
    private val removePublishing: RemovePublishing,
    private val searchObjects: SearchObjects,
) : ViewModel() {

    init {

    }

    class Factory @Inject constructor(
        private val publish: CreatePublishing,
        private val getStatus: GetPublishingStatus,
        private val removePublishing: RemovePublishing,
        private val searchObjects: SearchObjects,
        private val params: Params,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublishToWebViewModel(
                vmParams = params,
                publish = publish,
                getStatus = getStatus,
                removePublishing = removePublishing,
                searchObjects = searchObjects
            ) as T
        }
    }

    data class Params(
        val ctx: Id,
        val space: SpaceId
    )
}
