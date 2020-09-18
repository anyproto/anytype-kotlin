package com.agileburo.anytype.presentation.moving

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.timber
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.block.interactor.Move
import com.agileburo.anytype.domain.block.model.Position
import com.agileburo.anytype.domain.common.Id
import com.agileburo.anytype.domain.config.GetConfig
import com.agileburo.anytype.domain.misc.UrlBuilder
import com.agileburo.anytype.domain.page.navigation.GetPageInfoWithLinks
import com.agileburo.anytype.presentation.mapper.toEmojiView
import com.agileburo.anytype.presentation.mapper.toImageView
import com.agileburo.anytype.presentation.mapper.toView
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.PageNavigationView
import com.agileburo.anytype.presentation.navigation.SupportNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class MoveToViewModel(
    private val urlBuilder: UrlBuilder,
    private val getPageInfoWithLinks: GetPageInfoWithLinks,
    private val getConfig: GetConfig,
    private val move: Move
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
            getPageInfoWithLinks.invoke(GetPageInfoWithLinks.Params(pageId = target)).proceed(
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
                                    title = documentInfo.fields.name.orEmpty(),
                                    subtitle = documentInfo.snippet.orEmpty(),
                                    image = documentInfo.fields.toImageView(urlBuilder),
                                    emoji = documentInfo.fields.toEmojiView(),
                                    inbound = links.inbound.map { it.toView(urlBuilder) },
                                    outbound = links.outbound.map { it.toView(urlBuilder) }
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
        context: Id
    ) {
        isMovingDisabled.value = (target == context || target == home)
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