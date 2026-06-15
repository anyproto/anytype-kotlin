package com.anytypeio.anytype.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.presentation.navigation.backstack.BackStackObjectEntry
import com.anytypeio.anytype.presentation.navigation.backstack.NavigationBackStackInspector
import com.anytypeio.anytype.ui.chats.ChatFragment
import com.anytypeio.anytype.ui.date.DateObjectFragment
import com.anytypeio.anytype.ui.editor.EditorFragment
import com.anytypeio.anytype.ui.primitives.ObjectTypeFragment
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Reads object-screen entries from the NavController's back stack.
 * Bound/unbound by MainActivity alongside the Navigator.
 */
@Singleton
class DefaultNavigationBackStackInspector @Inject constructor() : NavigationBackStackInspector {

    private var navController: NavController? = null

    fun bind(controller: NavController) {
        navController = controller
    }

    fun unbind() {
        navController = null
    }

    @SuppressLint("RestrictedApi") // NavController.currentBackStack is RestrictTo(LIBRARY_GROUP)
    override fun objectScreenEntries(): List<BackStackObjectEntry> {
        val controller = navController ?: return emptyList()
        return runCatching {
            controller.currentBackStack.value.mapNotNull { entry ->
                val keys = argKeysByDestination[entry.destination.id] ?: return@mapNotNull null
                val args = entry.arguments ?: return@mapNotNull null
                val objectId = args.getString(keys.first) ?: return@mapNotNull null
                val space = args.getString(keys.second) ?: return@mapNotNull null
                BackStackObjectEntry(
                    entryId = entry.id,
                    objectId = objectId,
                    space = space
                )
            }
        }.onFailure {
            Timber.e(it, "Error while reading navigation back stack")
        }.getOrDefault(emptyList())
    }

    companion object {
        /** destination id -> (object-id arg key, space-id arg key) */
        private val argKeysByDestination = mapOf(
            R.id.pageScreen to (EditorFragment.CTX_KEY to EditorFragment.SPACE_ID_KEY),
            R.id.objectSetScreen to (ObjectSetFragment.CONTEXT_ID_KEY to ObjectSetFragment.SPACE_ID_KEY),
            R.id.chatScreen to (ChatFragment.CTX_KEY to ChatFragment.SPACE_KEY),
            R.id.objectTypeScreen to (ObjectTypeFragment.ARG_OBJECT_ID to ObjectTypeFragment.ARG_SPACE),
            R.id.dateObjectScreen to (DateObjectFragment.ARG_OBJECT_ID to DateObjectFragment.ARG_SPACE)
        )
    }
}
