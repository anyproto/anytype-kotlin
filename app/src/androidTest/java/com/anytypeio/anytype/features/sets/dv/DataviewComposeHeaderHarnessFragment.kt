package com.anytypeio.anytype.features.sets.dv

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollHost

/**
 * Backend-free fixture of the type screen's shape: a Compose object header above pinned
 * controls and native rows, inside the production host. The header is sized after the first
 * layout so that the rows keep only a thin strip, the geometry a phone shows in landscape.
 * Each header region records its bounds, the container records the pointer stream it
 * receives, and the host records every request to keep a gesture, so a test can tell
 * whether the host claimed a drag or left it to Compose.
 */
class DataviewComposeHeaderHarnessFragment : Fragment() {
    lateinit var host: DataviewScrollHost
        private set
    lateinit var composeView: ComposeView
        private set
    lateinit var rows: RecyclerView
        private set
    val regionBounds = mutableMapOf<String, Rect>()
    val headerActions = mutableListOf<Int>()
    /** Every event the host received: action, y in the host, and the offset once handled. */
    val hostEvents = mutableListOf<Triple<Int, Float, Float>>()
    var disallowRequests = 0
        private set
    var chipClicks = 0
        private set
    var chipScroll: ScrollState? = null
        private set
    var listState: LazyListState? = null
        private set
    val rowsStripPx: Int get() = px(ROWS_STRIP_DP)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        rows = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FixtureAdapter(context)
        }
        composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        val header = object : FrameLayout(context) {
            override fun dispatchTouchEvent(event: MotionEvent): Boolean {
                headerActions.add(event.actionMasked)
                return super.dispatchTouchEvent(event)
            }
        }.apply { addView(composeView, ViewGroup.LayoutParams(-1, -1)) }
        val controls = TextView(context).apply {
            text = "Pinned dataview controls"
            setBackgroundColor(Color.WHITE)
        }
        host = object : DataviewScrollHost(context) {
            override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                if (disallowIntercept) disallowRequests++
                super.requestDisallowInterceptTouchEvent(disallowIntercept)
            }
            override fun dispatchTouchEvent(event: MotionEvent): Boolean =
                super.dispatchTouchEvent(event).also {
                    hostEvents.add(Triple(event.actionMasked, event.y, coordinator.offset))
                }
        }.apply {
            // The type screen keeps its toolbar outside the host, so nothing is pinned.
            pinHeight = 0
            addView(header, ViewGroup.LayoutParams(-1, px(240)))
            addView(controls, ViewGroup.LayoutParams(-1, px(48)))
            addView(FrameLayout(context).apply { addView(rows, ViewGroup.LayoutParams(-1, -1)) }, ViewGroup.LayoutParams(-1, -1))
        }
        host.doOnLayout {
            header.layoutParams = header.layoutParams.apply { height = host.height - px(48) - rowsStripPx }
            host.invalidateGeometry()
        }
        return host
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        composeView.setContent { FixtureHeader() }
    }

    @Composable
    private fun FixtureHeader() {
        val scroll = rememberScrollState()
        val list = remember { LazyListState() }
        DisposableEffect(scroll, list) {
            chipScroll = scroll
            listState = list
            onDispose {
                chipScroll = null
                listState = null
            }
        }
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.region(BACKGROUND).fillMaxWidth().height(48.dp))
            Row(Modifier.fillMaxWidth().height(36.dp).horizontalScroll(scroll)) {
                repeat(12) { index ->
                    Box(
                        Modifier
                            .then(if (index == 0) Modifier.region(CHIP) else Modifier)
                            .padding(horizontal = 4.dp)
                            .width(96.dp)
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.LightGray)
                            .clickable { chipClicks++ }
                    ) { Text("Chip $index") }
                }
            }
            LazyColumn(state = list, modifier = Modifier.region(LIST).fillMaxWidth().height(96.dp)) {
                items((0 until 60).toList(), key = { it }) { index ->
                    Text("Line $index", Modifier.height(24.dp).fillMaxWidth())
                }
            }
            SelectionContainer(Modifier.region(SELECTABLE).fillMaxWidth().height(40.dp)) {
                Text("Selectable header text that a long press selects")
            }
            Spacer(Modifier.weight(1f).fillMaxWidth())
        }
    }

    private fun Modifier.region(name: String): Modifier =
        onGloballyPositioned { regionBounds[name] = it.boundsInRoot() }

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private class FixtureAdapter(private val context: Context) : RecyclerView.Adapter<FixtureHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = FixtureHolder(TextView(context).apply {
            layoutParams = RecyclerView.LayoutParams(-1, (56 * context.resources.displayMetrics.density).toInt())
        })
        override fun onBindViewHolder(holder: FixtureHolder, position: Int) {
            holder.text.text = "Native record $position"
        }
        override fun getItemCount(): Int = 200
    }

    private class FixtureHolder(val text: TextView) : RecyclerView.ViewHolder(text)

    companion object {
        const val ROWS_STRIP_DP = 17
        const val BACKGROUND = "background"
        const val CHIP = "chip"
        const val LIST = "list"
        const val SELECTABLE = "selectable"
    }
}
