package com.anytypeio.anytype.ui.media

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.ui.media.screens.AudioPlayerBox
import com.anytypeio.anytype.ui.media.screens.ImageBox
import com.anytypeio.anytype.ui.media.screens.VideoPlayerBox
import timber.log.Timber

class MediaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra(EXTRA_URL)
        val name = intent.getStringExtra(EXTRA_MEDIA_NAME)
        val mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, TYPE_UNKNOWN)
        
        if (url == null || mediaType == TYPE_UNKNOWN) {
            finish()
            return
        }

        if (BuildConfig.DEBUG) {
            Timber.d("Media player for url: $url")
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                when(mediaType) {
                    TYPE_VIDEO -> VideoPlayerBox(url = url)
                    TYPE_IMAGE -> ImageBox(url = url)
                    TYPE_AUDIO -> {
                        AudioPlayerBox(
                            name = name.orEmpty(),
                            url = url
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
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EXTRA_MEDIA_NAME, name)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
} 