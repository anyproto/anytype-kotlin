package com.agileburo.anytype.feature_desktop.mvvm

import androidx.lifecycle.ViewModel
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class DesktopViewModel : ViewModel() {

    private val docs by lazy {
        BehaviorRelay.createDefault<List<DesktopView>>(listOf(DesktopView.NewDocument))
    }

    fun observeDesktop() : Observable<List<DesktopView>> = docs

    fun onAddNewDocumentClicked() {
        docs.value?.let { items ->

            when(items.size) {
                1 -> {
                    val doc = DesktopView.Document(
                        id = "1",
                        title = "Document"
                    )
                    val result = mutableListOf<DesktopView>().apply {
                        add(doc)
                        add(items.last())
                    }

                    docs.accept(result)
                }
                else -> {
                    val result = mutableListOf<DesktopView>().apply {

                        val documents = items.filter { it !is DesktopView.NewDocument }

                        addAll(documents)

                        val new = DesktopView.Document(
                            id = ((documents.last() as DesktopView.Document).id.toInt() + 1).toString(),
                            title = "Document"
                        )

                        add(new)

                        add(DesktopView.NewDocument)
                    }

                    docs.accept(result)
                }
            }
        }
    }

    fun onDocumentClicked() {}

    override fun onCleared() {
        super.onCleared()
        // TODO
    }
}