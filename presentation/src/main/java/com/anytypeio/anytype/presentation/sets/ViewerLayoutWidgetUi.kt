package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.presentation.relations.isSystemKey

data class ViewerLayoutWidgetUi(
    val viewer: Id?,
    val showWidget: Boolean,
    val layoutType: DVViewerType,
    val withIcon: State.Toggle.WithIcon,
    val fitImage: State.Toggle.FitImage,
    val cardSize: State.CardSize,
    val imagePreviewItems: List<State.ImagePreview>,
    val showCardSize: Boolean,
    val showCoverMenu: Boolean
) {

    fun dismiss() = copy(showWidget = false)

    fun empty() = this.copy(
        viewer = null,
        layoutType = DVViewerType.GRID,
        withIcon = State.Toggle.WithIcon(toggled = false),
        fitImage = State.Toggle.FitImage(toggled = false),
        cardSize = State.CardSize.Small,
        showCardSize = false,
        imagePreviewItems = emptyList()
    )

    companion object {
        fun init() = ViewerLayoutWidgetUi(
            viewer = null,
            showWidget = false,
            layoutType = DVViewerType.GRID,
            withIcon = State.Toggle.WithIcon(toggled = false),
            fitImage = State.Toggle.FitImage(toggled = false),
            cardSize = State.CardSize.Small,
            showCardSize = false,
            showCoverMenu = false,
            imagePreviewItems = emptyList()
        )
    }

    sealed class State {
        sealed class CardSize : State() {
            data object Small : CardSize()
            data object Large : CardSize()
        }

        sealed class Toggle : State() {
            abstract val toggled: Boolean

            data class FitImage(override val toggled: Boolean) : Toggle()
            data class WithIcon(override val toggled: Boolean) : Toggle()
        }

        sealed class ImagePreview : State() {
            abstract val relationKey: RelationKey
            abstract val isChecked: Boolean

            data class None(
                override val relationKey: RelationKey = RelationKey("None"),
                override val isChecked: Boolean
            ) : ImagePreview()

            data class PageCover(
                override val relationKey: RelationKey = RelationKey(Relations.PAGE_COVER),
                override val isChecked: Boolean
            ) : ImagePreview()

            data class Custom(
                override val relationKey: RelationKey,
                override val isChecked: Boolean,
                val name: String
            ) : ImagePreview()
        }
    }

    sealed class Action {
        data object Dismiss : Action()
        data object CardSizeMenu : Action()
        data object CoverMenu : Action()
        data object DismissCoverMenu : Action()
        data class Icon(val toggled: Boolean) : Action()
        data class FitImage(val toggled: Boolean) : Action()
        data class CardSize(val cardSize: State.CardSize) : Action()
        data class ImagePreviewUpdate(val item: State.ImagePreview) : Action()
        data class Type(val type: DVViewerType) : Action()
    }
}

suspend fun ViewerLayoutWidgetUi.updateState(
    viewer: DVViewer,
    storeOfRelations: StoreOfRelations,
    relationLinks: List<RelationLink>
): ViewerLayoutWidgetUi {
    val cardSize = when (viewer.cardSize) {
        Block.Content.DataView.Viewer.Size.SMALL -> ViewerLayoutWidgetUi.State.CardSize.Small
        Block.Content.DataView.Viewer.Size.MEDIUM -> ViewerLayoutWidgetUi.State.CardSize.Small
        Block.Content.DataView.Viewer.Size.LARGE -> ViewerLayoutWidgetUi.State.CardSize.Large
    }
    return this.copy(
        viewer = viewer.id,
        layoutType = viewer.type,
        withIcon = ViewerLayoutWidgetUi.State.Toggle.WithIcon(!viewer.hideIcon),
        fitImage = ViewerLayoutWidgetUi.State.Toggle.FitImage(viewer.coverFit),
        cardSize = cardSize,
        showCardSize = showCardSize,
        imagePreviewItems = viewer.getImagePreviewItems(
            storeOfRelations = storeOfRelations,
            relationLinks = relationLinks
        ),
    )
}

private suspend fun DVViewer.getImagePreviewItems(
    storeOfRelations: StoreOfRelations,
    relationLinks: List<RelationLink>
): List<ViewerLayoutWidgetUi.State.ImagePreview> {
    val selectedCoverRelationKey = coverRelationKey

    val validFileRelations = getValidFileRelations(storeOfRelations, relationLinks)

    return buildList {
        addNoneImagePreview(selectedCoverRelationKey)
        addPageCoverImagePreview(selectedCoverRelationKey)
        addCustomImagePreviews(validFileRelations, selectedCoverRelationKey)
    }
}

private suspend fun getValidFileRelations(
    storeOfRelations: StoreOfRelations,
    relationLinks: List<RelationLink>
): List<ObjectWrapper.Relation> {
    return relationLinks
        .filter { it.format == RelationFormat.FILE }
        .mapNotNull { storeOfRelations.getByKey(it.key) }
        .filter { relation ->
            relation.isValid && relation.isHidden != true && relation.isArchived != true &&
                    !relation.isReadonlyValue && !relation.key.isSystemKey()
        }
}

private fun MutableList<ViewerLayoutWidgetUi.State.ImagePreview>.addNoneImagePreview(
    selectedCoverRelationKey: String?
) {
    add(
        ViewerLayoutWidgetUi.State.ImagePreview.None(
            isChecked = selectedCoverRelationKey.isNullOrBlank()
        )
    )
}

private fun MutableList<ViewerLayoutWidgetUi.State.ImagePreview>.addPageCoverImagePreview(
    selectedCoverRelationKey: String?
) {
    add(
        ViewerLayoutWidgetUi.State.ImagePreview.PageCover(
            isChecked = selectedCoverRelationKey == Relations.PAGE_COVER
        )
    )
}

private fun MutableList<ViewerLayoutWidgetUi.State.ImagePreview>.addCustomImagePreviews(
    validFileRelations: List<ObjectWrapper.Relation>,
    selectedCoverRelationKey: String?
) {
    validFileRelations.forEach { fileRelation ->
        add(
            ViewerLayoutWidgetUi.State.ImagePreview.Custom(
                relationKey = RelationKey(fileRelation.key),
                isChecked = selectedCoverRelationKey == fileRelation.key,
                name = fileRelation.name.orEmpty()
            )
        )
    }
}