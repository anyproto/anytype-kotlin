package com.agileburo.anytype.feature_desktop.mvvm

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class DesktopViewModel : ViewModel() {

    private val docs by lazy {
        BehaviorRelay.createDefault<List<DesktopView>>(
            listOf(
                DesktopView.Document(
                    id = "1",
                    title = "Document"
                )
            )
        )
    }

    fun observeDesktop() : Observable<List<DesktopView>> = docs

    fun onAddNewDocumentClicked() {
        docs.value?.let { items ->

            when(items.size) {
                1 -> {
                    val doc = DesktopView.Document(
                        id = "2",
                        title = "Document"
                    )
                    val result = mutableListOf<DesktopView>().apply {
                        addAll(items)
                        add(doc)
                    }

                    docs.accept(result)
                }
                else -> {
                    val result = mutableListOf<DesktopView>().apply {

                        val new = DesktopView.Document(
                            id = ((items.last() as DesktopView.Document).id.toInt() + 1).toString(),
                            title = "Document"
                        )

                        addAll(items)
                        add(new)
                    }

                    docs.accept(result)
                }
            }
        }
    }

    fun onDocumentClicked() {}

    fun onProfileClicked() {

    }

}