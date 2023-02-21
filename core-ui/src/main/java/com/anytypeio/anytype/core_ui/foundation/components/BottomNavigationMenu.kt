package com.anytypeio.anytype.core_ui.foundation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.components.BottomNavigationDefaults.Height
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable


@Composable
fun BottomNavigationMenu(
    modifier: Modifier = Modifier,
    backClick: () -> Unit = {},
    homeClick: () -> Unit = {},
    searchClick: () -> Unit = {},
    addDocClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .height(Height)
            .fillMaxWidth()
            .background(color = colorResource(id = R.color.background_primary))
            /**
             * Workaround for clicks through the bottom navigation menu.
             */
            .noRippleClickable {  },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MenuItem(BottomNavigationItem.BACK.res, onClick = backClick)
        MenuItem(BottomNavigationItem.HOME.res, onClick = homeClick)
        MenuItem(BottomNavigationItem.SEARCH.res, onClick = searchClick)
        MenuItem(BottomNavigationItem.ADD_DOC.res, onClick = addDocClick)
    }
}

@Composable
private fun MenuItem(
    @DrawableRes res: Int,
    onClick: () -> Unit = {}
) {
    Image(
        painter = painterResource(id = res),
        contentDescription = "",
        modifier = Modifier.noRippleClickable {
            onClick.invoke()
        }
    )
}


private enum class BottomNavigationItem(@DrawableRes val res: Int) {
    BACK(R.drawable.ic_main_toolbar_back),
    HOME(R.drawable.ic_main_toolbar_home),
    SEARCH(R.drawable.ic_main_toolbar_search),
    ADD_DOC(R.drawable.ic_page_toolbar_add_doc)
}

@Immutable
private object BottomNavigationDefaults {
    val Height = 48.dp
}