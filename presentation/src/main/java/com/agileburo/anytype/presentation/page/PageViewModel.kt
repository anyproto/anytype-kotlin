package com.agileburo.anytype.presentation.page

import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.page.ObservePage
import com.agileburo.anytype.domain.page.OpenPage
import com.agileburo.anytype.presentation.mapper.toView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PageViewModel(
    private val openPage: OpenPage,
    private val observePage: ObservePage
) : ViewStateViewModel<PageViewModel.ViewState>() {

    init {
        startObservingPage()
        proceedWithOpeningPage()
    }

    private fun startObservingPage() {
        viewModelScope.launch {
            observePage
                .build()
                .map { blocks -> blocks.map { it.toView() } }
                .collect {
                    Timber.d("Received blocks: $it")
                    stateData.postValue(ViewState.Success(it + getMocks()))
                }
        }
    }

    private fun proceedWithOpeningPage() {
        stateData.postValue(ViewState.Loading)

        openPage.invoke(viewModelScope, OpenPage.Params.reference()) { result ->
            result.either(
                fnR = { Timber.d("Page has been opened") },
                fnL = { e -> Timber.e(e, "Error while openining the test page") }
            )
        }
    }

    sealed class ViewState {
        object Loading : ViewState()
        data class Success(val blocks: List<BlockView>) : ViewState()
        data class Error(val message: String) : ViewState()
    }

    private fun getMocks(): List<BlockView> {

        val placeholder =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat."

        return mutableListOf(
            BlockView.Text(
                id = "2",
                text = placeholder
            ),
            BlockView.HeaderOne(
                id = "3",
                text = "Header One"
            ),
            BlockView.Text(
                id = "4",
                text = placeholder
            ),
            BlockView.HeaderTwo(
                id = "5",
                text = "Header Two"
            ),
            BlockView.Bulleted(
                id = "6",
                text = "Первый",
                indent = 0
            ),
            BlockView.Bulleted(
                id = "7",
                text = "Второй",
                indent = 0
            ),
            BlockView.Bulleted(
                id = "8",
                text = "Третий",
                indent = 0
            ),
            BlockView.HeaderThree(
                id = "9",
                text = "Header Three"
            ),
            BlockView.Checkbox(
                id = "10",
                text = "Checkbox 1"
            ),
            BlockView.Checkbox(
                id = "11",
                text = "Checkbox 2",
                checked = true
            ),
            BlockView.Checkbox(
                id = "12",
                text = "Checkbox 3",
                checked = true
            ),
            BlockView.HeaderThree(
                id = "13",
                text = "Header Three"
            ),
            BlockView.Numbered(
                id = "14",
                text = "Numbered 1",
                number = "1",
                indent = 0
            ),
            BlockView.Numbered(
                id = "15",
                text = "Numbered 2",
                number = "1",
                indent = 0
            ),
            BlockView.Numbered(
                id = "16",
                text = "Numbered 3",
                number = "1",
                indent = 0
            ),
            BlockView.Contact(
                id = "17",
                avatar = "",
                name = "Konstantin Ivanov"
            ),
            BlockView.Task(
                id = "18",
                checked = true,
                text = "Task 1"
            ),
            BlockView.Task(
                id = "19",
                checked = false,
                text = "Task 2"
            ),
            BlockView.Task(
                id = "20",
                checked = true,
                text = "Task 3"
            ),
            BlockView.Page(
                id = "21",
                text = "Partnership terms",
                isArchived = false,
                isEmpty = true,
                emoji = null
            ),
            BlockView.Page(
                id = "22",
                text = "Partnership terms",
                isArchived = false,
                isEmpty = false,
                emoji = null
            ),
            BlockView.File(
                id = "23",
                filename = "Berlin.pdf",
                size = "2.1 MB"
            ),
            BlockView.Toggle(
                id = "24",
                toggled = false,
                text = "First toggle",
                indent = 0
            ),
            BlockView.Toggle(
                id = "25",
                toggled = true,
                text = "Second toggle",
                indent = 0
            ),
            BlockView.Highlight(
                id = "26",
                text = placeholder
            )
        )
    }
}

