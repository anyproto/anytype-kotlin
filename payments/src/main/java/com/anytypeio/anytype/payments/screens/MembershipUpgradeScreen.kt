package com.anytypeio.anytype.payments.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.ButtonPrimary
import com.anytypeio.anytype.core_ui.views.ButtonSize
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.payments.R

@Composable
fun MembershipUpgradeScreen(
    onButtonClicked: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .background(
                shape = RoundedCornerShape(16.dp),
                color = colorResource(id = R.color.background_primary)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Dragger()
        }
        Spacer(modifier = Modifier.height(19.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.membership_upgrade_title),
            color = colorResource(id = R.color.text_primary),
            style = HeadlineHeading,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            text = stringResource(id = R.string.membership_upgrade_description),
            color = colorResource(id = R.color.text_primary),
            style = BodyRegular,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(30.dp))
        ButtonPrimary(
            text = stringResource(id = R.string.membership_upgrade_button),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            onClick = { onButtonClicked() },
            size = ButtonSize.LargeSecondary
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun MembershipUpgradeScreenPreview() {
    MembershipUpgradeScreen(onDismiss = {}, onButtonClicked = {})
}