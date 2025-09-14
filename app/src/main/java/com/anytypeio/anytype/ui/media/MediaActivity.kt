package com.anytypeio.anytype.ui.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.media.screens.AudioPlayerBox
import com.anytypeio.anytype.ui.media.screens.ImageGalleryBox
import com.anytypeio.anytype.ui.media.screens.VideoPlayerBox
import java.util.ArrayList
import timber.log.Timber

class MediaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val urls = intent
            .getStringArrayListExtra(EXTRA_URL)
            ?.toList()
            .orEmpty()
        val name = intent.getStringExtra(EXTRA_MEDIA_NAME)
        val mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, TYPE_UNKNOWN)
        
        if (urls.isEmpty() || mediaType == TYPE_UNKNOWN) {
            Timber.e("Invalid intent, urls: $urls")
            finish()
            return
        }

        if (BuildConfig.DEBUG) {
            Timber.d("Media player for urls: $urls")
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                when(mediaType) {
                    TYPE_VIDEO -> VideoPlayerBox(url = urls.first())
                    TYPE_IMAGE -> ImageGalleryBox(
                        urls = urls.toList(),
                        onBackClick = {
                            finish()
                        },
                        onOpenClick = {
                            // TODO
                        },
                        onDownloadClick = {
                            // TODO
                        },
                        onDeleteClick = {
                            // TODO
                        }
                    )
                    TYPE_AUDIO -> {
                        AudioPlayerBox(
                            name = name.orEmpty(),
                            url = urls.first()
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_AUDIO = 3
        private const val TYPE_UNKNOWN = 0

        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_MEDIA_TYPE = "extra_media_type"
        private const val EXTRA_MEDIA_NAME = "extra_media_name"

        fun start(
            context: Context,
            url: String,
            mediaType: Int,
            name: String? = null
        ) {
            val intent = Intent(context, MediaActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_URL, arrayListOf(url))
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EXTRA_MEDIA_NAME, name)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }

        fun start(
            context: Context,
            urls: List<String>,
            mediaType: Int,
            name: String? = null
        ) {
            val intent = Intent(context, MediaActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_URL, ArrayList(urls))
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EXTRA_MEDIA_NAME, name)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
} 