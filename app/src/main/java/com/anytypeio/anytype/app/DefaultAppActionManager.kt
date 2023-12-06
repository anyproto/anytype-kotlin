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
                    val id = "$ACTION_CREATE_NEW_ID-${action.type}"
                    setupCreateNewObjectAction(action, id)
                }
                is AppActionManager.Action.ClearAll -> {
                    clearShortcuts()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error while setting up an app action: $action")
        }
    }

    override fun setup(actions: List<AppActionManager.Action.CreateNew>) {
        clearShortcuts()
        actions.forEach { action ->
            val id = "$ACTION_CREATE_NEW_ID-${action.type}"
            setupCreateNewObjectAction(action, id)
        }
    }

    private fun clearShortcuts() {
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context).map { it.id }
        ShortcutManagerCompat.removeLongLivedShortcuts(context, shortcuts)
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
    }

    private fun setupCreateNewObjectAction(
        action: AppActionManager.Action.CreateNew,
        id: String
    ) {
        val name = action.name.ifEmpty {
            context.resources.getString(R.string.unknown_type)
        }
        val label = context.resources.getString(R.string.shortcut_create_new_object_of_type, name)
        val shortcut = ShortcutInfoCompat.Builder(context, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(
                Intent(Intent.ACTION_VIEW, null).apply {
                    setClass(context, MainActivity::class.java)
                    putExtra(ACTION_CREATE_NEW_TYPE_KEY, action.type.key)
                }
            )
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    companion object {
        const val ACTION_CREATE_NEW_ID = "anytype.app-action.create-new.id"
        const val ACTION_CREATE_NEW_TYPE_KEY = "anytype.app-action.create-new.key"
    }
}