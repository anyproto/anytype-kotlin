package com.anytypeio.anytype.ui.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.anytypeio.anytype.ui.media.screens.MediaScreen

class MediaActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val url = intent.getStringExtra(EXTRA_URL)
        val mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, TYPE_UNKNOWN)
        
        if (url == null || mediaType == TYPE_UNKNOWN) {
            finish()
            return
        }

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                MediaScreen(
                    url = url,
                    mediaType = mediaType,
                    onClose = { finish() }
                )
            }
        }
    }

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        private const val TYPE_UNKNOWN = 0

        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_MEDIA_TYPE = "extra_media_type"

        fun start(context: Context, url: String, mediaType: Int) {
            val intent = Intent(context, MediaActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
            }
            context.startActivity(intent)
        }
    }
} 