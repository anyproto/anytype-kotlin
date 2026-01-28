package com.anytypeio.anytype.core_ui.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.Caption2Medium

@Composable
fun GlobalNameOrIdentity(
    modifier: Modifier = Modifier,
    globalName: String?,
    identity: String?,
    onIdentityClicked: () -> Unit
) {
    val effectiveModifier = if (globalName.isNullOrEmpty()) {
        modifier.then(
            Modifier
                .wrapContentWidth()
                .height(32.dp)
                .background(
                    color = colorResource(id = R.color.control_accent_25),
                    shape = RoundedCornerShape(16.dp)
                )
        )
    } else {
        modifier.height(32.dp)
    }
    Row(
        modifier = effectiveModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!globalName.isNullOrEmpty()) {
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_account_name_18),
                contentDescription = "Account any name"
            )
            Text(
                text = globalName,
                style = Caption2Medium,
                color = colorResource(id = R.color.text_primary),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.MiddleEllipsis,
                maxLines = 1,
                modifier = Modifier
                    .wrapContentWidth()
                    .noRippleClickable { onIdentityClicked() }
            )
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                modifier = Modifier.size(18.dp),
                painter = painterResource(R.drawable.ic_account_identity_16),
                contentDescription = "Account any name",
                contentScale = ContentScale.Fit
            )
            Text(
                text = identity.orEmpty(),
                style = Caption2Medium,
                color = colorResource(id = R.color.text_primary),
                overflow = TextOverflow.MiddleEllipsis,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .width(108.dp)
                    .noRippleClickable { onIdentityClicked() }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}