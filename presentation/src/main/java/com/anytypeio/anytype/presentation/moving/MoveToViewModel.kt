package com.anytypeio.anytype.presentation.moving

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.core_utils.ui.ViewStateViewModel
import com.anytypeio.anytype.domain.`object`.ObjectTypesProvider
import com.anytypeio.anytype.domain.block.interactor.Move
import com.anytypeio.anytype.domain.config.GetConfig
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.navigation.GetObjectInfoWithLinks
import com.anytypeio.anytype.presentation.mapper.getEmojiPath
import com.anytypeio.anytype.presentation.mapper.getImagePath
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.PageNavigationView
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MoveToViewModel(
    private val urlBuilder: UrlBuilder,
    private val getObjectInfoWithLinks: GetObjectInfoWithLinks,
    private val getConfig: GetConfig,
    private val move: Move,
    private val objectTypesProvider: ObjectTypesProvider
) : ViewStateViewModel<ViewState<PageNavigationView>>(),
    SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var pageId: String = ""
    private var home: String = ""

    val isMovingDisabled: MutableStateFlow<Boolean> = MutableStateFlow(true)

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun onStart(initialTarget: Id) {
        viewModelScope.launch {
            getConfig(Unit).proceed(
                failure = { Timber.e(it, "Error while getting config") },
                success = { config ->
                    home = config.home
                    proceedWithGettingDocumentLinks(initialTarget)
                }
            )
        }
    }

    fun proceedWithGettingDocumentLinks(target: String) {
        stateData.postValue(ViewState.Loading)
        viewModelScope.launch {
            getObjectInfoWithLinks.invoke(GetObjectInfoWithLinks.Params(pageId = target)).proceed(
                failure = { error ->
                    error.timber()
                    stateData.postValue(ViewState.Error(error.message ?: "Unknown error"))
                },
                success = { response ->
                    with(response.pageInfoWithLinks) {
                        pageId = this.id
                        stateData.postValue(
                            ViewState.Success(
                                PageNavigationView(
                                    title = documentInfo.obj.name.orEmpty(),
                                    subtitle = documentInfo.snippet.orEmpty(),
                                    image = documentInfo.obj.getImagePath(urlBuilder),
                                    emoji = documentInfo.obj.getEmojiPath(),
                                    inbound = links.inbound.map { it.toView(urlBuilder, objectTypesProvider.get()) },
                                    outbound = links.outbound.map { it.toView(urlBuilder, objectTypesProvider.get()) }
                                )
                            )
                        )
                    }
                }
            )
        }
    }

    fun onLinkClicked(
        target: Id,
        context: Id,
        excluded: List<Id>
    ) {
        isMovingDisabled.value = (target == context || target == home || excluded.contains(target))
        proceedWithGettingDocumentLinks(target)
    }

    fun onMoveToClicked(
        context: Id,
        targets: List<Id>
    ) {
        viewModelScope.launch {
            move(
                Move.Params(
                    context = context,
                    blockIds = targets,
                    position = Position.INNER,
                    targetId = pageId,
                    targetContext = pageId
                )
            ).proceed(
                failure = { Timber.e(it, "Error while moving blocks") },
                success = { navigate(EventWrapper(AppNavigation.Command.Exit)) }
            )
        }
    }
}