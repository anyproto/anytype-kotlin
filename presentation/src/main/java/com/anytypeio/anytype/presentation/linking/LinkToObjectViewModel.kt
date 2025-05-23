package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class LinkToObjectViewModel(
    private val vmParams: VmParams,
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    getObjectTypes: GetObjectTypes,
    analytics: Analytics,
    analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
) : ObjectSearchViewModel(
    vmParams = vmParams,
    urlBuilder = urlBuilder,
    getObjectTypes = getObjectTypes,
    searchObjects = searchObjects,
    analytics = analytics,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
    fieldParser = fieldParser,
    storeOfObjectTypes = storeOfObjectTypes
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        Timber.d("LinkToObjectViewModel init")
    }

    override suspend fun getSearchObjectsParams(ignore: Id?) = SearchObjects.Params(
        space = vmParams.space,
        limit = SEARCH_LIMIT,
        filters = ObjectSearchConstants.getFilterLinkTo(
            ignore = ignore
        ),
        sorts = ObjectSearchConstants.sortLinkTo,
        fulltext = EMPTY_QUERY,
        keys = ObjectSearchConstants.defaultKeys
    )

    override fun onObjectClicked(view: DefaultObjectView) {
        sendSearchResultEvent(view.id)
        viewModelScope.launch {
            commands.emit(
                Command.Link(
                    link = view.id,
                    isBookmark = view.layout == ObjectType.Layout.BOOKMARK
                )
            )
        }
    }

    override fun onDialogCancelled() {
        viewModelScope.launch {
            commands.emit(Command.Exit)
        }
    }

    override suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.emit(
            Resultat.success(data.filter {
                SupportedLayouts.layouts.contains(it.layout)
            })
        )
    }

    sealed class Command {
        data object Exit : Command()
        data class Link(
            val link: Id,
            val isBookmark: Boolean
        ) : Command()
    }
}