package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.UXBody
import com.anytypeio.anytype.core_ui.views.animations.DotsLoadingIndicator
import com.anytypeio.anytype.core_ui.views.animations.FadeAnimationSpecs
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.models.AllContentTab
import com.anytypeio.anytype.feature_allcontent.models.TopBarViewState
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentUiState
import com.anytypeio.anytype.feature_allcontent.ui.AllContentNavigation.ALL_CONTENT_MAIN

@Composable
fun AllContentMainScreen(uiState: AllContentUiState, onTabClick: (AllContentTab) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        when (uiState) {
            AllContentUiState.Idle -> {}
            is AllContentUiState.Content -> {
                AllContentTopBarContainer(uiState.topToolbarState)
                AllContentTabs(uiState.tabs) {}
                //ContentState(uiState)
            }
            is AllContentUiState.Error -> {
                AllContentTopBarContainer(uiState.topToolbarState)
                //ErrorState(message = uiState.message)
            }
            is AllContentUiState.Loading -> {
                AllContentTopBarContainer(uiState.topToolbarState)
                //LoadingState()
            }
        }
    }
}

@Composable
private fun BoxScope.LoadingState() {
    val loadingAlpha by animateFloatAsState(targetValue = 1f, label = "")
    DotsLoadingIndicator(
        animating = true,
        modifier = Modifier
            .graphicsLayer { alpha = loadingAlpha }
            .align(Alignment.Center),
        animationSpecs = FadeAnimationSpecs(itemCount = 3),
        color = colorResource(id = R.color.text_primary),
        size = ButtonSize.Large
    )
}

@Composable
private fun ContentState(uiState: AllContentUiState.Content) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}

@Composable
private fun BoxScope.ErrorState(message: String) {
    Text(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.Center),
        text = "Error : message",
        color = colorResource(id = R.color.palette_system_red),
        style = UXBody
    )
}

object AllContentNavigation {
    const val ALL_CONTENT_MAIN = "all_content_main"
}