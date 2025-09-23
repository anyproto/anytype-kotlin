package com.anytypeio.anytype.ui.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.BuildConfig
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.media.MediaViewModel
import com.anytypeio.anytype.presentation.media.MediaViewModel.MediaViewState
import com.anytypeio.anytype.ui.media.screens.AudioPlayerBox
import com.anytypeio.anytype.ui.media.screens.ImageGalleryBox
import com.anytypeio.anytype.ui.media.screens.VideoPlayerBox
import java.util.ArrayList
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class MediaActivity : ComponentActivity() {

    @Inject
    lateinit var factory: MediaViewModel.Factory

    private val vm by viewModels<MediaViewModel> { factory }

    private val space get() = intent.getStringExtra(EXTRA_SPACE_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        super.onCreate(savedInstanceState)
        
        processIntentData()
        
        if (BuildConfig.DEBUG) {
            Timber.d("MediaActivity created")
        }

        proceedWithCommands()

        setContent {

            val viewState by vm.viewState.collectAsStateWithLifecycle()
            
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                when (val state = viewState) {
                    is MediaViewState.Loading -> {
                        // Show loading or nothing
                    }
                    is MediaViewState.Error -> {
                        Timber.e("Media error: ${state.message}")
                        finish()
                    }
                    is MediaViewState.VideoContent -> {
                        VideoPlayerBox(url = state.url)
                    }
                    is MediaViewState.ImageContent -> {
                        ImageGalleryBox(
                            images = state.images,
                            index = state.currentIndex,
                            onBackClick = { finish() },
                            onDownloadClick = { obj ->
                                val givenSpace = space
                                if (givenSpace != null) {
                                    vm.onDownloadObject(
                                        id = obj,
                                        space = SpaceId(space.orEmpty())
                                    )
                                } else {
                                    toast("Space not found")
                                }
                            },
                            onDeleteClick = vm::onDeleteObject
                        )
                    }
                    is MediaViewState.AudioContent -> {
                        AudioPlayerBox(
                            name = state.name,
                            url = state.url
                        )
                    }
                }
            }
        }
    }

    private fun proceedWithCommands() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.commands.collect { command ->
                        when (command) {
                            is MediaViewModel.Command.Dismiss -> {
                                finish()
                            }
                            is MediaViewModel.Command.ShowToast -> {
                                toast(command.message)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun processIntentData() {
        val objects = intent
            .getStringArrayListExtra(EXTRA_OBJECTS)
            ?.toList()
            .orEmpty()
        val name = intent.getStringExtra(EXTRA_MEDIA_NAME)
        val mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, TYPE_UNKNOWN)
        val index = intent.getIntExtra(EXTRA_IMAGE_INDEX, 0)
        
        when (mediaType) {
            TYPE_IMAGE -> vm.processImage(objects, index)
            TYPE_VIDEO -> vm.processVideo(objects.firstOrNull().orEmpty())
            TYPE_AUDIO -> vm.processAudio(objects.firstOrNull().orEmpty(), name.orEmpty())
            else -> {
                Timber.e("Invalid media type: $mediaType")
                finish()
            }
        }
    }

    private fun inject() {
        componentManager().mediaComponent.get().inject(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().mediaComponent.release()
    }

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_VIDEO = 2
        const val TYPE_AUDIO = 3
        private const val TYPE_UNKNOWN = 0

        private const val EXTRA_OBJECTS = "extra_object_ids"
        private const val EXTRA_SPACE_ID = "extra_space_id"
        private const val EXTRA_IMAGE_INDEX = "extra_image_index"
        private const val EXTRA_MEDIA_TYPE = "extra_media_type"
        private const val EXTRA_MEDIA_NAME = "extra_media_name"

        fun start(
            context: Context,
            obj: Id,
            space: Id,
            mediaType: Int,
            name: String? = null
        ) {
            val intent = Intent(context, MediaActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_OBJECTS, arrayListOf(obj))
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EXTRA_MEDIA_NAME, name)
                putExtra(EXTRA_SPACE_ID, space)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }

        /***
         *
         */
        fun start(
            context: Context,
            objects: List<Id>,
            space: Id,
            mediaType: Int,
            name: String? = null,
            index: Int = 0
        ) {
            val intent = Intent(context, MediaActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_OBJECTS, ArrayList(objects))
                putExtra(EXTRA_SPACE_ID, space)
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EXTRA_MEDIA_NAME, name)
                putExtra(EXTRA_IMAGE_INDEX, index)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
} 