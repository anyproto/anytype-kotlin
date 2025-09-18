package com.anytypeio.anytype.core_ui.features.editor.holders.media

import android.view.View
import android.widget.FrameLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import coil3.ImageLoader
import coil3.load
import coil3.request.crossfade
import coil3.video.VideoFrameDecoder
import coil3.video.videoFrameMillis
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockVideoBinding
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util

class Video(val binding: ItemBlockVideoBinding) : Media(binding.root) {

    override val root: View = itemView
    override val container: View = root
    override val clickContainer: View = binding.videoContainer

    init {
        clickContainer.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.Media.Video,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item, clicked)
        setupPreview(
            onPlayClicked = {
                clicked(ListenerType.Video.View(obj = item.targetObjectId, target = item.id, url = item.url))
            },
            url = item.url
        )
    }

    private fun setupPreview(
        onPlayClicked: () -> Unit,
        url: String?
    ) {
        with(binding) {

            progress.visible()
            playButton.setOnClickListener {
                onPlayClicked()
            }

            if (!url.isNullOrEmpty()) {
                val imageLoader = ImageLoader.Builder(itemView.context)
                    .components {
                        add(VideoFrameDecoder.Factory())
                    }
                    .build()

                videoThumbnail.load(url, imageLoader) {
                    crossfade(true)
                    videoFrameMillis(1000L)
                    listener(
                        onStart = {
                            progress.visible()
                        },
                        onSuccess = { _, _ ->
                            progress.gone()
                            playButton.visible()
                        },
                        onError = { _, _ ->
                            progress.gone()
                            playButton.visible()
                        }
                    )
                }
            }
        }
    }

    override fun onMediaBlockClicked(item: BlockView.Media, clicked: (ListenerType) -> Unit) {
        if (item is BlockView.Media.Video) {
            clicked(ListenerType.Video.View(obj = item.targetObjectId, target = item.id, url = item.url))
        }
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