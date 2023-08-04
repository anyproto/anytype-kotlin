package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView

sealed class TemplateView {

    object Blank : TemplateView()

    data class NoIcon(val title: String) : TemplateView()

    data class Icon(val title: String, val icon: String) : TemplateView()

    data class Image(val title: String, val image: String) : TemplateView()

    data class Cover(val title: String, val cover: String) : TemplateView()

    data class CoverWithIcon(val title: String, val cover: String, val icon: String) : TemplateView()

    data class Profile(val title: String, val cover: String, val icon: String) : TemplateView()

    data class Task(val title: String, val icon: String) : TemplateView()
}

class TemplatesWidgetUiState(
    val items: List<TemplateView>,
    val showWidget: Boolean
) {
    companion object {
        fun empty() = TemplatesWidgetUiState(
            items = emptyList(),
            showWidget = false
        )
    }
}
