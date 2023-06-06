package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockPictureBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import timber.log.Timber

class Picture(val binding: ItemBlockPictureBinding) : Media(binding.root), DecoratableCardViewHolder {

    override val root: View = itemView
    override val container: View = root
    override val clickContainer: View = root
    private val image = binding.image
    private val error = binding.error

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val decoratableCard: View = binding.card

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    private val listener: RequestListener<Drawable> = object : RequestListener<Drawable> {

        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            error.visible()
            Timber.e(e, "Error while loading picture")
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            error.invisible()
            return false
        }
    }

    fun bind(item: BlockView.Media.Picture, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        Glide.with(image).load(item.url).listener(listener).into(image)
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
}