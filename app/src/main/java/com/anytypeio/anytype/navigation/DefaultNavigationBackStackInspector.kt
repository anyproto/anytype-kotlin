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
        // TODO(DROID-4518) temporary diagnostic — remove once back-history menu ordering is confirmed.
        runCatching {
            val dump = controller.currentBackStack.value.joinToString(" -> ") { e ->
                val name = e.destination.displayName.substringAfterLast('/')
                val ctx = e.arguments?.let { args ->
                    argKeysByDestination[e.destination.id]?.let { args.getString(it.first) }
                }
                "$name#${e.id.takeLast(4)}${ctx?.let { "(ctx=${it.takeLast(4)})" }.orEmpty()}"
            }
            val current = controller.currentBackStackEntry
            Timber.i("BACK_HISTORY_DEBUG stack: $dump")
            Timber.i("BACK_HISTORY_DEBUG current: ${current?.destination?.displayName?.substringAfterLast('/')}#${current?.id?.takeLast(4)}")
        }
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

    override fun currentEntryId(): String? = navController?.currentBackStackEntry?.id

    @SuppressLint("RestrictedApi") // NavController.currentBackStack is RestrictTo(LIBRARY_GROUP)
    override fun homeScreenEntryId(): String? {
        val controller = navController ?: return null
        return runCatching {
            controller.currentBackStack.value
                .lastOrNull { it.destination.id == R.id.homeScreen }
                ?.id
        }.onFailure {
            Timber.e(it, "Error while reading home screen back stack entry")
        }.getOrNull()
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
