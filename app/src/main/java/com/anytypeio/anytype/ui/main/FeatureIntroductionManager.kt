package com.anytypeio.anytype.ui.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.ui.vault.SpacesIntroductionScreen

/**
 * Manages feature introductions in MainActivity by observing MainViewModel state.
 *
 * Shows feature introductions (like SpacesIntroductionScreen) when MainViewModel
 * determines they should be shown (after account start, for existing users only).
 */
@Composable
fun FeatureIntroductionManager(
    viewModel: MainViewModel
) {
    // Observe the StateFlow from MainViewModel
    val showSpacesIntroduction by viewModel.showSpacesIntroduction.collectAsState()

    // Show Spaces Introduction Screen when ViewModel state is not null
    if (showSpacesIntroduction != null) {
        SpacesIntroductionScreen(
            onDismiss = {
                viewModel.onSpacesIntroductionDismissed()
            },
            onComplete = {
                viewModel.onSpacesIntroductionDismissed()
            }
        )
    }
}
