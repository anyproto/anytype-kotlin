package com.anytypeio.anytype.sample.design_system

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryDarkTheme
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLoading
import com.anytypeio.anytype.core_ui.views.ButtonWarningLoading
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.sample.R
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.FlowPreview

class ComposeButtonsPrimaryFragment : BaseComposeFragment() {

    @FlowPreview
    @ExperimentalPagerApi
    @ExperimentalMaterialApi
    @ExperimentalComposeUiApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Button.XSmall",
                            modifier = Modifier.align(CenterHorizontally),
                            style = HeadlineHeading,
                            color = colorResource(id = R.color.text_primary)
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.XSmall
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            enabled = false,
                            size = ButtonSize.XSmall
                        )
                        loadingPrimaryButton(
                            size = ButtonSize.XSmall,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.XSmallSecondary
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            enabled = false,
                            size = ButtonSize.XSmallSecondary
                        )
                        loadingSecondaryButton(
                            size = ButtonSize.XSmallSecondary,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.XSmall
                        )
                        loadingWarningButton(
                            size = ButtonSize.XSmall,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonPrimaryDarkTheme(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button Dark",
                            size = ButtonSize.XSmall
                        )
                        Text(
                            text = "Button.Small",
                            modifier = Modifier
                                .align(CenterHorizontally)
                                .padding(top = 16.dp),
                            style = HeadlineHeading,
                            color = colorResource(id = R.color.text_primary)
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Small
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Small,
                            enabled = false
                        )
                        loadingPrimaryButton(
                            size = ButtonSize.Small,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.SmallSecondary
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            enabled = false,
                            size = ButtonSize.SmallSecondary
                        )
                        loadingSecondaryButton(
                            size = ButtonSize.SmallSecondary,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Small
                        )
                        loadingWarningButton(
                            size = ButtonSize.Small,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                        )
                        ButtonPrimaryDarkTheme(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button Dark",
                            size = ButtonSize.Small
                        )
                        Text(
                            text = "Button.Medium",
                            modifier = Modifier
                                .align(CenterHorizontally)
                                .padding(top = 16.dp),
                            style = HeadlineHeading,
                            color = colorResource(id = R.color.text_primary)
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Medium
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Medium,
                            enabled = false
                        )
                        loadingPrimaryButton(
                            size = ButtonSize.Medium,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .wrapContentHeight()
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.MediumSecondary
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            enabled = false,
                            size = ButtonSize.MediumSecondary
                        )
                        loadingSecondaryButton(
                            size = ButtonSize.MediumSecondary,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Medium
                        )
                        loadingWarningButton(
                            size = ButtonSize.Medium,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ButtonPrimaryDarkTheme(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button Dark",
                            size = ButtonSize.Medium
                        )
                        Text(
                            text = "Button.Large",
                            modifier = Modifier
                                .align(CenterHorizontally)
                                .padding(top = 16.dp),
                            style = HeadlineHeading,
                            color = colorResource(id = R.color.text_primary)
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Large
                        )
                        ButtonPrimary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Large,
                            enabled = false
                        )
                        loadingPrimaryButton(
                            size = ButtonSize.Large,
                            modifierBox = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.LargeSecondary
                        )
                        ButtonSecondary(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            enabled = false,
                            size = ButtonSize.LargeSecondary
                        )
                        loadingSecondaryButton(
                            size = ButtonSize.LargeSecondary,
                            modifierBox = Modifier
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
                            size = ButtonSize.Large
                        )
                        loadingWarningButton(
                            size = ButtonSize.Large,
                            modifierBox = Modifier
                                .align(CenterHorizontally),
                            modifierButton = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        ButtonPrimaryDarkTheme(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button Dark",
                            size = ButtonSize.Large
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun loadingPrimaryButton(
        size: ButtonSize,
        modifierBox: Modifier = Modifier,
        modifierButton: Modifier = Modifier
    ) {
        val loadingState = remember { mutableStateOf(false) }
        ButtonPrimaryLoading(
            onClick = {
                loadingState.value = !loadingState.value
            },
            modifierButton = modifierButton,
            modifierBox = modifierBox,
            text = "Button",
            size = size,
            loading = loadingState.value,
            enabled = true,
            loadingItemsCount = 3
        )
    }

    @Composable
    fun loadingSecondaryButton(
        size: ButtonSize,
        modifierBox: Modifier = Modifier,
        modifierButton: Modifier = Modifier
    ) {
        val loadingState = remember { mutableStateOf(false) }
        ButtonSecondaryLoading(
            onClick = {
                loadingState.value = !loadingState.value
            },
            modifierButton = modifierButton,
            modifierBox = modifierBox,
            text = "Button",
            size = size,
            loading = loadingState.value,
            enabled = true,
            loadingItemsCount = 3
        )
    }

    @Composable
    fun loadingWarningButton(
        size: ButtonSize,
        modifierBox: Modifier = Modifier,
        modifierButton: Modifier = Modifier
    ) {
        val loadingState = remember { mutableStateOf(false) }
        ButtonWarningLoading(
            onClick = {
                loadingState.value = !loadingState.value
            },
            modifierButton = modifierButton,
            modifierBox = modifierBox,
            text = "Button",
            size = size,
            loading = loadingState.value,
            loadingItemsCount = 3
        )
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}

@Composable
@Preview
fun MyFragment() {
    ComposeButtonsPrimaryFragment()
}