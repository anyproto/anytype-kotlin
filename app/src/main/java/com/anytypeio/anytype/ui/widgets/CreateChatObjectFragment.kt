package com.anytypeio.anytype.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.parseImagePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.withParentSafe
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.feature_chats.ui.CreateChatObjectScreen
import com.anytypeio.anytype.presentation.widgets.CreateChatObjectViewModel
import com.anytypeio.anytype.ui.chats.SelectChatIconFragment
import com.anytypeio.anytype.ui.settings.typography
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateChatObjectFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: CreateChatObjectViewModel.Factory

    private val space get() = arg<Id>(SPACE_ID_KEY)

    private val vm by viewModels<CreateChatObjectViewModel> { factory }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            context?.let {
                vm.onImageSelected(url = uri.parseImagePath(it))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(
                typography = typography,
                shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp)),
                colors = MaterialTheme.colors.copy(
                    surface = colorResource(id = R.color.context_menu_background)
                )
            ) {
                CreateChatObjectScreen(
                    icon = vm.icon.collectAsStateWithLifecycle().value,
                    onCreateClicked = { name ->
                        vm.onCreateClicked(name)
                    },
                    onIconUploadClicked = {
                        vm.onIconUploadClicked()
                    },
                    onIconRemoveClicked = {
                        vm.onIconRemoveClicked()
                    },
                    onEmojiIconClicked = {
                        vm.onEmojiIconClicked()
                    },
                    isLoading = vm.isLoading.collectAsStateWithLifecycle().value
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEmojiResultListener()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { vm.commands.collect { command -> proceed(command) } }
                launch { vm.toasts.collect { toast(it) } }
            }
        }
    }

    private fun setupEmojiResultListener() {
        childFragmentManager.setFragmentResultListener(
            SelectChatIconFragment.REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val emoji = bundle.getString(SelectChatIconFragment.RESULT_EMOJI_KEY)
            if (emoji != null) {
                vm.onEmojiSelected(emoji)
            }
        }
    }

    private fun proceed(command: CreateChatObjectViewModel.Command) {
        when (command) {
            is CreateChatObjectViewModel.Command.ChatObjectCreated -> {
                Timber.d("Chat object created: ${command.objectId}")
                withParentSafe<CreateChatObjectListener> {
                    onChatObjectCreated(objectId = command.objectId)
                }
                dismiss()
            }
            is CreateChatObjectViewModel.Command.UploadImage -> {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            is CreateChatObjectViewModel.Command.SelectEmoji -> {
                val dialog = SelectChatIconFragment.new()
                dialog.show(childFragmentManager, null)
            }
        }
    }

    override fun injectDependencies() {
        val vmParams = CreateChatObjectViewModel.VmParams(
            space = SpaceId(space)
        )
        componentManager().createChatObjectComponent.get(vmParams).inject(this)
    }

    override fun releaseDependencies() {
        super.releaseDependencies()
    }

    companion object {
        private const val SPACE_ID_KEY = "arg.create-chat-object.space-id"

        fun new(space: Id) = CreateChatObjectFragment().apply {
            arguments = bundleOf(
                SPACE_ID_KEY to space
            )
        }
    }
}

interface CreateChatObjectListener {
    fun onChatObjectCreated(objectId: Id)
}
