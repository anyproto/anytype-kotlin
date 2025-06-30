package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockPictureBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import coil3.imageLoader
import coil3.load
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import timber.log.Timber

class Picture(val binding: ItemBlockPictureBinding) : Media(binding.root), DecoratableCardViewHolder {

    override val root: View = itemView
    override val container: View = root
    override val clickContainer: View = root
    private val image = binding.image
    private val error = binding.error
    private val loading = binding.progress

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val decoratableCard: View = binding.card

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    private fun createImageRequest(url: String): ImageRequest {
        return ImageRequest.Builder(image.context)
            .data(url)
            .target(image)
            .listener(
                onStart = { loading.visible() },
                onSuccess = { _, _ ->
                    loading.gone()
                    error.invisible()
                },
                onError = { _, result ->
                    loading.gone()
                    error.visible()
                    Timber.w(result.throwable, "Error while loading picture with url: $url")
                }
            )
            .build()
    }

    fun bind(item: BlockView.Media.Picture, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        val request = createImageRequest(item.url)
        image.context.imageLoader.enqueue(request)
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Picture.View(item.id))
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun select(isSelected: Boolean) {
        binding.selected.isSelected = isSelected
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        binding.selected.applySelectorOffset<FrameLayout.LayoutParams>(
            content = binding.card,
            res = itemView.resources
        )
    }

    companion object {
        const val LOADING_TIMEOUT_IN_MILLIS = 30000
    }
}