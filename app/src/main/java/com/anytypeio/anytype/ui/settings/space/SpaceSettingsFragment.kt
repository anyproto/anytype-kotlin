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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.extensions.throttledClick
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.spaces.SpaceSettingsViewModel
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.Section
import com.anytypeio.anytype.ui.spaces.TypeOfSpace
import com.anytypeio.anytype.ui_settings.main.SpaceHeader
import javax.inject.Inject
import timber.log.Timber

class SpaceSettingsFragment : BaseBottomSheetComposeFragment() {

    @Inject
    lateinit var factory: SpaceSettingsViewModel.Factory

    private val vm by viewModels<SpaceSettingsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(), dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                SpaceSettingsScreen(onSpaceIconClick = {},
                    onNameSet = vm::onNameSet,
                    spaceData = vm.spaceViewState.collectAsStateWithLifecycle().value,
                    onDeleteSpaceClicked = {
                        toast("Coming soon...")
                    },
                    onFileStorageClick = {
                        findNavController().navigate(R.id.filesStorageScreen)
                    },
                    onPersonalizationClicked = {
                       findNavController().navigate(R.id.personalizationScreen)
                    }
                )
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
                    is ViewState.Success -> spaceData.data.name.also {
                        Timber.d("Setting name: $it")
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
            Section(title = "Type")
        }
        item {
            TypeOfSpace()
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
            Option(image = R.drawable.ic_personalization,
                text = stringResource(R.string.personalization),
                onClick = throttledClick(onPersonalizationClicked)
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
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.spaceId ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp),
                        maxLines = 2
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
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.createdBy ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp),
                        maxLines = 1
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
                    modifier = Modifier.padding(top = 12.dp)
                )
                if (spaceData is ViewState.Success) {
                    Text(
                        text = spaceData.data.createdDate ?: stringResource(id = R.string.unknown),
                        style = PreviewTitle2Regular,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(bottom = 12.dp, end = 50.dp),
                        maxLines = 1
                    )
                }
            }
        }
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
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}