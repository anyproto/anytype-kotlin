package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.objects.StoreOfRelations

data class ViewerLayoutWidgetUi(
    val showWidget: Boolean,
    val layoutType: DVViewerType,
    val withIcon: State.Toggle.HideIcon,
    val fitImage: State.Toggle.FitImage,
    val cardSize: State.CardSize,
    val cover: State.ImagePreview,
    val showCardSize: Boolean
) {

    fun dismiss() = copy(showWidget = false)

    companion object {
        fun init() = ViewerLayoutWidgetUi(
            showWidget = false,
            layoutType = DVViewerType.GRID,
            withIcon = State.Toggle.HideIcon(toggled = false),
            fitImage = State.Toggle.FitImage(toggled = false),
            cardSize = State.CardSize.Small,
            cover = State.ImagePreview.None,
            showCardSize = false
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
        object CardSizeMenu : Action()
        data class Icon(val toggled: Boolean) : Action()
        data class FitImage(val toggled: Boolean) : Action()
        data class CardSize(val cardSize: State.CardSize) : Action()
        data class Cover(val cover: State.ImagePreview) : Action()
    }
}

suspend fun ViewerLayoutWidgetUi.updateState(
    dvViewer: DVViewer,
    storeOfRelations: StoreOfRelations
): ViewerLayoutWidgetUi {
    val cardSize = when (dvViewer.cardSize) {
        Block.Content.DataView.Viewer.Size.SMALL -> ViewerLayoutWidgetUi.State.CardSize.Small
        Block.Content.DataView.Viewer.Size.MEDIUM -> ViewerLayoutWidgetUi.State.CardSize.Small
        Block.Content.DataView.Viewer.Size.LARGE -> ViewerLayoutWidgetUi.State.CardSize.Large
    }
    val coverRelationKey = dvViewer.coverRelationKey
    val cover = when {
        coverRelationKey.isNullOrBlank() -> ViewerLayoutWidgetUi.State.ImagePreview.None
        coverRelationKey == Relations.PAGE_COVER -> ViewerLayoutWidgetUi.State.ImagePreview.Cover
        else -> {
            val preview = storeOfRelations.getByKey(coverRelationKey)
            if (preview != null) {
                ViewerLayoutWidgetUi.State.ImagePreview.Custom(preview.name.orEmpty())
            } else {
                ViewerLayoutWidgetUi.State.ImagePreview.None
            }
        }
    }
    return this.copy(
        layoutType = dvViewer.type,
        withIcon = ViewerLayoutWidgetUi.State.Toggle.HideIcon(dvViewer.hideIcon),
        fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(dvViewer.coverFit),
        cardSize = cardSize,
        cover = cover,
        showCardSize = showCardSize
    )
}
