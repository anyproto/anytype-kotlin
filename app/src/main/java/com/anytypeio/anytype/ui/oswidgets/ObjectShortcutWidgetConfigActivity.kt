package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.BodyRegular
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.ListWidgetObjectIcon
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.ui.OsObjectShortcutWidgetUpdater
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetIconCache
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Configuration Activity for the Object Shortcut widget.
 * Two-step flow: Select Space -> Select Object (with search)
 */
class ObjectShortcutWidgetConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var urlBuilder: UrlBuilder

    @Inject
    lateinit var searchObjects: SearchObjects

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var selectedSpace: ObjectWrapper.SpaceView? = null

    // UI State
    private var screenState by mutableStateOf<ScreenState>(ScreenState.SpaceSelection)
    private var objects by mutableStateOf<List<ObjectWrapper.Basic>>(emptyList())
    private var isLoading by mutableStateOf(false)
    private var searchQuery by mutableStateOf("")

    private var searchJob: Job? = null

    sealed class ScreenState {
        data object SpaceSelection : ScreenState()
        data object ObjectSelection : ScreenState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        componentManager().objectShortcutWidgetConfigComponent.get().inject(this)

        setContentView(
            ComposeView(this).apply {
                setContent {
                    WidgetConfigTheme {
                        when (screenState) {
                            ScreenState.SpaceSelection -> {
                                SpaceSelectionScreen(
                                    spaceViews = spaceViews,
                                    urlBuilder = urlBuilder,
                                    onSpaceSelected = { space ->
                                        selectedSpace = space
                                        screenState = ScreenState.ObjectSelection
                                        searchObjectsInSpace(space.targetSpaceId.orEmpty(), "")
                                    },
                                    onCancel = { finish() }
                                )
                            }
                            ScreenState.ObjectSelection -> {
                                ObjectSelectionScreen(
                                    spaceName = selectedSpace?.name.orEmpty(),
                                    objects = objects,
                                    isLoading = isLoading,
                                    searchQuery = searchQuery,
                                    urlBuilder = urlBuilder,
                                    onSearchQueryChanged = { query ->
                                        searchQuery = query
                                        searchObjectsInSpace(
                                            selectedSpace?.targetSpaceId.orEmpty(),
                                            query
                                        )
                                    },
                                    onObjectSelected = { obj ->
                                        completeConfiguration(obj)
                                    },
                                    onBack = {
                                        screenState = ScreenState.SpaceSelection
                                        objects = emptyList()
                                        searchQuery = ""
                                    }
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    private fun searchObjectsInSpace(spaceId: String, query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            isLoading = true
            delay(300) // Debounce

            try {
                val filters = buildList {
                    // Exclude deleted
                    add(DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude archived
                    add(DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude templates
                    add(DVFilter(
                        relation = Relations.TYPE_UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeUniqueKeys.TEMPLATE
                    ))
                    // Include common layouts
                    add(DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.IN,
                        value = listOf(
                            ObjectType.Layout.BASIC.code.toDouble(),
                            ObjectType.Layout.PROFILE.code.toDouble(),
                            ObjectType.Layout.TODO.code.toDouble(),
                            ObjectType.Layout.NOTE.code.toDouble(),
                            ObjectType.Layout.BOOKMARK.code.toDouble(),
                            ObjectType.Layout.SET.code.toDouble(),
                            ObjectType.Layout.COLLECTION.code.toDouble(),
                            ObjectType.Layout.IMAGE.code.toDouble(),
                            ObjectType.Layout.FILE.code.toDouble(),
                            ObjectType.Layout.PDF.code.toDouble(),
                            ObjectType.Layout.AUDIO.code.toDouble(),
                            ObjectType.Layout.VIDEO.code.toDouble(),
                            ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                        )
                    ))
                }

                val sorts = listOf(
                    DVSort(
                        relationKey = Relations.LAST_OPENED_DATE,
                        type = DVSortType.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                )

                val params = SearchObjects.Params(
                    space = SpaceId(spaceId),
                    filters = filters,
                    sorts = sorts,
                    fulltext = query,
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = 100
                )

                val result = searchObjects(params)
                result.process(
                    failure = { error ->
                        Timber.e(error, "Error searching objects")
                        objects = emptyList()
                    },
                    success = { foundObjects ->
                        objects = foundObjects
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error searching objects")
                objects = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    private fun completeConfiguration(obj: ObjectWrapper.Basic) {
        val space = selectedSpace ?: return
        
        lifecycleScope.launch {
            try {
                // Cache the icon image if available
                val iconCache = OsWidgetIconCache(applicationContext)
                val cachedIconPath = obj.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    iconCache.cacheShortcutIcon(
                        url = iconUrl,
                        widgetId = appWidgetId,
                        prefix = OsWidgetIconCache.PREFIX_OBJECT
                    )
                }

                val config = OsWidgetObjectShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    objectId = obj.id,
                    objectName = obj.name.orEmpty(),
                    objectIconEmoji = obj.iconEmoji,
                    objectIconImage = obj.iconImage,
                    objectLayout = obj.layout?.code,
                    cachedIconPath = cachedIconPath
                )

                OsWidgetsDataStore(applicationContext).saveObjectShortcutConfig(config)
                OsObjectShortcutWidgetUpdater.update(applicationContext, appWidgetId)

                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                Toast.makeText(this@ObjectShortcutWidgetConfigActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().objectShortcutWidgetConfigComponent.release()
    }
}

@Composable
private fun WidgetConfigTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color(0xFF1F1E1D),
            surface = Color(0xFF2B2A29),
            onBackground = Color.White,
            onSurface = Color.White,
            primary = Color(0xFFFFC940)
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceSelectionScreen(
    spaceViews: SpaceViewSubscriptionContainer,
    urlBuilder: UrlBuilder,
    onSpaceSelected: (ObjectWrapper.SpaceView) -> Unit,
    onCancel: () -> Unit
) {
    val spaces = remember { spaceViews.get() }
    val sortedSpaces = remember(spaces) {
        spaces
            .filter { it.isActive && it.spaceUxType != SpaceUxType.CHAT && it.spaceUxType != SpaceUxType.ONE_TO_ONE }
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.select_space),
                        style = BodyBold,
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_24),
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.background_primary)
                )
            )
        },
        containerColor = colorResource(id = R.color.background_primary)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (sortedSpaces.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_spaces_available),
                        color = colorResource(id = R.color.text_secondary)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = sortedSpaces, key = { it.id }) { space ->
                        SpaceGridItem(
                            space = space,
                            urlBuilder = urlBuilder,
                            onClick = { onSpaceSelected(space) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpaceGridItem(
    space: ObjectWrapper.SpaceView,
    urlBuilder: UrlBuilder,
    onClick: () -> Unit
) {
    val icon = space.toSpaceIconView(urlBuilder)
    
    Column(
        modifier = Modifier.noRippleClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.height(86.dp).width(92.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SpaceIconView(icon = icon, mainSize = 80.dp, onSpaceIconClick = onClick)
        }
        Text(
            text = space.name.orEmpty().ifEmpty { stringResource(R.string.untitled) },
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth().height(30.dp).padding(horizontal = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ObjectSelectionScreen(
    spaceName: String,
    objects: List<ObjectWrapper.Basic>,
    isLoading: Boolean,
    searchQuery: String,
    urlBuilder: UrlBuilder,
    onSearchQueryChanged: (String) -> Unit,
    onObjectSelected: (ObjectWrapper.Basic) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.select_objects),
                        style = BodyBold,
                        color = colorResource(id = R.color.text_primary)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_24),
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.glyph_active)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.background_primary)
                )
            )
        },
        containerColor = colorResource(id = R.color.background_primary)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search field
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.search),
                        color = colorResource(id = R.color.text_tertiary)
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colorResource(id = R.color.shape_tertiary),
                    textColor = colorResource(id = R.color.text_primary),
                    cursorColor = colorResource(id = R.color.palette_system_amber_50),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            // Space name indicator
            Text(
                text = spaceName,
                style = Relations3,
                color = colorResource(id = R.color.text_secondary),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // Object list
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(id = R.color.palette_system_amber_50)
                    )
                } else if (objects.isEmpty()) {
                    Text(
                        text = stringResource(R.string.nothing_found),
                        color = colorResource(id = R.color.text_secondary),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = objects, key = { it.id }) { obj ->
                            ObjectListItem(
                                obj = obj,
                                urlBuilder = urlBuilder,
                                onClick = { onObjectSelected(obj) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ObjectListItem(
    obj: ObjectWrapper.Basic,
    urlBuilder: UrlBuilder,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(colorResource(id = R.color.shape_tertiary))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Object icon
        val icon = obj.toObjectIcon(urlBuilder)
        ListWidgetObjectIcon(
            icon = icon,
            modifier = Modifier.size(24.dp),
            iconSize = 24.dp,
            iconWithoutBackgroundMaxSize = 24.dp
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Object name
        Text(
            text = obj.name.orEmpty().ifEmpty { stringResource(R.string.untitled) },
            style = BodyRegular,
            color = colorResource(id = R.color.text_primary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun ObjectWrapper.SpaceView.toSpaceIconView(urlBuilder: UrlBuilder): SpaceIconView {
    val isChat = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
    val color = iconOption?.toInt()?.let { SystemColor.color(it) } ?: SystemColor.SKY
    val imageUrl = iconImage?.takeIf { it.isNotEmpty() }?.let { urlBuilder.medium(it) }
    
    return if (imageUrl != null) {
        if (isChat) SpaceIconView.ChatSpace.Image(url = imageUrl, color = color)
        else SpaceIconView.DataSpace.Image(url = imageUrl, color = color)
    } else {
        if (isChat) SpaceIconView.ChatSpace.Placeholder(color = color, name = name.orEmpty())
        else SpaceIconView.DataSpace.Placeholder(color = color, name = name.orEmpty())
    }
}

private fun ObjectWrapper.Basic.toObjectIcon(urlBuilder: UrlBuilder): ObjectIcon {
    return when {
        !iconEmoji.isNullOrEmpty() -> ObjectIcon.Basic.Emoji(unicode = iconEmoji!!)
        !iconImage.isNullOrEmpty() -> ObjectIcon.Basic.Image(hash = urlBuilder.thumbnail(iconImage!!))
        else -> ObjectIcon.Basic.Emoji(unicode = "")
    }
}
