package com.anytypeio.anytype.ui.settings.space

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.core_utils.ext.shareFile
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel.Command
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.ui.settings.FilesStorageFragment
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.DeleteSpaceWarning
import com.anytypeio.anytype.ui.spaces.Section
import com.anytypeio.anytype.ui.spaces.TypeOfSpace
import com.anytypeio.anytype.ui_settings.main.SpaceHeader
import java.io.File
import javax.inject.Inject
import timber.log.Timber

class SpaceSettingsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SpaceSettingsViewModel.Factory

    @Inject
    lateinit var uriFileProvider: UriFileProvider

    private val vm by viewModels<SpaceSettingsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(), dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                SpaceSettingsScreen(
                    onSpaceIconClick = {},
                    onNameSet = vm::onNameSet,
                    spaceData = vm.spaceViewState.collectAsStateWithLifecycle().value,
                    onDeleteSpaceClicked = throttledClick(
                        onClick = {
                            vm.onDeleteSpaceClicked()
                            val dialog = DeleteSpaceWarning.new()
                            dialog.onDeletionAccepted = {
                                dialog.dismiss()
                                vm.onDeleteSpaceAcceptedClicked()
                            }
                            dialog.onDeletionCancelled = {
                                vm.onDeleteSpaceWarningCancelled()
                            }
                            dialog.show(childFragmentManager, null)
                        }
                    ),
                    onFileStorageClick = throttledClick(
                        onClick = {
                            findNavController()
                                .navigate(
                                    R.id.filesStorageScreen,
                                    FilesStorageFragment.args(isRemote = true)
                                )
                        }
                    ),
                    onPersonalizationClicked = throttledClick(
                        onClick = {
                            findNavController().navigate(R.id.personalizationScreen)
                        }
                    ),
                    onSpaceIdClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Space ID",
                            successToast = context.getString(R.string.space_id_copied_toast_msg)
                        )
                    },
                    onNetworkIdClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Network ID",
                            successToast = context.getString(R.string.network_id_copied_toast_msg)
                        )
                    },
                    onCreatedByClicked = {
                        context.copyPlainTextToClipboard(
                            plainText = it,
                            label = "Created-by ID",
                            successToast = context.getString(R.string.created_by_id_copied_toast_msg)
                        )
                    },
                    onDebugClicked = vm::onSpaceDebugClicked
                )
                LaunchedEffect(Unit) { vm.toasts.collect { toast(it) } }
                LaunchedEffect(Unit) {
                    vm.isDismissed.collect { isDismissed ->
                        if (isDismissed) dismiss()
                    }
                }
                LaunchedEffect(Unit) {
                    observeCommands()
                }
            }
        }
    }

    private suspend fun observeCommands() {
        vm.commands.collect { command ->
            when (command) {
                is Command.ShareSpaceDebug -> {
                    try {
                        shareFile(
                            uriFileProvider.getUriForFile(File(command.filepath))
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error while sharing space debug").also {
                            toast("Error while sharing space debug. Please try again later.")
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expand()
        skipCollapsed()
    }

    override fun injectDependencies() {
        componentManager().spaceSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().spaceSettingsComponent.release()
    }
}

@Composable
fun SpaceSettingsScreen(
    spaceData: ViewState<SpaceSettingsViewModel.SpaceData>,
    onSpaceIconClick: () -> Unit,
    onNameSet: (String) -> Unit,
    onDeleteSpaceClicked: () -> Unit,
    onFileStorageClick: () -> Unit,
    onPersonalizationClicked: () -> Unit,
    onSpaceIdClicked: (Id) -> Unit,
    onNetworkIdClicked: (Id) -> Unit,
    onCreatedByClicked: (Id) -> Unit,
    onDebugClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .nestedScroll(rememberNestedScrollInteropConnection())
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            SpaceHeader(
                modifier = Modifier,
                name = when (spaceData) {
                    is ViewState.Success -> spaceData.data.name.ifEmpty { 
                        stringResource(id = R.string.untitled)
                    }
                    else -> null
                },
                icon = when (spaceData) {
                    is ViewState.Success -> spaceData.data.icon
                    else -> null
                },
                onSpaceIconClick = onSpaceIconClick,
                onNameSet = onNameSet
            )
        }
        item { Divider() }
        item {
            Section(title = stringResource(id = R.string.type))
        }
        item {
            TypeOfSpace(
                if (spaceData is ViewState.Success)
                    spaceData.data.spaceType
                else
                    null
            )
        }
        item {
            Divider()
        }
        item {
            Section(title = stringResource(id = R.string.settings))
        }
        item {
            Option(image = R.drawable.ic_file_storage,
                text = stringResource(R.string.remote_storage),
                onClick = throttledClick(onFileStorageClick)
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(image = R.drawable.ic_personalization,
                text = stringResource(R.string.personalization),
                onClick = throttledClick(onPersonalizationClicked)
            )
        }
        item {
            Divider(paddingStart = 60.dp)
        }
        item {
            Option(image = R.drawable.ic_debug,
                text = stringResource(R.string.debug),
                onClick = throttledClick(onDebugClicked)
            )
        }
        item {
            Divider(
                paddingStart = 60.dp
            )
        }
        item {
            Section(title = stringResource(id = R.string.space_info))
        }
        item {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.space_id),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.spaceId ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 2,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp)
                            .noRippleClickable {
                                onSpaceIdClicked(spaceData.data.spaceId.orEmpty())
                            }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.created_by),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.createdBy ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 1,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp)
                            .noRippleClickable {
                                onCreatedByClicked(spaceData.data.createdBy.orEmpty())
                            }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(72.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.creation_date),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    val formattedDate = spaceData.data.createdDateInMillis?.formatTimeInMillis(
                        DateConst.DEFAULT_DATE_FORMAT
                    ) ?: stringResource(id = R.string.unknown)
                    Text(
                        text = formattedDate,
                        style = PreviewTitle2Regular,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp),
                        maxLines = 1,
                        color = colorResource(id = R.color.text_primary)
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .height(92.dp)
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.network_id),
                    style = Title1,
                    modifier = Modifier.padding(top = 12.dp),
                    color = colorResource(id = R.color.text_primary)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.network ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        maxLines = 2,
                        color = colorResource(id = R.color.text_primary),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp)
                            .noRippleClickable {
                                onNetworkIdClicked(spaceData.data.network.orEmpty())
                            }
                    )
                }
            }
        }
        if (spaceData is ViewState.Success && spaceData.data.isDeletable) {
            item {
                Box(modifier = Modifier.height(78.dp)) {
                    ButtonWarning(
                        onClick = { onDeleteSpaceClicked() },
                        text = "Delete space",
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        size = ButtonSize.Large
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}