package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidgetUpdater
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import androidx.lifecycle.lifecycleScope
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Configuration Activity for the Create Object widget.
 * Two-step flow: Select Space -> Select Type
 */
class CreateObjectWidgetConfigActivity : ComponentActivity() {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var blockRepository: BlockRepository

    @Inject
    lateinit var dispatchers: AppCoroutineDispatchers

    @Inject
    lateinit var configStorage: ConfigStorage

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the result to CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED)

        // Get the appWidgetId from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Inject dependencies
        componentManager().createObjectWidgetConfigComponent.get().inject(this)

        setContent {
            WidgetConfigTheme {
                ConfigScreen(
                    spaceViews = spaceViews,
                    blockRepository = blockRepository,
                    dispatchers = dispatchers,
                    configStorage = configStorage,
                    urlBuilder = urlBuilder,
                    appWidgetId = appWidgetId,
                    onConfigComplete = { config ->
                        completeConfiguration(config)
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }

    private fun completeConfiguration(config: OsWidgetCreateObjectEntity) {
        lifecycleScope.launch {
            try {
                // Save the configuration
                OsWidgetsDataStore(applicationContext).saveCreateObjectConfig(config)

                // Trigger widget update so it picks up the saved config
                OsCreateObjectWidgetUpdater.update(applicationContext, appWidgetId)

                // Return success and finish
                setResult(
                    Activity.RESULT_OK,
                    Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                )
                finish()
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                Toast.makeText(this@CreateObjectWidgetConfigActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentManager().createObjectWidgetConfigComponent.release()
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

private sealed class ConfigStep {
    object SelectSpace : ConfigStep()
    data class SelectType(
        val space: ObjectWrapper.SpaceView
    ) : ConfigStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    spaceViews: SpaceViewSubscriptionContainer,
    blockRepository: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
    configStorage: ConfigStorage,
    urlBuilder: UrlBuilder,
    appWidgetId: Int,
    onConfigComplete: (OsWidgetCreateObjectEntity) -> Unit,
    onCancel: () -> Unit
) {
    var currentStep by remember { mutableStateOf<ConfigStep>(ConfigStep.SelectSpace) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentStep) {
                            ConfigStep.SelectSpace -> "Select Space"
                            is ConfigStep.SelectType -> "Select Type"
                        },
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        when (currentStep) {
                            ConfigStep.SelectSpace -> onCancel()
                            is ConfigStep.SelectType -> currentStep = ConfigStep.SelectSpace
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_24),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1F1E1D)
                )
            )
        },
        containerColor = Color(0xFF1F1E1D)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val step = currentStep) {
                ConfigStep.SelectSpace -> {
                    SpaceSelectionScreen(
                        spaceViews = spaceViews,
                        urlBuilder = urlBuilder,
                        onSpaceSelected = { space ->
                            currentStep = ConfigStep.SelectType(space)
                        }
                    )
                }
                is ConfigStep.SelectType -> {
                    TypeSelectionScreen(
                        space = step.space,
                        blockRepository = blockRepository,
                        dispatchers = dispatchers,
                        configStorage = configStorage,
                        appWidgetId = appWidgetId,
                        onTypeSelected = { type ->
                            val config = OsWidgetCreateObjectEntity(
                                appWidgetId = appWidgetId,
                                spaceId = step.space.targetSpaceId.orEmpty(),
                                typeKey = type.uniqueKey.orEmpty(),
                                typeName = type.name.orEmpty(),
                                typeIconEmoji = type.iconEmoji,
                                spaceName = step.space.name.orEmpty()
                            )
                            onConfigComplete(config)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpaceSelectionScreen(
    spaceViews: SpaceViewSubscriptionContainer,
    urlBuilder: UrlBuilder,
    onSpaceSelected: (ObjectWrapper.SpaceView) -> Unit
) {
    val spaces = remember { spaceViews.get() }
    
    // Filter to only show active data spaces (exclude chat spaces), pinned first
    val sortedSpaces = remember(spaces) {
        spaces
            .filter { it.isActive && it.spaceUxType != SpaceUxType.CHAT && it.spaceUxType != SpaceUxType.ONE_TO_ONE }
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
    }

    if (sortedSpaces.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No spaces available",
                color = Color(0xFFACA9A6)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(sortedSpaces) { space ->
                SpaceItem(
                    space = space,
                    urlBuilder = urlBuilder,
                    onClick = { onSpaceSelected(space) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SpaceItem(
    space: ObjectWrapper.SpaceView,
    urlBuilder: UrlBuilder,
    onClick: () -> Unit
) {
    val iconColor = space.iconOption
        ?.toInt()
        ?.let { SystemColor.color(it) }
        ?: SystemColor.SKY
    
    val backgroundColor = iconColor.toComposeColor()
    val textColor = iconColor.toComposeLightTextColor()
    val imageUrl = space.iconImage?.takeIf { it.isNotEmpty() }?.let { urlBuilder.medium(it) }
    val isChat = space.spaceUxType == SpaceUxType.CHAT || space.spaceUxType == SpaceUxType.ONE_TO_ONE
    val iconShape = if (isChat) CircleShape else RoundedCornerShape(6.dp)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B2A29))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Space icon with proper color/image
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = space.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(iconShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(iconShape)
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = space.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = textColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = space.name.orEmpty().ifEmpty { "Untitled" },
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!space.spaceOrder.isNullOrEmpty()) {
                Text(
                    text = "Pinned",
                    color = Color(0xFFACA9A6),
                    fontSize = 12.sp
                )
            }
        }

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_forward_24),
            contentDescription = null,
            tint = Color(0xFFACA9A6),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TypeSelectionScreen(
    space: ObjectWrapper.SpaceView,
    blockRepository: BlockRepository,
    dispatchers: AppCoroutineDispatchers,
    configStorage: ConfigStorage,
    appWidgetId: Int,
    onTypeSelected: (ObjectWrapper.Type) -> Unit
) {
    var types by remember { mutableStateOf<List<ObjectWrapper.Type>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(space) {
        isLoading = true
        scope.launch {
            try {
                val spaceId = SpaceId(space.targetSpaceId.orEmpty())
                val spaceUxType = space.spaceUxType
                val createableLayouts = SupportedLayouts.getCreateObjectLayouts(spaceUxType)

                val result = withContext(dispatchers.io) {
                    blockRepository.searchObjects(
                        space = spaceId,
                        keys = listOf(
                            Relations.ID,
                            Relations.UNIQUE_KEY,
                            Relations.NAME,
                            Relations.ICON_EMOJI,
                            Relations.RECOMMENDED_LAYOUT,
                            Relations.IS_ARCHIVED,
                            Relations.IS_DELETED,
                            Relations.IS_HIDDEN,
                            Relations.IS_HIDDEN_DISCOVERY
                        ),
                        filters = listOf(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                condition = DVFilterCondition.EQUAL,
                                value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
                            ),
                            DVFilter(
                                relation = Relations.IS_ARCHIVED,
                                condition = DVFilterCondition.NOT_EQUAL,
                                value = true
                            ),
                            DVFilter(
                                relation = Relations.IS_DELETED,
                                condition = DVFilterCondition.NOT_EQUAL,
                                value = true
                            ),
                            DVFilter(
                                relation = Relations.IS_HIDDEN,
                                condition = DVFilterCondition.NOT_EQUAL,
                                value = true
                            ),
                            DVFilter(
                                relation = Relations.IS_HIDDEN_DISCOVERY,
                                condition = DVFilterCondition.NOT_EQUAL,
                                value = true
                            ),
                            DVFilter(
                                relation = Relations.UNIQUE_KEY,
                                condition = DVFilterCondition.NOT_EMPTY
                            ),
                            DVFilter(
                                relation = Relations.UNIQUE_KEY,
                                condition = DVFilterCondition.NOT_EQUAL,
                                value = ObjectTypeUniqueKeys.TEMPLATE
                            ),
                            DVFilter(
                                relation = Relations.RECOMMENDED_LAYOUT,
                                condition = DVFilterCondition.IN,
                                value = createableLayouts.map { it.code.toDouble() }
                            )
                        ),
                        sorts = emptyList(),
                        limit = 100,
                        offset = 0,
                        fulltext = ""
                    )
                }

                types = result.mapNotNull { struct ->
                    try {
                        ObjectWrapper.Type(struct)
                    } catch (e: Exception) {
                        null
                    }
                }.sortedBy { it.name?.lowercase() }
                
            } catch (e: Exception) {
                Timber.e(e, "Error loading types")
                types = emptyList()
            }
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFFFC940))
        }
    } else if (types.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No types available",
                color = Color(0xFFACA9A6)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(types) { type ->
                TypeItem(
                    type = type,
                    onClick = { onTypeSelected(type) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun TypeItem(
    type: ObjectWrapper.Type,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2B2A29))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Type emoji or placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3D3C3B)),
            contentAlignment = Alignment.Center
        ) {
            val emoji = type.iconEmoji
            if (!emoji.isNullOrEmpty()) {
                Text(
                    text = emoji,
                    fontSize = 20.sp
                )
            } else {
                Text(
                    text = type.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = type.name.orEmpty().ifEmpty { "Untitled" },
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Maps SystemColor to Compose Color for widget config UI.
 */
private fun SystemColor.toComposeColor(): Color {
    return when (this) {
        SystemColor.GRAY -> Color(0xFF928F8E)
        SystemColor.YELLOW -> Color(0xFFE5D044)
        SystemColor.AMBER -> Color(0xFFF19611)
        SystemColor.RED -> Color(0xFFF55522)
        SystemColor.PINK -> Color(0xFFE51284)
        SystemColor.PURPLE -> Color(0xFFAB50CC)
        SystemColor.BLUE -> Color(0xFF3E58EB)
        SystemColor.SKY -> Color(0xFF2AA7EE)
        SystemColor.TEAL -> Color(0xFF0FC8BA)
        SystemColor.GREEN -> Color(0xFF5DD400)
    }
}

/**
 * Maps SystemColor to light text color for icon initials.
 */
private fun SystemColor.toComposeLightTextColor(): Color {
    return when (this) {
        SystemColor.GRAY -> Color(0xFFD6D5D4)
        SystemColor.YELLOW -> Color(0xFFFCF6CE)
        SystemColor.AMBER -> Color(0xFFFEECCE)
        SystemColor.RED -> Color(0xFFFED6C9)
        SystemColor.PINK -> Color(0xFFF9CFEB)
        SystemColor.PURPLE -> Color(0xFFEBD4F3)
        SystemColor.BLUE -> Color(0xFFD2DAF8)
        SystemColor.SKY -> Color(0xFFD2EEFA)
        SystemColor.TEAL -> Color(0xFFC6F2EE)
        SystemColor.GREEN -> Color(0xFFDAF5B0)
    }
}
