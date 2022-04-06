package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockVideoBinding
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class Video(val binding: ItemBlockVideoBinding) : Media(binding.root), LifecycleEventObserver {

    override val root: View = itemView

    override val clickContainer: View =
        binding.playerView.findViewById<FrameLayout>(R.id.exo_controller)

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(item: BlockView.Media.Video, clicked: (ListenerType) -> Unit, lifecycle: Lifecycle) {
        super.bind(item, clicked)
        lifecycle.addObserver(this)
        binding.playerView.visibility = View.VISIBLE

        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
            ).build()

        val player = ExoPlayer.Builder(itemView.context)
            .setLoadControl(loadControl)
            .build()

        val source = DefaultDataSource.Factory(
            itemView.context,
            DefaultHttpDataSource.Factory().setUserAgent(
                Util.getUserAgent(itemView.context, BuildConfig.LIBRARY_PACKAGE_NAME)
            )
        )

        val mediaSource =
            ProgressiveMediaSource.Factory(source).createMediaSource(MediaItem.fromUri(item.url))

        player.playWhenReady = false
        player.seekTo(0)
        player.setMediaSource(mediaSource)
        player.prepare()
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

    fun pause() {
        binding.playerView.player?.pause()
    }

    fun recycle() {
        binding.playerView.player?.release()
        binding.playerView.player = null
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_PAUSE) {
            pause()
        }
    }
}