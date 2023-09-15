package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.DVViewerType

data class ViewerLayoutWidgetUi(
    val showWidget: Boolean,
    val layoutType: DVViewerType,
    val withIcon: State.Toggle.HideIcon,
    val fitImage: State.Toggle.FitImage,
    val cardSize: State.CardSize,
    val cover: State.ImagePreview
) {

    fun dismiss() = copy(showWidget = false)

    companion object {
        fun init() = ViewerLayoutWidgetUi(
            showWidget = false,
            layoutType = DVViewerType.GRID,
            withIcon = State.Toggle.HideIcon(toggled = false),
            fitImage = State.Toggle.FitImage(toggled = false),
            cardSize = State.CardSize.Small,
            cover = State.ImagePreview.None
        )
    }

    sealed class State {
        sealed class CardSize : State() {
            object Small : CardSize()
            object Large : CardSize()
        }

        sealed class Toggle : State() {
            abstract val toggled: Boolean

            data class FitImage(override val toggled: Boolean) : Toggle()
            data class HideIcon(override val toggled: Boolean) : Toggle()
        }

        sealed class ImagePreview : State() {
            object None : ImagePreview()
            object Cover : ImagePreview()
            data class Custom(val name: String) : ImagePreview()
        }
    }

    sealed class Action {
        object Dismiss : Action()
        data class Icon(val toggled: Boolean) : Action()
        data class FitImage(val toggled: Boolean) : Action()
    }
}
