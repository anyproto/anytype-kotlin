package com.anytypeio.anytype.core_ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

@ExperimentalMaterial3Api
@Composable
fun BaseAlertDialog(
    dialogText: String,
    buttonText: String,
    onButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        modifier = Modifier
            .padding(horizontal = 53.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(32.dp),
        onDismissRequest = onDismissRequest
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = dialogText,
                style = UXBody,
                maxLines = 5,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(18.dp))
            ButtonOnboardingSecondaryLarge(
                text = buttonText,
                onClick = onButtonClick,
                size = ButtonSize.Large,
                modifierBox = Modifier.fillMaxWidth()
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun BaseTwoButtonsDarkThemeAlertDialog(
    dialogText: String,
    actionButtonText: String,
    dismissButtonText: String,
    onActionButtonClick: () -> Unit,
    onDismissButtonClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .background(
                color = colorResource(R.color.background_secondary),
                shape = RoundedCornerShape(size = 8.dp)
            )
            .padding(32.dp),
        onDismissRequest = onDismissRequest
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,) {
            Text(
                modifier = Modifier.padding(horizontal = 10.dp),
                text = dialogText,
                style = UXBody,
                maxLines = 5,
                textAlign = TextAlign.Center,
                color = colorResource(R.color.text_primary)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                ButtonOnboardingSecondaryLarge(
                    text = dismissButtonText,
                    onClick = onDismissButtonClick,
                    size = ButtonSize.Large,
                    modifierBox = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                ButtonOnboardingPrimaryLarge(
                    text = actionButtonText,
                    onClick = onActionButtonClick,
                    size = ButtonSize.Large,
                    modifierBox = Modifier.weight(1f)
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
@DefaultPreviews
fun BaseAlertDialogPreview() {
    BaseTwoButtonsDarkThemeAlertDialog(
        dialogText = "This is a dialog",
        actionButtonText = "Contact us",
        dismissButtonText = "Ok",
        onDismissButtonClick = {},
        onActionButtonClick = {},
        onDismissRequest = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@DefaultPreviews
fun BaseAlertDialogPreviewLight() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BaseAlertDialog(
            dialogText = "This is a dialog",
            buttonText = "Ok",
            onButtonClick = {},
            onDismissRequest = {}
        )
    }
}