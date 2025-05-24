package com.anytypeio.anytype.app

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.anytypeio.anytype.ui.main.MainActivity

/**
 * Quick Settings tile service that allows creating new objects from the quick settings panel.
 * 
 * The tile's behavior is configured through shared preferences:
 * - typeKey: The type of object to create when clicked
 * - label: The display label for the tile
 *
 * The tile will be unavailable if no type is configured, and greyed out (but clickable) when it's set.
 * When clicked, it launches MainActivity with an intent to create a new object of the configured type.
 */
class QuickCreateTileService : TileService() {
    private var typeKey: String? = null

    @SuppressLint("StartActivityAndCollapseDeprecated")
    override fun onClick() {
        super.onClick()
        unlockAndRun {
            val intent = Intent(Intent.ACTION_VIEW, null).apply {
                setClass(applicationContext, MainActivity::class.java)
                putExtra(DefaultAppActionManager.ACTION_CREATE_NEW_TYPE_KEY, typeKey)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent = PendingIntent.getActivity(
                    applicationContext,
                    0, // request code
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
                startActivityAndCollapse(pendingIntent)
            } else {
                startActivityAndCollapse(intent)
            }
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    private fun updateTile() {
        val prefs = getSharedPreferences(DefaultAppActionManager.QUICK_CREATE_TILE_PREFS, Context.MODE_PRIVATE)
        typeKey = prefs.getString("typeKey", null)

        val tile = qsTile
        tile.state = if (typeKey != null) Tile.STATE_INACTIVE else Tile.STATE_UNAVAILABLE
        tile.label = prefs.getString("label", "New Object")
        tile.updateTile()
    }
}
