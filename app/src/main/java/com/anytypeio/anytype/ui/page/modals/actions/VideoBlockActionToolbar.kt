package com.anytypeio.anytype.ui.page.modals.actions

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.features.page.BlockView
import com.anytypeio.anytype.core_ui.features.page.BlockViewHolder
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class VideoBlockActionToolbar : BlockActionToolbar() {

    companion object {
        const val SEEK_PREVIEW_POSITION = 1000L
    }

    lateinit var block: BlockView.Media.Video

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        block = arguments?.getParcelable(ARG_BLOCK)!!
    }

    override fun blockLayout() =
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_VIDEO -> R.layout.item_block_video_playback_off
            else -> R.layout.item_block_video_uploading_preview
        }

    override fun getBlock(): BlockView = block

    override fun initUi(view: View, colorView: ImageView?, backgroundView: ImageView?) {
        when (block.getViewType()) {
            BlockViewHolder.HOLDER_VIDEO -> initVideo(view)
            else -> Unit
        }
    }

    private fun initVideo(view: View) {
        val item = block
        view.findViewById<PlayerView>(R.id.playerView).apply {
            val player = SimpleExoPlayer.Builder(context).build()
            val source = DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, BuildConfig.LIBRARY_PACKAGE_NAME)
            )
            val mediaSource =
                ProgressiveMediaSource.Factory(source).createMediaSource(Uri.parse(item.url))
            player.playWhenReady = false
            player.seekTo(SEEK_PREVIEW_POSITION)
            player.prepare(mediaSource, false, false)
            this.player = player
        }
        setConstraints()
    }
}