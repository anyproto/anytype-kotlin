package com.anytypeio.anytype.ui.oswidgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.views.BodyBold
import com.anytypeio.anytype.core_ui.views.Relations3
import com.anytypeio.anytype.core_ui.widgets.objectIcon.SpaceIconView
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidgetUpdater
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.persistence.oswidgets.OsWidgetsDataStore
import com.anytypeio.anytype.ui.objects.creation.ObjectTypeSelectionFragment
import com.anytypeio.anytype.ui.objects.types.pickers.ObjectTypeSelectionListener
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Configuration Activity for the Create Object widget.
 * Two-step flow: Select Space -> Select Type (using ObjectTypeSelectionFragment)
 */
class CreateObjectWidgetConfigActivity : AppCompatActivity(), ObjectTypeSelectionListener {

    @Inject
    lateinit var spaceViews: SpaceViewSubscriptionContainer

    @Inject
    lateinit var urlBuilder: UrlBuilder

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    // Store selected space for use when type is selected
    private var selectedSpace: ObjectWrapper.SpaceView? = null

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

        // Set up content with space selection screen
        setContentView(
            ComposeView(this).apply {
                setContent {
                    WidgetConfigTheme {
                        SpaceSelectionScreen(
                            spaceViews = spaceViews,
                            urlBuilder = urlBuilder,
                            onSpaceSelected = { space ->
                                selectedSpace = space
                                showTypeSelectionDialog(space)
                            },
                            onCancel = {
                                finish()
                            }
                        )
                    }
                }
            }
        )
    }

    private fun showTypeSelectionDialog(space: ObjectWrapper.SpaceView) {
        val spaceId = space.targetSpaceId.orEmpty()
        val dialog = ObjectTypeSelectionFragment.new(space = spaceId)
        dialog.show(supportFragmentManager, "object-type-selection")
    }

    override fun onSelectObjectType(objType: ObjectWrapper.Type) {
        val space = selectedSpace ?: run {
            Toast.makeText(this, "Error: No space selected", Toast.LENGTH_SHORT).show()
            return
        }
        
        val config = OsWidgetCreateObjectEntity(
            appWidgetId = appWidgetId,
            spaceId = space.targetSpaceId.orEmpty(),
            typeKey = objType.uniqueKey.orEmpty(),
            typeName = objType.name.orEmpty(),
            typeIconEmoji = objType.iconEmoji,
            typeIconName = objType.iconName,
            typeIconOption = objType.iconOption?.toInt(),
            spaceName = space.name.orEmpty()
        )
        completeConfiguration(config)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpaceSelectionScreen(
    spaceViews: SpaceViewSubscriptionContainer,
    urlBuilder: UrlBuilder,
    onSpaceSelected: (ObjectWrapper.SpaceView) -> Unit,
    onCancel: () -> Unit
) {
    val spaces = remember { spaceViews.get() }
    
    // Filter to only show active data spaces (exclude chat spaces), pinned first
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
                    items(
                        items = sortedSpaces,
                        key = { it.id }
                    ) { space ->
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
            modifier = Modifier
                .height(86.dp)
                .width(92.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            SpaceIconView(
                icon = icon,
                mainSize = 80.dp,
                onSpaceIconClick = onClick
            )
        }

        // Space Name
        Text(
            text = space.name.orEmpty().ifEmpty { stringResource(R.string.untitled) },
            style = Relations3,
            color = colorResource(id = R.color.text_primary),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 4.dp)
        )
    }
}

/**
 * Converts ObjectWrapper.SpaceView to SpaceIconView for rendering.
 */
private fun ObjectWrapper.SpaceView.toSpaceIconView(urlBuilder: UrlBuilder): SpaceIconView {
    val isChat = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
    val color = iconOption?.toInt()?.let { SystemColor.color(it) } ?: SystemColor.SKY
    val imageUrl = iconImage?.takeIf { it.isNotEmpty() }?.let { urlBuilder.medium(it) }
    
    return if (imageUrl != null) {
        if (isChat) {
            SpaceIconView.ChatSpace.Image(url = imageUrl, color = color)
        } else {
            SpaceIconView.DataSpace.Image(url = imageUrl, color = color)
        }
    } else {
        if (isChat) {
            SpaceIconView.ChatSpace.Placeholder(color = color, name = name.orEmpty())
        } else {
            SpaceIconView.DataSpace.Placeholder(color = color, name = name.orEmpty())
        }
    }
}
