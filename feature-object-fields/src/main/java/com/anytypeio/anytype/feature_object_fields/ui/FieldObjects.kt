package com.anytypeio.anytype.feature_object_fields.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_ui.views.Relations1
import com.anytypeio.anytype.core_ui.views.Relations2
import com.anytypeio.anytype.feature_object_fields.R

// Модель данных
data class FieldObject(val title: String, val items: List<Item1>)
data class Item1(val title: String, val icon: String? = null) // Здесь icon – URL в виде String

@Composable
fun FieldObjects() {

}

// Вспомогательная функция для отображения одного item: иконка (если есть) + текст.
@Composable
fun ItemView(modifier: Modifier, item: Item1) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item.icon?.let { iconUrl ->
            Image(
                painter = painterResource(R.drawable.ic_search_18),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = item.title,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Компоновщик, который отображает строку, состоящую из основного текста и суффикса.
 * При этом, если основной текст достаточно короткий – суффикс (например, "+n")
 * будет сразу после него, а если длинный – текст обрежется (с Ellipsis), чтобы оставить место для суффикса.
 */
@Composable
fun TextWithSuffix(
    text: String,
    suffix: String,
    textStyle: TextStyle,
    countStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    SubcomposeLayout(modifier = modifier) { constraints ->
        val suffixConstraints = constraints.copy(minWidth = 0, maxWidth = Constraints.Infinity)
        val suffixPlaceable = subcompose("suffix") {
            Box(
                modifier = Modifier.background(
                    color = colorResource(R.color.shape_tertiary),
                    shape = RoundedCornerShape(4.dp)
                )
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = suffix,
                    style = countStyle,
                    maxLines = 1,
                )
            }
        }.first().measure(suffixConstraints)

        // Доступное место для основного текста – это общая ширина минус ширина суффикса.
        val availableWidthForText = (constraints.maxWidth - suffixPlaceable.width).coerceAtLeast(0)
        val textConstraints = constraints.copy(minWidth = 0, maxWidth = availableWidthForText)
        val textPlaceable = subcompose("text") {
            Text(
                text = text,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }.first().measure(textConstraints)

        // Если ограничения по ширине заданы (например, при использовании weight), то используем всю доступную ширину.
        val finalWidth =
            if (constraints.hasBoundedWidth) constraints.maxWidth else (textPlaceable.width + suffixPlaceable.width)
        val height = maxOf(textPlaceable.height, suffixPlaceable.height)
        layout(finalWidth, height) {
            // Выравниваем контент по левому краю.
            textPlaceable.placeRelative(0, 0)
            val offsetYPx = with(density) { 0.5.dp.roundToPx() }
            val offsetXPx = with(density) { 8.dp.roundToPx() }
            suffixPlaceable.placeRelative(textPlaceable.width + offsetXPx, offsetYPx)
        }
    }
}

/**
 * Основной компоновщик для FieldObject.
 * Внешний контейнер отступает от краёв экрана на 16.dp, а контент внутри также имеет горизонтальные отступы 16.dp.
 *
 * Первый элемент отображается в Box с ограничением ширины до половины экрана.
 *
 * Если второй элемент существует:
 *  - Если элементов всего два – отображаем его стандартно.
 *  - Если больше двух – отображаем второй элемент, где после его текста сразу идет суффикс "+n"
 *    (n = общее количество элементов минус два). Если текст второй записи длинный, он обрезается,
 *    чтобы суффикс всегда был виден.
 */
@Composable
fun FieldObjectRow(fieldObject: FieldObject) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val halfScreenWidth = screenWidth / 2 - 32.dp

    val defaultModifier = Modifier
        .fillMaxWidth()
        .border(
            width = 1.dp,
            color = colorResource(id = R.color.shape_secondary),
            shape = RoundedCornerShape(12.dp)
        )
        .padding(vertical = 16.dp)
        .padding(horizontal = 16.dp)

    Column(
        modifier = defaultModifier
    ) {
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = fieldObject.title,
            style = Relations1,
            color = colorResource(id = R.color.text_secondary)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
        ) {
            // Первый элемент (если есть)
            if (fieldObject.items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .widthIn(max = halfScreenWidth)
                ) {
                    ItemView(
                        modifier = Modifier.height(22.dp),
                        item = fieldObject.items.first()
                    )
                }
            }
            // Второй элемент (если есть)
            if (fieldObject.items.size > 1) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.widthIn(max = halfScreenWidth)) {
                    if (fieldObject.items.size == 2) {
                        ItemView(
                            modifier = Modifier.height(22.dp),
                            item = fieldObject.items[1]
                        )
                    } else {
                        // Если элементов больше двух, отображаем второй элемент с суффиксом "+n"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().height(22.dp)
                        ) {
                            // Если иконка задана, показываем её
                            fieldObject.items[1].icon?.let { iconUrl ->
                                Image(
                                    painter = painterResource(R.drawable.ic_search_18),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            // Основной текст со встроенным суффиксом, занимающий оставшееся пространство
                            TextWithSuffix(
                                text = fieldObject.items[1].title,
                                suffix = "+${fieldObject.items.size - 2}",
                                textStyle = Relations1,
                                countStyle = Relations2.copy(
                                    color = colorResource(id = R.color.text_secondary)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_4_XL)
@Composable
fun TwoItemsLongLongPreview() {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //short - short
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Short - short",
                    items = listOf(
                        Item1(title = "First title", icon = "https://via.placeholder.com/18"),
                        Item1(
                            title = "Second title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //short - long
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Short - long",
                    items = listOf(
                        Item1(title = "First title", icon = "https://via.placeholder.com/18"),
                        Item1(
                            title = "Second title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //long - short
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Long - short",
                    items = listOf(
                        Item1(
                            title = "First title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Second title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //long - long
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Long - long",
                    items = listOf(
                        Item1(
                            title = "First title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Second title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //short - short +n
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Short - short +n",
                    items = listOf(
                        Item1(title = "First title", icon = "https://via.placeholder.com/18"),
                        Item1(
                            title = "Second title",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Third title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //short - long +n
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Short - long +n",
                    items = listOf(
                        Item1(title = "First title", icon = "https://via.placeholder.com/18"),
                        Item1(
                            title = "Second title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Third title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //long - short +n
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Long - short +n",
                    items = listOf(
                        Item1(
                            title = "First title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Second title",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Third title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        //long - long +n
        item {
            FieldObjectRow(
                fieldObject = FieldObject(
                    title = "Long - long +n",
                    items = listOf(
                        Item1(
                            title = "First title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Second title, very very long one",
                            icon = "https://via.placeholder.com/18"
                        ),
                        Item1(
                            title = "Third title",
                            icon = "https://via.placeholder.com/18"
                        ),
                    )
                )
            )
        }
        item {
            FieldEmpty(item = Item(format = RelationFormat.LONG_TEXT))
        }
    }
}