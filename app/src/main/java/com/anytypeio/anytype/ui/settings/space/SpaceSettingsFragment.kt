package com.anytypeio.anytype.ui.settings.space

import android.os.Bundle
import android.view.LayoutInflater
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
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.ComposeDialogView
import com.anytypeio.anytype.core_ui.foundation.Divider
import com.anytypeio.anytype.core_ui.foundation.Option
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetComposeFragment
import com.anytypeio.anytype.presentation.settings.MainSettingsViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.ui.settings.typography
import com.anytypeio.anytype.ui.spaces.Section
import com.anytypeio.anytype.ui.spaces.TypeOfSpace
import com.anytypeio.anytype.ui_settings.main.SpaceHeader

class SpaceSettingsFragment : BaseBottomSheetComposeFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ) = ComposeDialogView(
        context = requireContext(), dialog = requireDialog()
    ).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            MaterialTheme(typography = typography) {
                SpaceSettingsScreen(onSpaceIconClick = {},
                    onNameSet = {},
                    workspace = MainSettingsViewModel.WorkspaceAndAccount.Account(
                        space = MainSettingsViewModel.WorkspaceAndAccount.SpaceData(
                            "Personal space", icon = SpaceIconView.Placeholder
                        ), profile = null
                    )
                )
            }
        }
    }

    override fun injectDependencies() {
        // TODO
    }

    override fun releaseDependencies() {
        // TODO
    }
}

@Composable
fun SpaceSettingsScreen(
    workspace: MainSettingsViewModel.WorkspaceAndAccount,
    onSpaceIconClick: () -> Unit,
    onNameSet: (String) -> Unit
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
                workspace = workspace,
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
                onClick = {
                    // TODO
                })
        }
        item {
            Option(image = R.drawable.ic_personalization,
                text = stringResource(R.string.personalization),
                onClick = {
                    // TODO
                })
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
                    text = "Space ID", style = Title1, modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    text = "sddsdsds;lds;dsld;sld;wd ssdsd s dsdsdsdsdsdsdsd sd sd sdsdsds sd sds d d",
                    style = PreviewTitle2Regular,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 12.dp, end = 50.dp),
                    maxLines = 2
                )
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
                Text(
                    text = "sddsdsds;lds;dsld;sld;wd ssdsd s dsdsdsdsdsdsdsd sd sd sdsdsds sd sds d d",
                    style = PreviewTitle2Regular,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 12.dp, end = 50.dp),
                    maxLines = 1
                )
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
                Text(
                    text = "sddsdsds;lds;dsld;sld;wd ssdsd s dsdsdsdsdsdsdsd sd sd sdsdsds sd sds d d",
                    style = PreviewTitle2Regular,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 12.dp, end = 50.dp),
                    maxLines = 1
                )
            }
        }
        item {
            Box(modifier = Modifier.height(78.dp)) {
                ButtonWarning(
                    onClick = {},
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