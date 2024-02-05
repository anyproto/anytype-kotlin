package com.anytypeio.anytype.ui.sharing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argStringList
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.sharing.AddToAnytypeViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.settings.typography
import java.lang.IllegalStateException
import javax.inject.Inject

class SharingFragment : BaseBottomSheetComposeFragment() {

    private val sharedData : SharingData get() {
        val args = requireArguments()
        return if (args.containsKey(SHARING_TEXT_KEY)) {
            val result = arg<String>(SHARING_TEXT_KEY)
            if (URLUtil.isValidUrl(result)) {
                SharingData.Url(result)
            } else {
                SharingData.Raw(result)
            }
        } else if (args.containsKey(SHARING_IMAGE_KEY)) {
            val result = arg<String>(SHARING_IMAGE_KEY)
            SharingData.Image(uri = result)
        } else if (args.containsKey(SHARING_FILE_KEY)) {
            val result = arg<String>(SHARING_FILE_KEY)
            SharingData.File(uri = result)
        }else if (args.containsKey(SHARING_MULTIPLE_IMAGES_KEY)) {
            val result = argStringList(SHARING_MULTIPLE_IMAGES_KEY)
            SharingData.Images(uris = result)
        } else if (args.containsKey(SHARING_MULTIPLE_FILES_KEY)) {
            val result = argStringList(SHARING_MULTIPLE_FILES_KEY)
            SharingData.Files(uris = result)
        } else {
            throw IllegalStateException("Unexpected result")
        }
    }

    @Inject
    lateinit var factory: AddToAnytypeViewModel.Factory

    private val vm by viewModels<AddToAnytypeViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography
            ) {
                AddToAnytypeScreen(
                    data = sharedData,
                    onDoneClicked = { option ->
                        when(option) {
                            SAVE_AS_BOOKMARK -> vm.onCreateBookmark(url = sharedData.data)
                            SAVE_AS_NOTE -> vm.onCreateNote(sharedData.data)
                            SAVE_AS_IMAGE -> vm.onShareMedia(listOf(sharedData.data))
                            SAVE_AS_FILE -> vm.onShareMedia(listOf(sharedData.data))
                            SAVE_AS_IMAGES -> vm.onShareMedia(
                                uris = (sharedData as SharingData.Images).uris
                            )
                            SAVE_AS_FILES -> vm.onShareMedia(
                                uris = (sharedData as SharingData.Files).uris
                            )
                        }
                    },
                    onCancelClicked = {
                        vm.onCancelClicked().also {
                            dismiss()
                        }
                    },
                    spaces = vm.spaceViews.collectAsStateWithLifecycle().value,
                    onSelectSpaceClicked = { vm.onSelectSpaceClicked(it) }
                )
                LaunchedEffect(Unit) {
                    vm.navigation.collect { nav ->
                        when(nav) {
                            is OpenObjectNavigation.OpenEditor -> {
                                dismiss()
                                findNavController().navigate(
                                    R.id.objectNavigation,
                                    bundleOf(
                                        EditorFragment.ID_KEY to nav.target
                                    )
                                )
                            }
                            else -> {
                                // Do nothing.
                            }
                        }
                    }
                }
                LaunchedEffect(Unit) {
                    vm.toasts.collect { toast ->
                        toast(toast)
                    }
                }
                LaunchedEffect(Unit) {
                    vm.commands.collect { command ->
                        proceedWithCommand(command)
                    }
                }
            }
        }
    }

    private fun proceedWithCommand(command: AddToAnytypeViewModel.Command) {
        when (command) {
            AddToAnytypeViewModel.Command.Dismiss -> {
                dismiss()
            }
            is AddToAnytypeViewModel.Command.ObjectAddToSpaceToast -> {
                val name = command.spaceName ?: resources.getString(R.string.untitled)
                val msg = resources.getString(R.string.sharing_menu_toast_object_added, name)
                toast(msg = msg)
            }
        }
    }

    override fun injectDependencies() {
        componentManager().addToAnytypeComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().addToAnytypeComponent.release()
    }

    companion object {
        private const val SHARING_TEXT_KEY = "arg.sharing.text-key"
        private const val SHARING_IMAGE_KEY = "arg.sharing.image-key"
        private const val SHARING_FILE_KEY = "arg.sharing.file-key"
        private const val SHARING_MULTIPLE_IMAGES_KEY = "arg.sharing.multiple-images-key"
        private const val SHARING_MULTIPLE_FILES_KEY = "arg.sharing.multiple-files-key"

        fun text(data: String) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_TEXT_KEY to data)
        }

        fun image(uri: String) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_IMAGE_KEY to uri)
        }

        fun images(uris: List<String>) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_MULTIPLE_IMAGES_KEY to ArrayList(uris))
        }

        fun files(uris: List<String>) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_MULTIPLE_FILES_KEY to ArrayList(uris))
        }

        fun file(uri: String) : SharingFragment = SharingFragment().apply {
            arguments = bundleOf(SHARING_FILE_KEY to uri)
        }
    }
}