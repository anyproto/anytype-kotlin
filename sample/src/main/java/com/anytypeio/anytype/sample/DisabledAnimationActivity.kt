package com.anytypeio.anytype.sample

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_utils.ext.imm
import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.sample.adapter.AbstractAdapter
import com.anytypeio.anytype.sample.adapter.AbstractHolder
import kotlinx.android.synthetic.main.activity_disabled_animation.*
import kotlinx.android.synthetic.main.item_editable.view.*
import timber.log.Timber

class DisabledAnimationActivity : AppCompatActivity(R.layout.activity_disabled_animation) {

    private val start: List<Mock>
        get() = mutableListOf(
            Mock(
                id = 0,
                text = "TITLE",
                type = 0,
                isFocused = false
            ),
            Mock(
                id = 0,
                text = "BULLETED",
                type = 0,
                isFocused = true
            ),
            Mock(
                id = 1,
                text = "PARAGRAPH 2",
                type = 0,
                isFocused = false
            )
        )

    private val end: List<Mock>
        get() = listOf(
            Mock(
                id = 0,
                text = "TITLE",
                type = 0,
                isFocused = false
            ),
            Mock(
                id = 0,
                text = "PARAGRAPH",
                type = 1,
                isFocused = true
            ),
            Mock(
                id = 1,
                text = "PARAGRAPH 2",
                type = 0,
                isFocused = false
            )
        )

    private val mockAdapter = MockAdapter(
        items = start.toMutableList()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recycler.apply {
            layoutManager = CustomLinearLayoutManager(context)
            adapter = mockAdapter
            itemAnimator = null
        }
        startButton.setOnClickListener {
            Timber.d("Start button clicked")
            mockAdapter.update(
                update = end
            )
        }

        endButton.setOnClickListener {
            Timber.d("End button clicked")
            mockAdapter.update(
                update = start
            )
        }
    }

    class MockAdapter(val items: MutableList<Mock>) : AbstractAdapter<Mock>(items) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractHolder<Mock> {
            return when (viewType) {
                0 -> {
                    val inflater = LayoutInflater.from(parent.context)
                    val view = inflater.inflate(R.layout.item_editable, parent, false)
                    MockHolder(view)
                }
                1 -> {
                    val inflater = LayoutInflater.from(parent.context)
                    val view = inflater.inflate(R.layout.item_editable, parent, false)
                    MockHolder2(view)
                }
                else -> throw IllegalStateException()
            }
        }

        override fun getItemViewType(position: Int): Int {
            return items[position].getViewType()
        }

        override fun update(update: List<Mock>) {
            val old = ArrayList(items)
            val cb = Differ(old = old, new = update)
            val result = DiffUtil.calculateDiff(cb)
            items.clear()
            items.addAll(update)
            result.dispatchUpdatesTo(this)
        }
    }

    class MockHolder(view: View) : AbstractHolder<Mock>(view) {

        override fun bind(item: Mock) {
            Timber.d("Binding item: $item")
            itemView.input.setText(item.text)
            if (item.isFocused)
                focus()
            else
                itemView.input.clearFocus()
        }

        private fun focus() {
            itemView.input.apply {
                post {
                    if (!hasFocus()) {
                        if (requestFocus()) {
                            context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
                        } else {
                            Timber.d("Couldn't gain focus")
                        }
                    } else
                        Timber.d("Already had focus")
                }
            }
        }
    }

    class MockHolder2(view: View) : AbstractHolder<Mock>(view) {

        override fun bind(item: Mock) {
            Timber.d("Binding item: $item")
            itemView.input.setText(item.text)
            itemView.input.setTextColor(Color.GREEN)
            if (item.isFocused) focus()
        }

        private fun focus() {
            itemView.input.apply {
                post {
                    Timber.d("Focusing!")
                    if (!hasFocus()) {
                        if (requestFocus()) {
                            context.imm().showSoftInput(this, InputMethodManager.SHOW_FORCED)
                        } else {
                            Timber.d("Couldn't gain focus")
                        }
                    } else
                        Timber.d("Already had focus")
                }
            }
        }
    }

    data class Mock(
        val id: Int,
        val text: String,
        val isFocused: Boolean,
        val type: Int
    ) : ViewType {
        override fun getViewType(): Int = type
    }

    class Differ(
        private val old: List<Mock>,
        private val new: List<Mock>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            Timber.d("areItemsTheSame for: $oldItem \n and \n $newItem")
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = old[oldItemPosition]
            val newItem = new[newItemPosition]
            Timber.d("areContentsTheSame for: $oldItem \n and \n $newItem")
            return oldItem == newItem
        }
    }

    class CustomLinearLayoutManager(
        context: Context,
        //focus: View
    ) : LinearLayoutManager(context) {

        override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
            Timber.d("OnInterceptFocusSearch view with text: ${(focused as EditText).text}")
            when (direction) {
                View.FOCUS_UP -> {
                    Timber.d("OnInterceptFocusSearch direction: FOCUS_UP")
                }
                View.FOCUS_DOWN -> {
                    Timber.d("OnInterceptFocusSearch direction: FOCUS_DOWN")
                }
            }
            //return super.onInterceptFocusSearch(focused, direction)

            //val v = getChildAt(2)?.findViewById<EditText>(R.id.input)

            //v?.requestFocus()

            //Timber.d("At position 2 there is a view with text: ${(v as EditText).text}")

            return null
        }

        override fun removeView(child: View?) {
            Timber.d("On remove view")
            super.removeView(child)
        }

        override fun detachView(child: View) {
            Timber.d("On detach view")
            super.detachView(child)
        }

        override fun onFocusSearchFailed(
            focused: View,
            focusDirection: Int,
            recycler: RecyclerView.Recycler,
            state: RecyclerView.State
        ): View? {
            Timber.d("onFocusSearchFailed for view with text: ${(focused as EditText).text}")
            when (focusDirection) {
                View.FOCUS_UP -> {
                    Timber.d("onFocusSearchFailed direction: FOCUS_UP")
                }
                View.FOCUS_DOWN -> {
                    Timber.d("onFocusSearchFailed direction: FOCUS_DOWN")
                }
            }
            return super.onFocusSearchFailed(focused, focusDirection, recycler, state)
        }
    }
}
