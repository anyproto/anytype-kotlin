package com.anytypeio.anytype.ui.sharing

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.sharing.SharingScreen
import com.anytypeio.anytype.core_utils.ext.argOrNull
import com.anytypeio.anytype.core_utils.ext.parseActionSendMultipleUris
import com.anytypeio.anytype.core_utils.ext.parseActionSendUri
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.sharing.SharedContent
import com.anytypeio.anytype.presentation.sharing.SharingCommand
import com.anytypeio.anytype.presentation.sharing.SharingViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import javax.inject.Inject
import timber.log.Timber

/**
 * SharingFragment is the single entry point for all share intents.
 * It handles all MIME types: text, images, videos, audio, PDF, and generic files.
 *
 * ## Usage
 * ```kotlin
 * SharingFragment.newInstance(intent).show(supportFragmentManager, "share")
 * ```
 */
class SharingFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SharingViewModel.Factory

    private val vm by viewModels<SharingViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MaterialTheme {
            // Handle back press
            BackHandler {
                if (!vm.onBackPressed()) {
                    dismiss()
                }
            }

            SharingScreen(
                state = vm.screenState.collectAsStateWithLifecycle().value,
                onSpaceSelected = vm::onSpaceSelected,
                onSearchQueryChanged = vm::onSearchQueryChanged,
                onCommentChanged = vm::onCommentChanged,
                onSendClicked = vm::onSendClicked,
                onObjectSelected = vm::onObjectSelected,
                onBackPressed = {
                    if (!vm.onBackPressed()) {
                        dismiss()
                    }
                },
                onCancelClicked = { dismiss() },
                onRetryClicked = vm::onSendClicked
            )

            // Handle commands
            LaunchedEffect(Unit) {
                vm.commands.collect { command ->
                    proceedWithCommand(command)
                }
            }

            // Handle toasts
            LaunchedEffect(Unit) {
                vm.toasts.collect { toast ->
                    toast(toast)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        expand()
    }

    override fun onStart() {
        super.onStart()
        val sharedContent = parseSharedContent()
        vm.onSharedDataReceived(sharedContent)
    }

    /**
     * Parses the shared content from the fragment arguments.
     * Supports both new Intent-based entry point and legacy factory methods.
     */
    private fun parseSharedContent(): SharedContent {
        val args = requireArguments()

        // New single entry point: Intent passed directly
        val intent: Intent? = argOrNull(SHARING_INTENT_KEY)
        if (intent != null) {
            return convertIntentToSharedContent(intent)
        }

        // Legacy support: individual keys for backward compatibility
        return parseLegacySharedContent(args)
    }

    /**
     * Single entry point for converting Android Intent to SharedContent.
     * Handles all MIME types: text, images, videos, audio, PDF, and files.
     */
    private fun convertIntentToSharedContent(intent: Intent): SharedContent {
        val mimeType = intent.type

        Timber.d("Converting intent to SharedContent. MIME type: $mimeType, action: ${intent.action}")

        return when {
            // No MIME type - try to extract text
            mimeType == null -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (URLUtil.isValidUrl(text)) {
                    SharedContent.Url(text)
                } else {
                    SharedContent.Text(text)
                }
            }

            // Text content (plain text, URLs)
            mimeType == MIME_TEXT_PLAIN -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                if (URLUtil.isValidUrl(text)) {
                    SharedContent.Url(text)
                } else {
                    SharedContent.Text(text)
                }
            }

            // Images
            mimeType.startsWith(MIME_IMAGE_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.IMAGE)
            }

            // Videos
            mimeType.startsWith(MIME_VIDEO_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.VIDEO)
            }

            // Audio (music, voice memos, podcasts)
            mimeType.startsWith(MIME_AUDIO_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.AUDIO)
            }

            // PDF specifically
            mimeType == MIME_PDF -> {
                parseMediaIntent(intent, SharedContent.MediaType.PDF)
            }

            // Other application files (zip, doc, etc.)
            mimeType.startsWith(MIME_APPLICATION_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }

            // Other text types (html, csv, xml) - treat as file
            mimeType.startsWith(MIME_TEXT_PREFIX) -> {
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }

            // Fallback for unknown types
            else -> {
                Timber.w("Unknown MIME type: $mimeType, treating as generic file")
                parseMediaIntent(intent, SharedContent.MediaType.FILE)
            }
        }
    }

    /**
     * Parses media content from an Intent, handling both single and multiple items.
     */
    private fun parseMediaIntent(intent: Intent, type: SharedContent.MediaType): SharedContent {
        return if (intent.action == Intent.ACTION_SEND_MULTIPLE) {
            val uris = intent.parseActionSendMultipleUris()
            if (uris.isNotEmpty()) {
                SharedContent.MultipleMedia(uris = uris, type = type)
            } else {
                Timber.w("No URIs found in ACTION_SEND_MULTIPLE intent")
                SharedContent.Text("")
            }
        } else {
            val uri = intent.parseActionSendUri()
            if (uri != null) {
                SharedContent.SingleMedia(uri = uri, type = type)
            } else {
                // Fallback: try to get text content
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
                SharedContent.Text(text)
            }
        }
    }

    /**
     * Legacy content parsing for backward compatibility with old factory methods.
     */
    private fun parseLegacySharedContent(args: Bundle): SharedContent {
        return when {
            args.containsKey(SHARING_TEXT_KEY) -> {
                val result = args.getString(SHARING_TEXT_KEY, "")
                if (URLUtil.isValidUrl(result)) {
                    SharedContent.Url(result)
                } else {
                    SharedContent.Text(result)
                }
            }
            args.containsKey(SHARING_IMAGE_KEY) -> {
                val uri = args.getString(SHARING_IMAGE_KEY, "")
                SharedContent.SingleMedia(uri = uri, type = SharedContent.MediaType.IMAGE)
            }
            args.containsKey(SHARING_FILE_KEY) -> {
                val uri = args.getString(SHARING_FILE_KEY, "")
                SharedContent.SingleMedia(uri = uri, type = SharedContent.MediaType.FILE)
            }
            args.containsKey(SHARING_MULTIPLE_IMAGES_KEY) -> {
                val uris = args.getStringArrayList(SHARING_MULTIPLE_IMAGES_KEY) ?: emptyList()
                SharedContent.MultipleMedia(uris = uris, type = SharedContent.MediaType.IMAGE)
            }
            args.containsKey(SHARING_MULTIPLE_FILES_KEY) -> {
                val uris = args.getStringArrayList(SHARING_MULTIPLE_FILES_KEY) ?: emptyList()
                SharedContent.MultipleMedia(uris = uris, type = SharedContent.MediaType.FILE)
            }
            args.containsKey(SHARING_MULTIPLE_VIDEOS_KEY) -> {
                val uris = args.getStringArrayList(SHARING_MULTIPLE_VIDEOS_KEY) ?: emptyList()
                SharedContent.MultipleMedia(uris = uris, type = SharedContent.MediaType.VIDEO)
            }
            else -> {
                Timber.e("Unexpected shared data - no recognized keys in bundle")
                SharedContent.Text("")
            }
        }
    }

    private fun proceedWithCommand(command: SharingCommand) {
        when (command) {
            is SharingCommand.Dismiss -> {
                dismiss()
            }
            is SharingCommand.ShowToast -> {
                toast(command.message)
            }
            is SharingCommand.NavigateToObject -> {
                dismiss()
                findNavController().navigate(
                    R.id.objectNavigation,
                    EditorFragment.args(
                        ctx = command.objectId,
                        space = command.spaceId
                    )
                )
            }
            is SharingCommand.ObjectAddedToSpaceToast -> {
                val msg = resources.getString(
                    R.string.sharing_menu_toast_object_added,
                    command.spaceName
                )
                toast(msg = msg)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().sharingComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().sharingComponent.release()
    }

    companion object {
        // New single entry point key
        private const val SHARING_INTENT_KEY = "arg.sharing.intent"

        // Legacy keys (kept for backward compatibility)
        private const val SHARING_TEXT_KEY = "arg.sharing.text-key"
        private const val SHARING_IMAGE_KEY = "arg.sharing.image-key"
        private const val SHARING_FILE_KEY = "arg.sharing.file-key"
        private const val SHARING_MULTIPLE_IMAGES_KEY = "arg.sharing.multiple-images-key"
        private const val SHARING_MULTIPLE_VIDEOS_KEY = "arg.sharing.multiple-videos-key"
        private const val SHARING_MULTIPLE_FILES_KEY = "arg.sharing.multiple-files-key"

        // MIME type constants
        private const val MIME_TEXT_PLAIN = "text/plain"
        private const val MIME_TEXT_PREFIX = "text/"
        private const val MIME_IMAGE_PREFIX = "image/"
        private const val MIME_VIDEO_PREFIX = "video/"
        private const val MIME_AUDIO_PREFIX = "audio/"
        private const val MIME_APPLICATION_PREFIX = "application/"
        private const val MIME_PDF = "application/pdf"

        /**
         * Single entry point for all share intents.
         * Handles all MIME types: text, images, videos, audio, PDF, and files.
         *
         * @param intent The share intent from Android system
         * @return A new SharingFragment instance
         */
        fun newInstance(intent: Intent): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_INTENT_KEY to intent)
        }

        // Legacy factory methods - kept for backward compatibility
        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun text(data: String): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_TEXT_KEY to data)
        }

        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun image(uri: String): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_IMAGE_KEY to uri)
        }

        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun images(uris: List<String>): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_MULTIPLE_IMAGES_KEY to ArrayList(uris))
        }

        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun videos(uris: List<String>): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_MULTIPLE_VIDEOS_KEY to ArrayList(uris))
        }

        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun files(uris: List<String>): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_MULTIPLE_FILES_KEY to ArrayList(uris))
        }

        @Deprecated("Use newInstance(intent) instead", ReplaceWith("newInstance(intent)"))
        fun file(uri: String): SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_FILE_KEY to uri)
        }
    }
}
