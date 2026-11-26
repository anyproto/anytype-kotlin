package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.view.View
import android.widget.FrameLayout
import coil3.load
import com.anytypeio.anytype.core_ui.databinding.ItemBlockPictureBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
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

    private var pendingRetry: Runnable? = null

    fun bind(item: BlockView.Media.Picture, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        pendingRetry?.let { image.removeCallbacks(it) }
        pendingRetry = null
        loading.visible()
        error.invisible()
        load(url = item.url, attempt = 1)
    }

    /**
     * The middleware may not serve a file right after it is uploaded,
     * so a failed load is retried a few times before the error is shown.
     */
    private fun load(url: String, attempt: Int) {
        image.load(url) {
            listener(
                onSuccess = { _, _ ->
                    loading.gone()
                    error.invisible()
                },
                onError = { _, result ->
                    Timber.w(
                        result.throwable,
                        "Error while loading picture with url: $url, attempt: $attempt"
                    )
                    if (attempt < MAX_LOAD_ATTEMPTS) {
                        val retry = Runnable {
                            pendingRetry = null
                            load(url = url, attempt = attempt + 1)
                        }
                        pendingRetry = retry
                        image.postDelayed(retry, RETRY_DELAY_IN_MILLIS)
                    } else {
                        loading.gone()
                        error.visible()
                    }
                }
            )
        }
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(
            ListenerType.Picture.View(
                target = item.id,
                obj = item.targetObjectId
            )
        )
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
        private const val MAX_LOAD_ATTEMPTS = 3
        private const val RETRY_DELAY_IN_MILLIS = 1000L
    }
}