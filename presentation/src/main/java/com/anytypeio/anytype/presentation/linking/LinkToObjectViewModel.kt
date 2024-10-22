package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LinkToObjectViewModel(
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    getObjectTypes: GetObjectTypes,
    analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : ObjectSearchViewModel(
    urlBuilder = urlBuilder,
    getObjectTypes = getObjectTypes,
    searchObjects = searchObjects,
    analytics = analytics,
    spaceManager = spaceManager,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
) {

    val commands = MutableSharedFlow<Command>(replay = 0)

    override suspend fun getSearchObjectsParams(ignore: Id?) = SearchObjects.Params(
        // TODO DROID-2916 Provide space id to vm params
        space = SpaceId(spaceManager.get()),
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
        object Exit : Command()
        data class Link(
            val link: Id,
            val isBookmark: Boolean
        ) : Command()
    }
}