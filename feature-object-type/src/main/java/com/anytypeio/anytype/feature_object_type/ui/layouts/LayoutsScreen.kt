package com.anytypeio.anytype.feature_object_type.ui.layouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.common.DefaultPreviews
import com.anytypeio.anytype.core_ui.foundation.Dragger
import com.anytypeio.anytype.core_ui.foundation.noRippleThrottledClickable
import com.anytypeio.anytype.core_ui.views.AvatarTitle
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.fontInterRegular
import com.anytypeio.anytype.feature_object_type.R
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeLayoutsScreen(
    modifier: Modifier,
    uiState: UiLayoutTypeState.Visible,
    onTypeEvent: (TypeEvent) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        modifier = modifier,
        dragHandle = {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Dragger()
                Spacer(modifier = Modifier.height(6.dp))
            }
        },
        scrimColor = colorResource(id = R.color.modal_screen_outside_background),
        containerColor = colorResource(id = R.color.background_secondary),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetState = bottomSheetState,
        onDismissRequest = {
            onTypeEvent(TypeEvent.OnLayoutTypeDismiss)
        }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(id = R.string.layout_type),
            style = Title1,
            color = colorResource(id = R.color.text_primary)
        )
        Spacer(modifier = Modifier.height(20.dp))
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 12.dp,
                alignment = Alignment.End
            ),
        ) {
            items(
                count = uiState.layouts.size,
                key = { index -> uiState.layouts[index].code },
                itemContent = {
                    val item = uiState.layouts[it]
                    val (borderWidth, borderColor) = if (item.code == uiState.selectedLayout?.code) {
                        2.dp to colorResource(id = R.color.palette_system_amber_50)
                    } else {
                        1.dp to colorResource(id = R.color.shape_secondary)
                    }
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TemplateItemContent(
                            modifier = Modifier
                                .width(120.dp)
                                .height(224.dp)
                                .border(
                                    width = borderWidth,
                                    color = borderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp)
                                .noRippleThrottledClickable{
                                    onTypeEvent(TypeEvent.OnLayoutTypeItemClick(item))
                                    onTypeEvent(TypeEvent.OnLayoutTypeDismiss)
                                },
                            item = item
                        )
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = item.name.substring(0, 1).uppercase()
                                    + item.name.substring(1)
                                .toLowerCase(Locale.current),
                            style = TextStyle(
                                fontFamily = fontInterRegular,
                                fontWeight = FontWeight.W500,
                                fontSize = 13.sp,
                                letterSpacing = (-0.024).em
                            ),
                            color = colorResource(id = R.color.text_primary)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            )
        }
    }
}

@Composable
private fun TemplateItemContent(
    modifier: Modifier,
    item: ObjectType.Layout
) {
    when (item) {
        ObjectType.Layout.BASIC -> {
            Column(
                modifier = modifier
            ) {
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            shape = RoundedCornerShape(5.dp),
                            color = colorResource(id = R.color.shape_tertiary)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.wrapContentSize(),
                        painter = painterResource(R.drawable.ic_type_layout_basic_icon),
                        contentDescription = "Basic layout icon"
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(8.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicBlocks()
            }
        }

        ObjectType.Layout.PROFILE -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            shape = CircleShape,
                            color = colorResource(id = R.color.shape_tertiary)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.wrapContentSize(),
                        text = "N",
                        style = AvatarTitle.copy(
                            fontSize = 24.sp
                        ),
                        color = colorResource(id = R.color.glyph_active),
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .height(8.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicBlocks()
            }
        }

        ObjectType.Layout.TODO -> {
            Column(
                modifier = modifier
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Image(
                    modifier = Modifier.wrapContentSize(),
                    painter = painterResource(R.drawable.ic_type_layout_todo_icon),
                    contentDescription = "Todo layout icon"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicBlocks()
            }
        }

        ObjectType.Layout.NOTE -> {
            Column(
                modifier = modifier
            ) {
                Spacer(modifier = Modifier.height(46.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .height(4.dp)
                        .background(
                            color = colorResource(id = R.color.shape_secondary),
                            shape = RoundedCornerShape(size = 1.dp)
                        )
                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicBlocks()
            }
        }

        else -> {
            //do nothing
        }
    }
}

@Composable
private fun ColumnScope.BasicBlocks() {
    repeat(3) {
        Box(
            modifier = Modifier
                .width(88.dp)
                .height(6.dp)
                .background(
                    color = colorResource(id = R.color.shape_secondary),
                    shape = RoundedCornerShape(size = 1.dp)
                )
        )
        Spacer(modifier = Modifier.height(6.dp))
    }
    Box(
        modifier = Modifier
            .width(64.dp)
            .height(6.dp)
            .background(
                color = colorResource(id = R.color.shape_secondary),
                shape = RoundedCornerShape(size = 1.dp)
            )
    )
}

@DefaultPreviews
@Composable
fun TypeLayoutsScreenPreview() {
    TypeLayoutsScreen(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        uiState = UiLayoutTypeState.Visible(
            layouts = listOf(
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.BASIC,
                ObjectType.Layout.TODO,
                ObjectType.Layout.NOTE
            ),
            selectedLayout = ObjectType.Layout.BASIC
        ),
        onTypeEvent = {})
}