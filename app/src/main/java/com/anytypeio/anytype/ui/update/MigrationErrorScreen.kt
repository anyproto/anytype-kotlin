package com.anytypeio.anytype.ui.update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.AlertConfig
import com.anytypeio.anytype.core_ui.foundation.AlertIcon
import com.anytypeio.anytype.core_ui.foundation.GRADIENT_TYPE_RED
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate

@Composable
fun MigrationInProgressScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                ,
                backgroundColor = colorResource(R.color.shape_secondary),
                color = Color(0xFFFFB522),
                strokeWidth = 8.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.migration_migration_is_in_progress),
                style = HeadlineHeading,
                color = colorResource(R.color.text_primary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 44.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.migration_this_shouldn_t_take_long),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 44.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun MigrationFailedScreen(
    state: MigrationHelperDelegate.State.Failed,
    onRetryClicked: () -> Unit
) {
    val description = when(state) {
        MigrationHelperDelegate.State.Failed.NotEnoughSpace -> {
            stringResource(R.string.migration_error_please_free_up_space_and_run_the_process_again)
        }
        is MigrationHelperDelegate.State.Failed.UnknownError -> {
            state.error.message ?: stringResource(R.string.unknown_error)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
        ) {
            AlertIcon(
                icon = AlertConfig.Icon(
                    gradient = GRADIENT_TYPE_RED,
                    icon = R.drawable.ic_alert_error
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.migration_migration_failed),
                style = HeadlineHeading,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    color = colorResource(R.color.text_secondary),
                    style = BodyCalloutRegular,
                    modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
        ButtonPrimary(
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            text = stringResource(R.string.migration_error_try_again),
            size = ButtonSize.Large,
            onClick = onRetryClicked
        )
    }
}

@DefaultPreviews
@Composable
fun MigrationInProgressScreenPreview() {
    MigrationInProgressScreen()
}

@DefaultPreviews
@Composable
fun MigrationFailedScreenPreview() {
    MigrationFailedScreen(
        state = MigrationHelperDelegate.State.Failed.NotEnoughSpace,
        onRetryClicked = {}
    )
}

@DefaultPreviews
@Composable
fun MigrationFailedGenericScreenPreview() {
    MigrationFailedScreen(
        state = MigrationHelperDelegate.State.Failed.UnknownError(
            Exception(stringResource(R.string.default_text_placeholder))
        ),
        onRetryClicked = {}
    )
}