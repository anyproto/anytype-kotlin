package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BackLinkOrAddToObjectViewModel(
    private val vmParams: VmParams,
    urlBuilder: UrlBuilder,
    searchObjects: SearchObjects,
    analytics: Analytics,
    analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes,
    spaceViews: SpaceViewSubscriptionContainer
) : ObjectSearchViewModel(
    vmParams = vmParams,
    urlBuilder = urlBuilder,
    searchObjects = searchObjects,
    analytics = analytics,
    analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
    fieldParser = fieldParser,
    storeOfObjectTypes = storeOfObjectTypes,
    spaceViews = spaceViews
) {

    private val _commands = MutableSharedFlow<Command>(replay = 0)
    val commands: SharedFlow<Command> = _commands

    /**
     * Adding a source object as a backlink is only possible in objects
     * with these Layouts and also in Collection objects.
     */
    private val supported = listOf(
        ObjectType.Layout.BASIC,
        ObjectType.Layout.PROFILE,
        ObjectType.Layout.TODO,
        ObjectType.Layout.NOTE,
        ObjectType.Layout.COLLECTION
    )

    override suspend fun getSearchObjectsParams(ignore: Id?): SearchObjects.Params {
        return SearchObjects.Params(
            space = vmParams.space,
            limit = SEARCH_LIMIT,
            filters = ObjectSearchConstants.filtersBackLinkOrAddToObject(
                ignore = ignore
            ),
            sorts = ObjectSearchConstants.sortBackLinkOrAddToObject,
            fulltext = EMPTY_QUERY,
            keys = ObjectSearchConstants.defaultKeys
        )
    }

    override fun onObjectClicked(view: DefaultObjectView) {
        sendSearchResultEvent(view.id)
        viewModelScope.launch {
            _commands.emit(
                Command.CreateBacklink(
                    id = view.id,
                    name = view.name,
                    layout = view.layout,
                    icon = view.icon
                )
            )
        }
    }

    override fun onDialogCancelled() {
        viewModelScope.launch {
            _commands.emit(Command.Exit)
        }
    }

    override suspend fun setObjects(data: List<ObjectWrapper.Basic>) {
        objects.value = Resultat.success(
            data.filter {
                supported.contains(it.layout)
            }
        )
    }

    sealed class Command {
        data object Exit : Command()
        data class CreateBacklink(
            val id: Id,
            val name: String,
            val layout: ObjectType.Layout?,
            val icon: ObjectIcon
        ) : Command()
    }
}