package com.anytypeio.anytype.presentation.page

import androidx.lifecycle.*
import com.anytypeio.anytype.domain.page.CheckForUnlink
import timber.log.Timber

sealed class LinkViewState {

    data class Init(val text: String, val url: String?) : LinkViewState()
    data class AddLink(val link: String, val range: IntRange) : LinkViewState()
    data class Unlink(val range: IntRange) : LinkViewState()
}

class LinkAddViewModel(
    private val checkForUnlink: CheckForUnlink
) : ViewModel() {

    lateinit var range: IntRange
    private var initUrl: String? = null
    private val stateData = MutableLiveData<LinkViewState>()
    val state: LiveData<LinkViewState> = stateData

    fun onViewCreated(initUrl: String?, text: String, range: IntRange) {
        this.range = range
        this.initUrl = initUrl
        stateData.value = LinkViewState.Init(
            text = text,
            url = initUrl
        )
    }

    fun onLinkButtonClicked(text: String) {
        if (text.isNotEmpty()) {
            stateData.value = LinkViewState.AddLink(link = text, range = range)
        }
    }

    fun onUnlinkButtonClicked() =
        checkForUnlink.invoke(viewModelScope, CheckForUnlink.Params(link = initUrl)) { result ->
            result.either(
                fnL = { Timber.e("Can't proceed to unlink:${it.message}") },
                fnR = { stateData.postValue(LinkViewState.Unlink(range = range)) }
            )
        }
}

class LinkAddViewModelFactory(
    private val unlink: CheckForUnlink
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return LinkAddViewModel(unlink) as T
    }
}