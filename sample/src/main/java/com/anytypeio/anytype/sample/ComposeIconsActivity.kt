package com.anytypeio.anytype.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.core_ui.common.bottomBorder
import com.anytypeio.anytype.core_ui.views.Title1
import com.anytypeio.anytype.core_ui.views.Title2
import com.anytypeio.anytype.core_ui.views.Title3
import com.anytypeio.anytype.core_ui.views.animations.LoadingIndicator
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.sample.icons.type.AllStatesScreen
import com.anytypeio.anytype.sample.icons.type.BasicIconsScreen
import com.anytypeio.anytype.sample.icons.type.FileIconsAllMimeTypesScreen
import com.anytypeio.anytype.sample.icons.type.FileIconsScreen
import com.anytypeio.anytype.sample.icons.type.TypeIconsDeletedScreen
import com.anytypeio.anytype.sample.icons.type.TypeIconsEmojiScreen
import com.anytypeio.anytype.sample.icons.type.TypeIconsFallbackScreen
import com.anytypeio.anytype.sample.icons.type.TypeIconsScreen

class ComposeIconsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    containerColor = colorResource(id = R.color.background_primary),
                    topBar = {
                        LazyRow(
                            modifier = Modifier
                                .height(72.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                OutlinedButton(
                                    onClick = {
                                        val context = this@ComposeIconsActivity
                                        context.startActivity(
                                            android.content.Intent(context, XmlIconsActivity::class.java)
                                        )
                                    },
                                    modifier = Modifier,
                                    content = {
                                        Text(
                                            text = "XML Icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("AllStatesScreen")
                                    },
                                    modifier = Modifier,
                                    content = {
                                        Text(
                                            text = "All States", style = Title3,
                                            color = colorResource(id = R.color.text_primary)
                                        )
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("LoadingIconsScreen")
                                    },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "Loading Circle icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = {
                                        navController.navigate("LoadingRoundedIconsScreen")
                                    },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "Loading Rounded icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate("EmojiIconsScreen") },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "Emoji icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = {},
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "Image icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate("FileIconsScreen") },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "File icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate("FileIconsAllMimeTypesScreen") },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "All Mime-Type File icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }

                            item {
                                OutlinedButton(
                                    onClick = { navController.navigate("TypeIconsScreen") },
                                    modifier = Modifier,
                                    content = {
                                        Text(text = "Type icons", style = Title3,
                                            color = colorResource(id = R.color.text_primary))
                                    }
                                )
                            }
                        }
                    },
                    content = { paddingValues ->
                        NavHost(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues),
                            navController = navController,
                            startDestination = "TypeIconsScreen"
                        ) {
                            composable("ObjectIconAvatarScreen") {
                                ObjectIconAvatarScreen()
                            }
                            composable("LoadingIconsScreen") {
                                LoadingIconsScreen()
                            }
                            composable("LoadingRoundedIconsScreen") {
                                LoadingRoundedIconsScreen()
                            }
                            composable("EmojiIconsScreen") {
                                BasicIconsScreen()
                            }
                            composable("TypeIconsScreen") {
                                TypeIconsScreen()
                            }
                            composable("TypeIconsFallbackScreen") {
                                TypeIconsFallbackScreen()
                            }
                            composable("TypeIconsEmojiScreen") {
                                TypeIconsEmojiScreen()
                            }
                            composable("TypeIconsDeletedScreen") {
                                TypeIconsDeletedScreen()
                            }
                            composable("AllStatesScreen") {
                                AllStatesScreen()
                            }
                            composable("FileIconsScreen") {
                                FileIconsScreen()
                            }
                            composable("FileIconsAllMimeTypesScreen") {
                                FileIconsAllMimeTypesScreen()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ObjectIconAvatarScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Avatar Icons",
            style = Title1
        )
        Spacer(
            modifier = Modifier.height(32.dp)
        )
        val basicModifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .bottomBorder()

    }
}

@Composable
private fun LoadingIconsScreen() {
    // Define your container sizes and corresponding labels.
    val items = listOf(
        Pair(120.dp, "Loading, size 120"),
        Pair(96.dp, "Loading, size 96"),
        Pair(80.dp, "Loading, size 80"),
        Pair(64.dp, "Loading, size 64"),
        Pair(48.dp, "Loading, size 48"),
        Pair(40.dp, "Loading, size 40"),
        Pair(32.dp, "Loading, size 32"),
        Pair(20.dp, "Loading, size 20"),
        Pair(18.dp, "Loading, size 18"),
        Pair(16.dp, "Loading, size 16")
    )

    // This is your basic modifier for the rows.
    val basicModifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Text(
                text = "Loading Icons",
                style = Title1,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // List items
        items(
            count = items.size,
            key = { index -> items[index].second }
        ) { index ->
            val (containerSize, label) = items[index]
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator(
                    containerModifier = Modifier.padding(vertical = 16.dp),
                    containerSize = containerSize,
                )

                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = label,
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LoadingRoundedIconsScreen() {
    // Define your container sizes and corresponding labels.
    val items = listOf(
        Pair(120.dp, "Loading, size 120"),
        Pair(96.dp, "Loading, size 96"),
        Pair(80.dp, "Loading, size 80"),
        Pair(64.dp, "Loading, size 64"),
        Pair(48.dp, "Loading, size 48"),
        Pair(40.dp, "Loading, size 40"),
        Pair(32.dp, "Loading, size 32"),
        Pair(20.dp, "Loading, size 20"),
        Pair(18.dp, "Loading, size 18"),
        Pair(16.dp, "Loading, size 16")
    )

    // This is your basic modifier for the rows.
    val basicModifier = Modifier
        .wrapContentHeight()
        .fillMaxWidth()
        .padding(horizontal = 16.dp)

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(top = 32.dp, bottom = 32.dp)
    ) {
        // Header
        item {
            Text(
                text = "Loading Icons",
                style = Title1,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        // List items
        items(
            count = items.size,
            key = { index -> items[index].second }
        ) { index ->
            val (containerSize, label) = items[index]
            Row(
                modifier = basicModifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoadingIndicator(
                    containerModifier = Modifier.padding(vertical = 16.dp),
                    containerSize = containerSize,
                    withCircleBackground = false
                )

                Text(
                    modifier = Modifier.padding(start = 12.dp),
                    text = label,
                    style = Title2,
                    color = colorResource(id = R.color.text_primary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmojiIconsScreen() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Emoji Icons",
            style = Title1
        )
        Spacer(
            modifier = Modifier.height(32.dp)
        )
        val basicModifier = Modifier
            .height(72.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .bottomBorder()
        Row(
            modifier = basicModifier,
            verticalAlignment = CenterVertically
        ) {
            ListWidgetObjectIcon(
                icon = ObjectIcon.Basic.Emoji(
                    unicode = "\uD83D\uDE00"
                ),
                modifier = Modifier,
                iconSize = 48.dp
            )

            val name = "Basic.Emoji, iconSize 48"

            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = name,
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = basicModifier,
            verticalAlignment = CenterVertically
        ) {
            ListWidgetObjectIcon(
                icon = ObjectIcon.Basic.Emoji(
                    "😀1"
                ),
                modifier = Modifier,
                iconSize = 48.dp
            )

            val name = "Basic.Emoji, fallback iconSize 48"

            Text(
                modifier = Modifier.padding(start = 12.dp),
                text = name,
                style = Title2,
                color = colorResource(id = R.color.text_primary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}