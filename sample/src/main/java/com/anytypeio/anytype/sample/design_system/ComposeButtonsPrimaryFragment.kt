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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryMedium
import com.anytypeio.anytype.core_ui.views.ButtonPrimarySmall
import com.anytypeio.anytype.core_ui.views.ButtonPrimaryXSmall
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryLarge
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryMedium
import com.anytypeio.anytype.core_ui.views.ButtonSecondarySmall
import com.anytypeio.anytype.core_ui.views.ButtonSecondaryXSmall
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.ButtonWarning
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
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
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
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
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .align(CenterHorizontally),
                            text = "Button",
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
                        ButtonWarning(
                            onClick = {},
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .align(CenterHorizontally),
                            text = "Button",
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
                    }
                }
            }
        }
    }

    override fun injectDependencies() {}
    override fun releaseDependencies() {}
}

@Composable
@Preview
fun MyFragment() {
    ComposeButtonsPrimaryFragment()
}