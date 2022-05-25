package com.anytypeio.anytype.sample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ext.toast
import kotlinx.android.synthetic.main.activity_scroll_and_move.*
import kotlinx.android.synthetic.main.item_scroll_and_move.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class ScrollAndMove : AppCompatActivity() {

    private val decoration = object : RecyclerView.ItemDecoration() {

        var divider: Drawable? = null

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            parent.adapter?.itemCount?.let { count ->
                if (count == 1) {
                    outRect.bottom = screenHeight
                    outRect.top = screenHeight
                } else {
                    if (parent.getChildAdapterPosition(view) == count - 1) {
                        outRect.bottom = screenHeight
                    } else if (parent.getChildAdapterPosition(view) == 0) {
                        outRect.top = screenHeight
                    }
                }
            }
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            //c.save()

            Timber.d("child count: ${parent.childCount}")

            for (i in 0 until parent.childCount) {

                Timber.d("idx: $i")

                val child = parent.getChildAt(i)

                val pos = parent.getChildAdapterPosition(child)

                Timber.d("onDrawOver position: $pos")

                var targeted = false
                var ratio: Float = 0.0f

                (parent.adapter as? Adapter)?.let { adapter ->
                    val item = adapter.models[pos]
                    targeted = item.isTargeted
                    ratio = item.ratio
                }

                if (!targeted) continue

                Timber.d("onDrawOver ratio: $ratio")

                if (ratio in END_RANGE) {
                    Timber.d("Drawing bottom decoration for: $pos")

                    val left = 0
                    val right = parent.width
                    val top = child.bottom
                    val bottom = child.bottom + divider!!.intrinsicHeight


                    divider?.setBounds(left, top, right, bottom)
                    divider?.draw(c)
                } else if (ratio in START_RANGE) {
                    Timber.d("Drawing bottom decoration for: $pos")

                    val left = 0
                    val right = parent.width
                    val top = child.top
                    val bottom = child.top + divider!!.intrinsicHeight

                    divider?.setBounds(left, top, right, bottom)
                    divider?.draw(c)
                }
            }

            //c.restore()
        }
    }

    private val channel = Channel<Unit>()

    init {
        channel
            .consumeAsFlow()
            .onEach { Timber.d("event!") }
            .mapLatest {
                calculate()
            }
            .onEach { Timber.d("done") }
            .launchIn(lifecycleScope)
    }

    val screenHeight: Int
        get() {
            val wm = (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            val display = wm.defaultDisplay
            val p = Point()
            display.getSize(p)
            return p.y / 2
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scroll_and_move)
        setup()
    }

    fun toggleDecoration() {
        if (recycler.itemDecorationCount > 0) {
            recycler.removeItemDecoration(decoration)
        } else {
            recycler.addItemDecoration(decoration)
        }
    }


    fun setup() {

        toogleDecoration.setOnClickListener {
            toggleDecoration()
        }

        apply.setOnClickListener {
            move()
        }

        val text = getString(R.string.placeholder_text)

        val models = mutableListOf<Model>().apply {
            repeat(MODEL_COUNT) { count ->
                add(
                    Model(
                        text = "$count. $text",
                        isSelected = count in 0..1
                    )
                )
            }
        }

        decoration.divider = getDrawable(R.drawable.scroll_and_move_divider)

        recycler.apply {

            addItemDecoration(decoration)

            layoutManager = LinearLayoutManager(context)

            adapter = Adapter(
                models = models
            )

            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        //Timber.d("On scrolled dy: $dy")
                        //Timber.d("Current scroll y: ${recycler.scrollY}")
                    }

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            lifecycleScope.launch {
                                channel.send(Unit)
                            }
                        }
                        //return

                        // Timber.d("Vertical scroll: ${recycler.computeVerticalScrollOffset()}")
                    }
                }
            )
        }
    }

    fun move() {

        val adapter = (recycler.adapter as Adapter)
        val models = adapter.models
        val target = models.firstOrNull { it.isTargeted }

        check(models.count { it.isTargeted } == 1) { "merde!" }

        if (target != null) {
            when (target.ratio) {
                in START_RANGE -> {
                    // top
                    val result = mutableListOf<Model>()
                    val selected = models.filter { it.isSelected }
                    models.forEach { model ->
                        if (!model.isSelected) {
                            if (!model.isTargeted)
                                result.add(model)
                            if (model.isTargeted) {
                                result.addAll(selected)
                                result.add(
                                    model.copy(
                                        isTargeted = false,
                                        ratio = 0.0f
                                    )
                                )
                            }
                        }
                    }
                    adapter.update(result)
                }
                in MIDDLE_RANDE -> {
                    // inside
                    val update =
                        models.filter { !it.isSelected }.map { it.copy(isTargeted = false) }
                    adapter.update(update)
                }
                in END_RANGE -> {
                    // bottom
                    val result = mutableListOf<Model>()
                    val selected = models.filter { it.isSelected }
                    models.forEach { model ->
                        if (!model.isSelected) {
                            if (!model.isTargeted)
                                result.add(model)
                            if (model.isTargeted) {
                                result.add(
                                    model.copy(
                                        isTargeted = false,
                                        ratio = 0.0f
                                    )
                                )
                                result.addAll(selected)
                            }
                        }
                    }
                    adapter.update(result)
                }
            }
        }

        toast("well done!")
    }

    fun calculate() {

        val lm = recycler.layoutManager as LinearLayoutManager

        recycler.findChildViewUnder(0f, screenHeight.toFloat())?.let { view ->

            Timber.d("Found view: $view")

            val first = lm.findFirstVisibleItemPosition()
            val last = lm.findLastVisibleItemPosition()

            val position = recycler.getChildAdapterPosition(view)

            val center = screenHeight.toFloat()

            val top = view.top
            val height = view.height

            val ratio: Float

            if (center < top) {
                Timber.d("top of the view is below center")
                val delta = top - center
                ratio = delta / height
                Timber.d("Ration = $ratio")
            } else {
                Timber.d("top ($top) of the view is above center ($center)")
                val delta = center - top
                ratio = delta / height
                Timber.d("Ration = $ratio")
            }

            Timber.d("Target position: $position")
            Timber.d("First visible item position: $first")
            Timber.d("Last visible item position: $last")

            (recycler.adapter as Adapter).let {
                val update = it.models.mapIndexed { index, model ->
                    model.copy(
                        isTargeted = index == position,
                        ratio = ratio
                    )
                }
                it.update(update)
            }
        }

    }

    companion object {
        const val MODEL_COUNT = 20
        val START_RANGE = 0.0..0.2
        val MIDDLE_RANDE = 0.2..0.8
        val END_RANGE = 0.8..1.0
    }
}

