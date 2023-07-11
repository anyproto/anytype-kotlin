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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.views.BaseAlertDialog
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_utils.ui.BaseComposeFragment
import com.anytypeio.anytype.sample.R
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.FlowPreview

class ComposeDialogFragment : BaseComposeFragment() {

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

                val showDialog = remember { mutableStateOf(false) }

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
                            text = "Dialogs",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            style = HeadlineHeading,
                            color = colorResource(id = R.color.text_primary)
                        )
                        ButtonPrimary(
                            onClick = { showDialog.value = true },
                            modifier = Modifier
                                .wrapContentHeight()
                                .padding(horizontal = 16.dp, vertical = 32.dp)
                                .align(Alignment.CenterHorizontally),
                            text = "Show Dialog",
                            size = ButtonSize.Medium
                        )
                    }
                }

                if (showDialog.value) {
                    BaseAlertDialog(
                        onDismissRequest = { showDialog.value = false },
                        onButtonClick = { showDialog.value = false },
                        dialogText = "Here’s some alert text. It can span multiple lines if needed!",
                        buttonText = "Ok"
                    )
                }
            }
        }
    }

    override fun injectDependencies() {}

    override fun releaseDependencies() {}
}