package com.anytypeio.anytype.presentation.linking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ext.rangeIntersection
import com.anytypeio.anytype.core_utils.tools.UrlValidator
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.editor.Editor
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchQueryEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsSearchResultEvent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import com.anytypeio.anytype.presentation.objects.toLinkToObjectView
import com.anytypeio.anytype.presentation.objects.toLinkToView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class LinkToObjectOrWebViewModel(
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val objectTypesProvider: ObjectTypesProvider,
    private val analytics: Analytics,
    private val stores: Editor.Storage,
    private val urlValidator: UrlValidator
) : ViewModel() {

    val viewState = MutableStateFlow<ViewState>(ViewState.Init)
    val commands = MutableSharedFlow<Command>(replay = 0)
    private val userInput = MutableStateFlow(ObjectSearchViewModel.EMPTY_QUERY)
    private val searchQuery = userInput
        .debounce(SEARCH_INPUT_DEBOUNCE)
        .distinctUntilChanged()

    private val objectTypes get() = objectTypesProvider.get().filter { !it.isArchived }

    private val _markupLinkParam = MutableStateFlow<String?>(null)
    val markupLinkParam: StateFlow<String?> = _markupLinkParam
    private val _markupObjectParam = MutableStateFlow<String?>(null)
    val markupObjectParam: StateFlow<String?> = _markupObjectParam
    private var clipboardUrl: String? = null

    private val jobs = mutableListOf<Job>()

    fun onStart(blockId: Id, rangeStart: Int, rangeEnd: Int, clipboardUrl: String?) {
        this.clipboardUrl = clipboardUrl
        setupBlockInRangeMarkupParam(blockId, rangeStart, rangeEnd)
        startObservingViewState()
    }

    fun onStop() {
        viewState.value = ViewState.Success(emptyList())
        clipboardUrl = null
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    private fun startObservingViewState() {
        jobs += viewModelScope.launch {
            searchQuery.collectLatest { query ->
                sendSearchQueryEvent(query.length)
                val params = getSearchObjectsParams().copy(fulltext = query)
                searchObjects(params).process(
                    success = { searchResponse ->
                        val result = proceedWithSearchObjectsResponse(searchResponse)
                        viewState.value = ViewState.Success(result)
                    },
                    failure = { Timber.e(it, "Error while searching for objects") }
                )
            }
        }
    }

    private fun proceedWithSearchObjectsResponse(searchResponse: List<ObjectWrapper.Basic>): List<LinkToItemView> {
        val linkUrl = _markupLinkParam.value
        val linkObject = _markupObjectParam.value
        val input = userInput.value
        return when {
            input.isBlank() && linkUrl != null -> {
                listOf(
                    LinkToItemView.Subheading.LinkedTo,
                    LinkToItemView.LinkedTo.Url(linkUrl),
                    LinkToItemView.Subheading.Actions,
                    LinkToItemView.RemoveLink,
                    LinkToItemView.CopyLink(linkUrl)
                )
            }
            input.isBlank() && linkObject != null -> {
                val obj = searchResponse.firstOrNull { it.id == linkObject }
                if (obj != null) {
                    listOf(
                        LinkToItemView.Subheading.LinkedTo,
                        obj.toLinkToObjectView(urlBuilder, objectTypes),
                        LinkToItemView.Subheading.Actions,
                        LinkToItemView.RemoveLink
                    )

                } else {
                    listOf(
                        LinkToItemView.Subheading.Actions,
                        LinkToItemView.RemoveLink
                    )
                }
            }
            else -> {
                val filteredSearchResponse =
                    searchResponse.filter { SupportedLayouts.layouts.contains(it.layout) }
                val objectViews = filteredSearchResponse.toLinkToView(
                    urlBuilder = urlBuilder,
                    objectTypes = objectTypes
                )
                val views = mutableListOf<LinkToItemView>()
                if (clipboardUrl != null && userInput.value.isBlank()) {
                    views.add(LinkToItemView.Subheading.Web)
                    views.add(LinkToItemView.PasteFromClipboard)
                }
                if (userInput.value.isNotEmpty() && urlValidator.isValid(userInput.value)) {
                    views.add(LinkToItemView.Subheading.Web)
                    views.add(LinkToItemView.WebItem(userInput.value))
                }
                views.add(LinkToItemView.Subheading.Objects)
                views.addAll(objectViews)
                views.add(LinkToItemView.CreateObject(userInput.value))
                views
            }
        }
    }

    private fun setupBlockInRangeMarkupParam(blockId: Id, rangeStart: Int, rangeEnd: Int) {
        val block = stores.document.get().firstOrNull { it.id == blockId }
        val content = block?.content
        if (content is Block.Content.Text) {
            val text = content.text
            if (rangeStart >= 0 && rangeEnd <= text.length && (rangeStart != rangeEnd)) {
                val range = IntRange(start = rangeStart, endInclusive = rangeEnd)
                val marks = block.content.asText().marks
                val filteredMarks = marks.filter { mark ->
                    (mark.type == Block.Content.Text.Mark.Type.LINK
                            || mark.type == Block.Content.Text.Mark.Type.OBJECT)
                            && mark.rangeIntersection(range) > 0
                }
                if (filteredMarks.isNotEmpty()) {
                    if (filteredMarks[0].type == Block.Content.Text.Mark.Type.LINK) {
                        _markupLinkParam.value = filteredMarks[0].param
                    }
                    if (filteredMarks[0].type == Block.Content.Text.Mark.Type.OBJECT) {
                        _markupObjectParam.value = filteredMarks[0].param
                    }
                }
            } else {
                viewState.value = ViewState.ErrorSelection
            }
        } else {
            viewState.value = ViewState.ErrorSelectedBlock
        }
    }

    fun onClicked(item: LinkToItemView) {
        Timber.d("onClicked, item:[$item] ")
        viewModelScope.launch {
            when (item) {
                is LinkToItemView.CreateObject -> {
                    commands.emit(Command.CreateAndSetObjectAsLink(objectName = item.title))
                }
                is LinkToItemView.Object -> {
                    onObjectClickEvent(item.position)
                    commands.emit(Command.SetObjectAsLink(objectId = item.id))
                }
                is LinkToItemView.WebItem -> {
                    commands.emit(Command.SetUrlAsLink(url = item.url))
                }
                is LinkToItemView.CopyLink -> {
                    commands.emit(Command.CopyLink(link = item.link))
                }
                is LinkToItemView.LinkedTo.Object -> {
                    commands.emit(Command.OpenObject(objectId = item.id))
                }
                is LinkToItemView.LinkedTo.Url -> {
                    commands.emit(Command.OpenUrl(url = item.url))
                }
                LinkToItemView.PasteFromClipboard -> {
                    clipboardUrl?.let {
                        commands.emit(Command.SetUrlAsLink(url = it))
                    }
                }
                LinkToItemView.RemoveLink -> {
                    commands.emit(Command.RemoveLink)
                }
                is LinkToItemView.Subheading -> {}
            }
        }
    }

    private fun onObjectClickEvent(position: Int) {
        viewModelScope.sendAnalyticsSearchResultEvent(
            analytics = analytics,
            pos = position + 1,
            length = userInput.value.length
        )
    }

    fun getSearchObjectsParams() = SearchObjects.Params(
        limit = ObjectSearchViewModel.SEARCH_LIMIT,
        filters = ObjectSearchConstants.getFilterLinkTo(ignore = null),
        sorts = ObjectSearchConstants.sortLinkTo,
        fulltext = ObjectSearchViewModel.EMPTY_QUERY
    )

    fun onSearchTextChanged(searchText: String) {
        userInput.value = searchText
    }

    private fun sendSearchQueryEvent(length: Int) {
        viewModelScope.sendAnalyticsSearchQueryEvent(
            analytics = analytics,
            route = EventsDictionary.Routes.searchMenu,
            length = length
        )
    }

    sealed class Command {
        object Exit : Command()
        data class SetUrlAsLink(val url: String) : Command()
        data class SetObjectAsLink(val objectId: Id) : Command()
        data class CreateAndSetObjectAsLink(val objectName: String) : Command()
        data class CopyLink(val link: String) : Command()
        object RemoveLink : Command()
        data class OpenObject(val objectId: Id) : Command()
        data class OpenUrl(val url: String) : Command()
    }

    sealed class ViewState {
        data class Success(
            val items: List<LinkToItemView>
        ) : ViewState()

        object Init : ViewState()
        data class SetFilter(val filter: String) : ViewState()
        object ErrorSelectedBlock : ViewState()
        object ErrorSelection : ViewState()
    }

    companion object {
        const val SEARCH_INPUT_DEBOUNCE = 300L
    }
}

sealed class LinkToItemView {
    sealed class Subheading : LinkToItemView() {
        object Objects : Subheading()
        object Web : Subheading()
        object LinkedTo : Subheading()
        object Actions : Subheading()
    }

    data class WebItem(val url: String) : LinkToItemView()
    data class CreateObject(val title: String) : LinkToItemView()
    data class Object(
        val id: Id,
        val title: String?,
        val subtitle: String?,
        val type: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val position: Int = 0
    ) : LinkToItemView()

    sealed class LinkedTo : LinkToItemView() {
        data class Url(val url: String) : LinkedTo()
        data class Object(
            val id: Id,
            val title: String?,
            val subtitle: String?,
            val type: String? = null,
            val layout: ObjectType.Layout? = null,
            val icon: ObjectIcon = ObjectIcon.None
        ) : LinkedTo()
    }

    object RemoveLink : LinkToItemView()
    data class CopyLink(val link: String) : LinkToItemView()
    object PasteFromClipboard : LinkToItemView()
}