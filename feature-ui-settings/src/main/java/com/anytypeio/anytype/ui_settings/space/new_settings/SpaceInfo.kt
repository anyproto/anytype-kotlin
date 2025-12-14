package com.anytypeio.anytype.ui_settings.space.new_settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.datasource.LoremIpsum
import androidx.compose.ui.unit.dp

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Header
import com.anytypeio.anytype.core_ui.views.BodyCalloutRegular
import com.anytypeio.anytype.core_ui.views.PreviewTitle2Regular
import com.anytypeio.anytype.core_utils.clipboard.copyPlainTextToClipboard
import com.anytypeio.anytype.core_utils.const.DateConst
import com.anytypeio.anytype.core_utils.ext.formatTimeInMillis
import com.anytypeio.anytype.presentation.spaces.SpaceTechInfo
import com.anytypeio.anytype.ui_settings.R

@Composable
fun SpaceInfoScreen(
    spaceTechInfo: SpaceTechInfo,
    isDebugBuild: Boolean = false,
    onTitleClick: () -> Unit = {},
    onDebugClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Header(
            text = stringResource(R.string.space_settings_space_info_button),
            modifier = Modifier.clickable { onTitleClick() }
        )
        SpaceInfoItem(
            name = stringResource(R.string.space_id),
            value = spaceTechInfo.spaceId.id,
            onClick = {
                context.copyPlainTextToClipboard(
                    plainText = spaceTechInfo.spaceId.id,
                    label = "Space ID",
                    successToast = context.getString(R.string.space_id_copied_toast_msg)
                )
            }
        )
        // Hide 'Created by' and 'Creation date' for ONE_TO_ONE spaces
        if (!spaceTechInfo.isOneToOne) {
            SpaceInfoItem(
                name = stringResource(R.string.created_by),
                value = spaceTechInfo.createdBy.ifEmpty {
                    stringResource(id = R.string.unknown)
                },
                onClick = {
                    context.copyPlainTextToClipboard(
                        plainText = spaceTechInfo.createdBy,
                        label = "Created-by ID",
                        successToast = context.getString(R.string.created_by_id_copied_toast_msg)
                    )
                }
            )
        }
        SpaceInfoItem(
            name = stringResource(R.string.network_id),
            value = spaceTechInfo.networkId.ifEmpty {
                stringResource(id = R.string.unknown)
            },
            onClick = {
                context.copyPlainTextToClipboard(
                    plainText = spaceTechInfo.networkId,
                    label = "Network ID",
                    successToast = context.getString(R.string.network_id_copied_toast_msg)
                )
            }
        )
        if (!spaceTechInfo.isOneToOne) {
            SpaceInfoItem(
                name = stringResource(R.string.creation_date),
                value = spaceTechInfo.creationDateInMillis?.formatTimeInMillis(
                    DateConst.DEFAULT_DATE_FORMAT
                ) ?: stringResource(id = R.string.unknown),
                onClick = {
                    // Do nothing.
                }
            )
        }

        // Device Token - shown if isDebugBuild or if title clicked 5+ times
        val deviceToken = spaceTechInfo.deviceToken
        if ((isDebugBuild || spaceTechInfo.isDebugVisible) && deviceToken != null) {
            SpaceInfoItem(
                name = "Device Token",
                value = deviceToken,
                onClick = {
                    context.copyPlainTextToClipboard(
                        plainText = deviceToken,
                        label = "Device Token",
                        successToast = "Device Token copied to clipboard"
                    )
                }
            )
        }
        
        // Debug button - shown if isDebugBuild or if title clicked 5+ times
        if (isDebugBuild || spaceTechInfo.isDebugVisible) {
            SpaceInfoItem(
                name = "Debug",
                value = "Open debug screen",
                onClick = onDebugClick
            )
        }
    }
}

@Composable
internal fun SpaceInfoItem(
    name: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .padding(
                vertical = 12.dp,
                horizontal = 20.dp
            )
    ) {
        Text(
            text = name,
            style = BodyCalloutRegular,
            color = colorResource(R.color.text_secondary),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = PreviewTitle2Regular,
            color = colorResource(R.color.text_primary)
        )
    }
}

@DefaultPreviews
@Composable
fun SpaceInfoScreenPreview() {
    SpaceInfoScreen(
        spaceTechInfo = SpaceTechInfo(
            spaceId = SpaceId(LoremIpsum(words = 10).toString()),
            createdBy = "Walter",
            networkId = LoremIpsum(words = 10).toString(),
            creationDateInMillis = 21313L,
            isDebugVisible = true,
            deviceToken = "FCM_TOKEN_1234567890ABCDEF_EXAMPLE_TOKEN"
        ),
        isDebugBuild = true
    )
}
