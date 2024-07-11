package com.anytypeio.anytype.feature_discussions.presentation

import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class DiscussionViewModel(
    private val params: BaseViewModel.DefaultParams
) : BaseViewModel() {

    val messages = MutableStateFlow<List<DiscussionView.Message>>(emptyList())

    fun onMessageSent(
        msg: String
    ) {
        messages.value = buildList {
            add(
                DiscussionView.Message(
                    id = {size.inc()}.toString(),
                    author = "Me",
                    timestamp = System.currentTimeMillis(),
                    msg = msg
                )
            )
            addAll(messages.value)
        }
    }

}