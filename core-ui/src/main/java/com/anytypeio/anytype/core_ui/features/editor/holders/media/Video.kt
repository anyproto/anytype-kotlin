package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockVideoBinding
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class Video(val binding: ItemBlockVideoBinding) : Media(binding.root) {

    override val root: View = itemView

    override val clickContainer: View =
        binding.playerView.findViewById<FrameLayout>(R.id.exo_controller)

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.Media.Video, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        binding.playerView.visibility = View.VISIBLE
        val player = SimpleExoPlayer.Builder(itemView.context).build()
        val source = DefaultDataSourceFactory(
            itemView.context,
            Util.getUserAgent(itemView.context, BuildConfig.LIBRARY_PACKAGE_NAME)
        )
        val mediaSource =
            ProgressiveMediaSource.Factory(source).createMediaSource(Uri.parse(item.url))
        player.playWhenReady = false
        player.seekTo(0)
        player.prepare(mediaSource, false, false)
        binding.playerView.player = player
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Video.View(item.id))
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentize(
            indent = item.indent,
            defIndent = dimen(R.dimen.indent),
            margin = dimen(R.dimen.video_default_margin_start)
        )
    }

    override fun select(isSelected: Boolean) {
        itemView.isSelected = isSelected
    }
}