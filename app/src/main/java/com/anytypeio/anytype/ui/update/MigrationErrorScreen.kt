package com.anytypeio.anytype.ui.update

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSecondary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.views.HeadlineSubheading
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate


@Composable
fun MigrationStartScreen(
    onStartUpdate: () -> Unit
) {
    var showReadMoreView by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.background_primary))
        ,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.Center),
                    strokeWidth = 6.dp,
                    trackColor = colorResource(R.color.palette_dark_blue),
                    color = colorResource(R.color.palette_dark_blue)
                )
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(
                        R.drawable.ic_migration_arrow
                    ),
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = stringResource(R.string.migration_screen_new_version_update),
                style = HeadlineTitle,
                color = colorResource(R.color.text_primary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.migration_screen_description_1),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.migration_screen_description_2),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter)
        ) {
            ButtonPrimary(
                modifier = Modifier.fillMaxWidth(),
                onClick = onStartUpdate,
                text = stringResource(R.string.migration_screen_start_update),
                size = ButtonSize.Large
            )
            Spacer(modifier = Modifier.height(12.dp))
            ButtonSecondary(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showReadMoreView = true },
                text = stringResource(R.string.migration_screen_read_more),
                size = ButtonSize.Large
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showReadMoreView) {
        MigrationReadMoreBottomSheet(
            onDismissRequest = {
                showReadMoreView = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MigrationReadMoreBottomSheet(
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Dragger(
                modifier = Modifier.padding(vertical = 6.dp)
            )
        },
        containerColor = colorResource(R.color.background_secondary),
        content = {
            MigrationReadMoreScreenContent()
        }
    )
}

@Composable
fun MigrationReadMoreScreenContent() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(
            horizontal = 16.dp
        )
    ) {
        item {
            Spacer(modifier = Modifier.height(44.dp))
            Box(
                modifier = Modifier
                    .background(
                        shape = CircleShape,
                        color = colorResource(R.color.palette_dark_blue)
                    )
                    .size(48.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_migration_union_32),
                    contentDescription = "Icon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.migration_screen_what_to_expect),
                style = HeadlineSubheading,
                color = colorResource(R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.migration_screen_what_to_expect_description),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary)
            )
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .background(
                        shape = CircleShape,
                        color = colorResource(R.color.palette_dark_blue)
                    )
                    .size(48.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_migration_data),
                    contentDescription = "Icon",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.migration_screen_your_data_remains_safe),
                style = HeadlineSubheading,
                color = colorResource(R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.migration_screen_your_data_description),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.migration_screen_your_data_description_2),
                style = BodyCalloutRegular,
                color = colorResource(R.color.text_secondary)
            )
            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}

@DefaultPreviews
@Composable
fun MigrationReadMoreScreenPreview() {
    MigrationReadMoreScreenContent()
}

@DefaultPreviews
@Composable
fun MigrationStartScreenPreview() {
    MigrationStartScreen(
        onStartUpdate = {}
    )
}

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.Center),
                    strokeWidth = 6.dp,
                    color = colorResource(R.color.palette_dark_blue),
                    trackColor = colorResource(R.color.shape_primary)
                )
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(R.drawable.ic_migration_error_exclamation),
                    contentDescription = null
                )
            }
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
        is MigrationHelperDelegate.State.Failed.NotEnoughSpace -> {
            stringResource(R.string.migration_error_please_free_up_space_and_run_the_process_again, state.requiredSpace)
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.Center),
                    strokeWidth = 6.dp,
                    color = colorResource(R.color.palette_dark_red),
                    trackColor = colorResource(R.color.shape_primary),
                    progress = { 1.0f }
                )
                Image(
                    modifier = Modifier.align(Alignment.Center),
                    painter = painterResource(
                        R.drawable.ic_migration_error_exclamation
                    ),
                    contentDescription = null
                )
            }
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
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
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
        state = MigrationHelperDelegate.State.Failed.NotEnoughSpace(450),
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