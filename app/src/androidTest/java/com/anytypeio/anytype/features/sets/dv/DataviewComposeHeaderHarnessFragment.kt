package com.anytypeio.anytype.features.sets.dv

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
 * Backend-free fixture of the type screen's shape inside the production host: a Compose
 * object header, the native pinned controls row (view selector, filter icon, New button)
 * and the grid's chrome (a horizontally scrolling column header row above native rows).
 * The header is sized after the first layout so that the rows keep only a thin strip,
 * the geometry a phone shows in landscape. Every region records its bounds or is exposed
 * as a view, each of the three children records the pointer stream it receives, and the
 * host records every request to keep a gesture, so a test can tell whether the host
 * claimed a drag or left it to the child under the finger.
 */
class DataviewComposeHeaderHarnessFragment : Fragment() {
    lateinit var host: DataviewScrollHost
        private set
    lateinit var composeView: ComposeView
        private set
    lateinit var viewSelector: TextView
        private set
    lateinit var filterIcon: View
        private set
    lateinit var controlsSpacer: View
        private set
    lateinit var newButton: TextView
        private set
    lateinit var horizontalScroll: HorizontalScrollView
        private set
    lateinit var columns: RecyclerView
        private set
    lateinit var rows: RecyclerView
        private set
    val regionBounds = mutableMapOf<String, Rect>()
    val headerActions = mutableListOf<Int>()
    val controlsActions = mutableListOf<Int>()
    val viewportActions = mutableListOf<Int>()
    /** Every event the host received: action, y in the host, and the offset once handled. */
    val hostEvents = mutableListOf<Triple<Int, Float, Float>>()
    var disallowRequests = 0
        private set
    var chipClicks = 0
        private set
    var selectorClicks = 0
        private set
    var filterClicks = 0
        private set
    var newClicks = 0
        private set
    var columnClicks = 0
        private set
    var chipScroll: ScrollState? = null
        private set
    var listState: LazyListState? = null
        private set
    val rowsStripPx: Int get() = px(ROWS_STRIP_DP)
    /** The column header row and its divider, which the viewport shows above the rows. */
    val viewportChromePx: Int get() = px(COLUMN_ROW_DP) + px(1)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        val header = Recording(context, headerActions).apply { addView(composeView, ViewGroup.LayoutParams(-1, -1)) }
        val controls = Recording(context, controlsActions).apply { addView(buildControls(context)) }
        val viewport = Recording(context, viewportActions).apply { addView(buildGrid(context), ViewGroup.LayoutParams(-1, -1)) }
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
            addView(controls, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
            addView(viewport, ViewGroup.LayoutParams(-1, -1))
        }
        host.doOnLayout {
            header.layoutParams = header.layoutParams.apply {
                height = host.height - controls.height - viewportChromePx - rowsStripPx
            }
            host.invalidateGeometry()
        }
        return host
    }

    /** The pinned row: view selector, filter icon, empty background, New button, divider. */
    private fun buildControls(context: Context): View {
        viewSelector = TextView(context).apply {
            text = "All objects"
            gravity = Gravity.CENTER_VERTICAL
            setPadding(px(20), 0, px(8), 0)
            setOnClickListener { selectorClicks++ }
        }
        filterIcon = View(context).apply {
            setBackgroundColor(Color.DKGRAY)
            setOnClickListener { filterClicks++ }
        }
        controlsSpacer = View(context)
        newButton = TextView(context).apply {
            text = "New"
            gravity = Gravity.CENTER
            setBackgroundColor(Color.LTGRAY)
            setOnClickListener { newClicks++ }
        }
        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            addView(viewSelector, LinearLayout.LayoutParams(-2, -1))
            addView(filterIcon, LinearLayout.LayoutParams(px(24), px(24)).apply { gravity = Gravity.CENTER_VERTICAL })
            addView(controlsSpacer, LinearLayout.LayoutParams(0, -1, 1f))
            addView(newButton, LinearLayout.LayoutParams(px(64), px(28)).apply {
                gravity = Gravity.CENTER_VERTICAL
                marginEnd = px(20)
            })
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
            addView(row, LinearLayout.LayoutParams(-1, px(CONTROLS_ROW_DP)))
            addView(View(context).apply { setBackgroundColor(Color.GRAY) }, LinearLayout.LayoutParams(-1, px(1)))
        }
    }

    /** The grid's chrome as production nests it: a horizontal scroller over columns and rows. */
    private fun buildGrid(context: Context): View {
        columns = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = ColumnAdapter(context)
        }
        rows = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = RowAdapter(context)
        }
        val tableWidth = px(COLUMN_WIDTH_DP) * COLUMN_COUNT
        val table = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(columns, LinearLayout.LayoutParams(tableWidth, px(COLUMN_ROW_DP)))
            addView(View(context).apply { setBackgroundColor(Color.GRAY) }, LinearLayout.LayoutParams(tableWidth, px(1)))
            addView(rows, LinearLayout.LayoutParams(tableWidth, -1))
        }
        horizontalScroll = HorizontalScrollView(context).apply {
            isFillViewport = true
            addView(RelativeLayout(context).apply { addView(table, ViewGroup.LayoutParams(tableWidth, -1)) })
        }
        return horizontalScroll
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
            LazyColumn(state = list, modifier = Modifier.region(LIST).fillMaxWidth().height(72.dp)) {
                items((0 until 60).toList(), key = { it }) { index ->
                    Text("Line $index", Modifier.height(24.dp).fillMaxWidth())
                }
            }
            SelectionContainer(Modifier.region(SELECTABLE).fillMaxWidth().height(32.dp)) {
                Text("Selectable header text that a long press selects")
            }
            Spacer(Modifier.weight(1f).fillMaxWidth())
        }
    }

    private fun Modifier.region(name: String): Modifier =
        onGloballyPositioned { regionBounds[name] = it.boundsInRoot() }

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    /** A wrapper that records the pointer stream its child receives. */
    private class Recording(context: Context, private val actions: MutableList<Int>) : FrameLayout(context) {
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
            actions.add(event.actionMasked)
            return super.dispatchTouchEvent(event)
        }
    }

    private inner class ColumnAdapter(private val context: Context) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(TextView(context).apply {
            layoutParams = RecyclerView.LayoutParams(px(COLUMN_WIDTH_DP), px(COLUMN_ROW_DP))
            gravity = Gravity.CENTER_VERTICAL
            setOnClickListener { columnClicks++ }
        })
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.text.text = "Column $position"
        }
        override fun getItemCount(): Int = COLUMN_COUNT
    }

    private inner class RowAdapter(private val context: Context) : RecyclerView.Adapter<Holder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(TextView(context).apply {
            layoutParams = RecyclerView.LayoutParams(-1, px(56))
        })
        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.text.text = "Native record $position"
        }
        override fun getItemCount(): Int = 200
    }

    private class Holder(val text: TextView) : RecyclerView.ViewHolder(text)

    companion object {
        const val ROWS_STRIP_DP = 17
        const val CONTROLS_ROW_DP = 48
        const val COLUMN_ROW_DP = 40
        const val COLUMN_WIDTH_DP = 120
        const val COLUMN_COUNT = 8
        const val BACKGROUND = "background"
        const val CHIP = "chip"
        const val LIST = "list"
        const val SELECTABLE = "selectable"
    }
}
