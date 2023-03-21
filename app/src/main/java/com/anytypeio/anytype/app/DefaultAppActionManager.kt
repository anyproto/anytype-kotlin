package com.anytypeio.anytype.app

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.anytypeio.anytype.R
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.ui.main.MainActivity
import timber.log.Timber

/**
 * Manager for removing or adding app shortcut actions.
 * Further details: https://developer.android.com/guide/topics/ui/shortcuts
 */
class DefaultAppActionManager(val context: Context) : AppActionManager {

    override fun setup(action: AppActionManager.Action) {
        try {
            when (action) {
                is AppActionManager.Action.CreateNew -> {
                    val label = context.resources.getString(R.string.shortcut_create_new, action.name)
                    val shortcut = ShortcutInfoCompat.Builder(context, ACTION_CREATE_NEW_ID)
                        .setShortLabel(label)
                        .setLongLabel(label)
                        .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_new_app_launcher))
                        .setIntent(
                            Intent(Intent.ACTION_VIEW, null).apply {
                                setClass(context, MainActivity::class.java)
                                putExtra(ACTION_CREATE_NEW_TYPE_KEY, action.type)
                            }
                        )
                        .build()
                    ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
                }
                is AppActionManager.Action.ClearAll -> {
                    val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context).map { it.id }
                    ShortcutManagerCompat.removeLongLivedShortcuts(context, shortcuts)
                    ShortcutManagerCompat.removeAllDynamicShortcuts(context)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while setting up an app action: $action")
        }
    }

    companion object {
        const val ACTION_CREATE_NEW_ID = "anytype.app-action.create-new.id"
        const val ACTION_CREATE_NEW_TYPE_KEY = "anytype.app-action.create-new.key"
    }
}