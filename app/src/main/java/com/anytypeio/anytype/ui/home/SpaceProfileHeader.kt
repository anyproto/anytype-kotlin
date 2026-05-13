package com.anytypeio.anytype.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.views.Caption1Regular
import com.anytypeio.anytype.core_ui.views.HeadlineHeading
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.ui.widgets.collection.DefaultTheme
import com.anytypeio.anytype.localization.R as LocalizationR

@Composable
fun SpaceProfileHeader(
    spaceIcon: SpaceIconView,
    spaceName: String,
    globalName: String?,
    identity: String?,
    spaceAccessType: SpaceAccessType,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 24.dp, top = 24.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpaceIconView(
            icon = spaceIcon,
            mainSize = 64.dp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spaceName,
                    style = HeadlineHeading,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!globalName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Image(
                        modifier = Modifier.size(20.dp),
                        painter = painterResource(id = R.drawable.ic_membership_badge_18),
                        contentDescription = "membership badge"
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            val displayIdentity = globalName?.takeIf { it.isNotEmpty() } ?: identity
            val subtitle = displayIdentity ?: when (spaceAccessType) {
                SpaceAccessType.PRIVATE -> stringResource(LocalizationR.string.space_header_private_channel)
                else -> stringResource(LocalizationR.string.space_header_group_channel)
            }
            Text(
                text = subtitle,
                style = Caption1Regular,
                color = colorResource(id = R.color.text_secondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SpaceProfileHeaderPreview() {
    DefaultTheme {
        SpaceProfileHeader(
            spaceIcon = SpaceIconView.DataSpace.Placeholder(
                color = SystemColor.SKY,
                name = "Anytype"
            ),
            spaceName = "Anytype",
            globalName = "anytype.any",
            identity = "0x1234567890abcdef",
            spaceAccessType = SpaceAccessType.PRIVATE
        )
    }
}

@Preview(name = "Private Channel", showBackground = true)
@Composable
private fun SpaceProfileHeaderPrivatePreview() {
    DefaultTheme {
        SpaceProfileHeader(
            spaceIcon = SpaceIconView.DataSpace.Placeholder(
                color = SystemColor.TEAL,
                name = "My Space"
            ),
            spaceName = "My Space",
            globalName = null,
            identity = null,
            spaceAccessType = SpaceAccessType.PRIVATE
        )
    }
}

@Preview(name = "Group Channel", showBackground = true)
@Composable
private fun SpaceProfileHeaderGroupPreview() {
    DefaultTheme {
        SpaceProfileHeader(
            spaceIcon = SpaceIconView.DataSpace.Placeholder(
                color = SystemColor.PINK,
                name = "Team"
            ),
            spaceName = "Team Space",
            globalName = null,
            identity = null,
            spaceAccessType = SpaceAccessType.SHARED
        )
    }
}
