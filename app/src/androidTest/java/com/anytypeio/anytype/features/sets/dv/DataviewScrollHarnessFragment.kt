package com.anytypeio.anytype.features.sets.dv

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.EdgeEffect
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewColumnScrollConnection
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.DataviewScrollHost
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.dataviewBoardTouchObserver
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.dataviewColumnTouchObserver
import com.anytypeio.anytype.core_ui.widgets.dv.scroll.rememberDataviewColumnScrollConnection
import com.google.android.material.appbar.AppBarLayout
import kotlinx.coroutines.CoroutineScope

/**
 * Backend-free fixture using the production Parent3 and Compose adapters on the app's pinned stack.
 * The native table and both columns coexist, so ownership changes cross the View/Compose boundary.
 * The AppBar argument swaps only the parent to provide the design's bounded standard comparison.
 */
class DataviewScrollHarnessFragment : Fragment() {
    lateinit var host: DataviewScrollHost
        private set
    lateinit var nativeList: RecyclerView
        private set
    lateinit var composeView: ComposeView
        private set
    lateinit var appBar: AppBarLayout
        private set
    lateinit var composeScope: CoroutineScope
        private set
    val columnStates = mutableMapOf<String, LazyListState>()
    val columnBounds = mutableMapOf<String, Rect>()
    val connections = mutableMapOf<String, DataviewColumnScrollConnection>()
    val overscrollEffects = mutableMapOf<String, OverscrollEffect?>()
    val nativeEdges = mutableMapOf<Int, EdgeEffect>()
    val headerTrace = mutableListOf<Float>()
    val flingTrace = mutableListOf<String>()
    val nativeTrace = mutableListOf<String>()
    var appBarOffset = 0
        private set
    var nativeOffset = 0
        private set
    private val isAppBar: Boolean get() = arguments?.getBoolean(ARG_APP_BAR) == true
    private var composeTopInset by mutableIntStateOf(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        nativeList = RecyclerView(context).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = FixtureAdapter()
            edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect =
                    EdgeEffect(view.context).also { nativeEdges[direction] = it }
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    nativeOffset += dy
                }
            })
        }
        // A framework horizontal wrapper matches the production table's nested-parent traversal.
        val nativeTable = HorizontalScrollView(context).apply {
            tag = NATIVE_TAG
            isFillViewport = true
            addView(nativeList, ViewGroup.LayoutParams(px(400), ViewGroup.LayoutParams.MATCH_PARENT))
        }
        composeView = ComposeView(context).apply {
            tag = COMPOSE_TAG
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        }
        val viewport = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            // LinearLayout's baseline probe measures weighted children with UNSPECIFIED height.
            // LazyColumn must receive the host's finite viewport on every measurement pass.
            isBaselineAligned = false
            addView(nativeTable, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
            addView(composeView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 2f))
        }
        val header = TextView(context).apply {
            text = "Measured object header"
            setBackgroundColor(Color.LTGRAY)
            minimumHeight = px(56)
        }
        val controls = TextView(context).apply {
            text = "Pinned dataview controls"
            setBackgroundColor(Color.WHITE)
        }
        if (isAppBar) {
            return CoordinatorLayout(context).apply {
                appBar = AppBarLayout(context).apply {
                    addView(header, AppBarLayout.LayoutParams(-1, px(240)).apply {
                        scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                            AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED
                    })
                    addView(controls, AppBarLayout.LayoutParams(-1, px(48)))
                    addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
                        appBarOffset = offset
                    })
                }
                addView(viewport, CoordinatorLayout.LayoutParams(-1, -1).apply {
                    behavior = AppBarLayout.ScrollingViewBehavior()
                })
                addView(appBar, CoordinatorLayout.LayoutParams(-1, -2))
            }
        }
        host = object : DataviewScrollHost(context) {
            fun trace(event: String) {
                if (nativeTrace.size < 150) nativeTrace.add("$event gen=${coordinator.generation} h=${coordinator.offset} idle=${coordinator.isIdle}")
            }
            override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean =
                super.onStartNestedScroll(child, target, axes, type).also { trace("start type=$type axes=$axes accepted=$it") }
            override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
                trace("accept before type=$type")
                super.onNestedScrollAccepted(child, target, axes, type)
                trace("accept after type=$type")
            }
            override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
                trace("pre before type=$type dy=$dy")
                super.onNestedPreScroll(target, dx, dy, consumed, type)
                trace("pre after type=$type consumedY=${consumed[1]}")
            }
            override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
                trace("post before type=$type child=$dyConsumed residual=$dyUnconsumed")
                super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
                trace("post after type=$type consumedY=${consumed[1]}")
            }
            override fun onStopNestedScroll(target: View, type: Int) {
                trace("stop before type=$type")
                super.onStopNestedScroll(target, type)
                trace("stop after type=$type")
            }
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val before = coordinator.generation
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                if (coordinator.generation != before) trace("measure cancelled from gen=$before")
            }
        }.apply {
            pinHeight = px(56)
            addView(header, ViewGroup.LayoutParams(-1, px(240)))
            addView(controls, ViewGroup.LayoutParams(-1, px(48)))
            addView(FrameLayout(context).apply { addView(viewport) }, ViewGroup.LayoutParams(-1, -1))
            excludeNestedScrollTarget = composeView
            coordinator.addListener { headerTrace.add(coordinator.offset) }
        }
        host.setStableViewportChild(composeView) { composeTopInset = it }
        return host
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        composeView.setContent { FixtureBoard() }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun FixtureBoard() {
        composeScope = rememberCoroutineScope()
        val rowModifier = if (isAppBar) {
            Modifier.nestedScroll(rememberNestedScrollInteropConnection())
        } else {
            Modifier.dataviewBoardTouchObserver(host.coordinator)
        }
        Row(rowModifier.fillMaxSize().padding(top = with(LocalDensity.current) { composeTopInset.toDp() })) {
            for ((columnId, count) in listOf(TALL to 150, EMPTY to (arguments?.getInt(ARG_SHORT_COUNT) ?: 0))) {
                val state = remember(columnId) { LazyListState() }
                val overscroll = if (arguments?.getBoolean(ARG_DISABLE_OVERSCROLL) == true) null else rememberOverscrollEffect()
                val connection: NestedScrollConnection? = if (isAppBar) null else {
                    rememberDataviewColumnScrollConnection(
                        coordinator = host.coordinator,
                        viewerId = "harness",
                        columnId = columnId,
                        scrollableState = state
                    )
                }
                DisposableEffect(columnId, state, connection) {
                    columnStates[columnId] = state
                    overscrollEffects[columnId] = overscroll
                    if (connection is DataviewColumnScrollConnection) connections[columnId] = connection
                    onDispose {
                        columnStates.remove(columnId)
                        columnBounds.remove(columnId)
                        connections.remove(columnId)
                        overscrollEffects.remove(columnId)
                    }
                }
                val input = if (connection is DataviewColumnScrollConnection) {
                    val traced = remember(connection) {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource) = connection.onPreScroll(available, source)
                            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource) = connection.onPostScroll(consumed, available, source)
                            override suspend fun onPreFling(available: Velocity): Velocity {
                                flingTrace.add("$columnId pre y=${available.y} h=${host.coordinator.offset}")
                                return connection.onPreFling(available)
                            }
                            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                                flingTrace.add("$columnId post child=${consumed.y} available=${available.y} h=${host.coordinator.offset}")
                                return connection.onPostFling(consumed, available).also {
                                    flingTrace.add("$columnId returned=${it.y} h=${host.coordinator.offset}")
                                }
                            }
                        }
                    }
                    Modifier.dataviewColumnTouchObserver(connection).nestedScroll(traced)
                } else Modifier
                LazyColumn(
                    state = state,
                    overscrollEffect = overscroll,
                    modifier = input
                        .weight(1f)
                        .fillMaxHeight()
                        .onGloballyPositioned { columnBounds[columnId] = it.boundsInRoot() }
                ) {
                    stickyHeader(key = "label-$columnId") {
                        Text(
                            text = columnId,
                            modifier = Modifier
                                .background(androidx.compose.ui.graphics.Color.LightGray)
                                .height(48.dp)
                                .fillParentMaxWidth()
                        )
                    }
                    items((0 until count).toList(), key = { "$columnId-$it" }) { index ->
                        Box(Modifier.height((48 + index % 3 * 8).dp).padding(4.dp)) {
                            Text("$columnId card $index")
                        }
                    }
                }
            }
        }
    }

    fun startNativeSession(type: Int = ViewCompat.TYPE_TOUCH) {
        check(host.onStartNestedScroll(nativeList, nativeList, ViewCompat.SCROLL_AXIS_VERTICAL, type))
        host.onNestedScrollAccepted(nativeList, nativeList, ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private inner class FixtureAdapter : RecyclerView.Adapter<FixtureHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FixtureHolder =
            FixtureHolder(TextView(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(-1, px(56))
            })

        override fun onBindViewHolder(holder: FixtureHolder, position: Int) {
            holder.text.text = "Native record $position"
        }

        override fun getItemCount(): Int = 200
    }

    private class FixtureHolder(val text: TextView) : RecyclerView.ViewHolder(text)

    companion object {
        const val ARG_APP_BAR = "app-bar-comparison"
        const val ARG_SHORT_COUNT = "short-column-card-count"
        const val ARG_DISABLE_OVERSCROLL = "disable-overscroll-for-velocity-control"
        const val NATIVE_TAG = "dataview-harness-native"
        const val COMPOSE_TAG = "dataview-harness-compose"
        const val TALL = "Tall column"
        const val EMPTY = "Empty column"
    }
}
