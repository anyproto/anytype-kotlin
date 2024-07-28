package com.anytypeio.anytype.feature_discussions.presentation

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.GlobalSearchItemView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DiscussionViewModel(
    private val params: DefaultParams,
    private val setObjectDetails: SetObjectDetails,
    private val openObject: OpenObject
) : BaseViewModel() {

    val name = MutableStateFlow<String?>(null)
    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())
    val attachments = MutableStateFlow<List<GlobalSearchItemView>>(emptyList())

    init {
        viewModelScope.launch {
            openObject.async(
                OpenObject.Params(
                    spaceId = params.space,
                    obj = params.ctx,
                    saveAsLastOpened = false
                )
            ).fold(
                onSuccess = { obj ->
                    val root = ObjectWrapper.Basic(obj.details[params.ctx].orEmpty())
                    Timber.d("DROID-2635 Opened object: $root")
                    name.value = root.name
                }
            )
        }
    }

    fun onMessageSent(msg: String) {
        Timber.d("DROID-2635 OnMessageSent: $msg")
        messages.value = buildList {
            add(
                DiscussionView.Message(
                    id = {size.inc()}.toString(),
                    author = "Me",
                    timestamp = System.currentTimeMillis(),
                    msg = msg,
                    attachments = attachments.value.map { a ->
                        DiscussionView.Message.Attachment(
                            item = a
                        )
                    }
                )
            )
            addAll(messages.value)
            attachments.value = emptyList()
        }
    }

    fun onTitleChanged(input: String) {
        Timber.d("DROID-2635 OnTitleChanged: $input")
        viewModelScope.launch {
            name.value = input
            setObjectDetails.async(
                params = SetObjectDetails.Params(
                    ctx = params.ctx,
                    details = mapOf(
                        Relations.NAME to input
                    )
                )
            )
        }
    }

    fun onAttachObject(obj: GlobalSearchItemView) {
        attachments.value = listOf(obj)
    }

    fun onClearAttachmentClicked() {
        attachments.value = emptyList()
    }
}