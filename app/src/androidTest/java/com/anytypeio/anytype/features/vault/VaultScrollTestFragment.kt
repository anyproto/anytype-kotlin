package com.anytypeio.anytype.features.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ui.SpaceIconView
import com.anytypeio.anytype.feature_vault.presentation.VaultSpaceView
import com.anytypeio.anytype.feature_vault.presentation.VaultUiState
import com.anytypeio.anytype.feature_vault.ui.VaultScreenContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/** Exercises the production list with subscription updates and no backend dependencies. */
class VaultScrollTestFragment : Fragment() {
    val sections = MutableStateFlow(VaultUiState.Sections(mainSpaces = (0..29).map { space("space-$it") }))
    lateinit var listState: LazyListState
    lateinit var scope: CoroutineScope
    var renderedSections: VaultUiState.Sections? = null
    val frames = mutableListOf<Viewport>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = arguments?.getInt("index") ?: 0,
                    initialFirstVisibleItemScrollOffset = arguments?.getInt("offset") ?: 0
                )
                scope = rememberCoroutineScope()
                val currentSections = sections.collectAsStateWithLifecycle().value
                MaterialTheme {
                    VaultScreenContent(
                        sections = currentSections,
                        lazyListState = listState,
                        paddings = PaddingValues(0.dp),
                        searchQuery = "",
                        onSpaceClicked = {},
                        onCreateSpaceClicked = {},
                        onMuteSpace = {},
                        onUnmuteSpace = {},
                        onSetSpaceNotificationMode = { _, _ -> },
                        onPinSpace = {},
                        onUnpinSpace = {},
                        onOrderChanged = { _, _ -> },
                        onSpaceSettings = {},
                        onDeleteOrLeaveSpace = { _, _ -> }
                    )
                }
                SideEffect { renderedSections = currentSections }
            }
            viewTreeObserver.addOnPreDrawListener {
                if (this@VaultScrollTestFragment::listState.isInitialized &&
                    listState.layoutInfo.totalItemsCount > 0
                ) {
                    frames += viewport()
                }
                // Key anchoring can leave identical pixels and skip invalidation entirely.
                // Keep drawing so even that case is sampled during a subscription update.
                postInvalidateOnAnimation()
                true
            }
        }

    fun viewport() = Viewport(
        index = listState.firstVisibleItemIndex,
        offset = listState.firstVisibleItemScrollOffset,
        key = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == listState.firstVisibleItemIndex }?.key
    )

    data class Viewport(val index: Int, val offset: Int, val key: Any?)

    companion object {
        fun space(id: String, pinned: Boolean = false) = VaultSpaceView.DataSpace(
            space = ObjectWrapper.SpaceView(mapOf(
                "id" to id,
                "name" to id,
                "spaceOrder" to if (pinned) id else null
            )),
            icon = SpaceIconView.DataSpace.Placeholder(name = id),
            accessType = "Owner",
            isOwner = true
        )
    }
}