data class Model(
    val text: String,
    val isTargeted: Boolean = false,
    val isSelected: Boolean = false,
    val ratio: Float = 0.0f
)

class Adapter(var models: List<Model>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder = ViewHolder(
        view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_scroll_and_move,
            parent,
            false
        )
    )

    override fun getItemCount(): Int = models.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(models[position])
    }

    fun update(update: List<Model>) {
        this.models = update
        notifyDataSetChanged()
        Timber.d("Updated data set: $update")
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val top = itemView.topDivider
        private val bottom = itemView.bottomDivider
        private val paragraph = itemView.paragraph

        fun bind(model: Model) {

            if (model.isTargeted) {
                when (model.ratio) {
                    in START_RANGE -> {
                        itemView.setBackgroundColor(Color.WHITE)
                    }
                    in MIDDLE_RANDE -> {
                        itemView.setBackgroundColor(Color.parseColor("#FFB522"))
                    }
                    in END_RANGE -> {
                        itemView.setBackgroundColor(Color.WHITE)
                    }
                }
                Timber.d("Binding selected model: $model")
            } else {
                itemView.setBackgroundColor(Color.WHITE)
                //bottom.invisible()
                //top.invisible()
                Timber.d("Binding simple model")
            }

            if (model.isSelected) {
                paragraph.setTextColor(
                    Color.parseColor("#3E58EB")
                )
            } else {
                paragraph.setTextColor(
                    Color.parseColor("#2C2B27")
                )
            }

            paragraph.text = model.text
            itemView.isSelected = false
        }
    }

    companion object {
        val START_RANGE = 0.0..0.2
        val MIDDLE_RANDE = 0.2..0.8
        val END_RANGE = 0.8..1.0
    }
}