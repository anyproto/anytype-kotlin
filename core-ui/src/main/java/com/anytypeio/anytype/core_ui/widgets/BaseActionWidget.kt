package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.anytypeio.anytype.core_ui.databinding.WidgetActionBaseBinding
import com.anytypeio.anytype.core_utils.ext.smoothSnapToPosition

abstract class BaseActionWidget<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CardView(context, attrs) {

    private val binding = WidgetActionBaseBinding.inflate(
        LayoutInflater.from(context), this, true
    )

    var actionListener: (T) -> Unit = {}

    protected abstract fun provideAdapter(): ListAdapter<T, *>

    protected val adapter: ListAdapter<T, *> by lazy { provideAdapter() }

    init {
        binding.blockActionRecycler.apply {
            itemAnimator = null
            adapter = this@BaseActionWidget.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                BaseActionWidgetItemDecoration(
                    context
                )
            )
        }
    }

    fun bind(actions: List<T>) {
        adapter.submitList(actions)
    }

    fun scrollToPosition(pos: Int, smooth: Boolean = false) {
        if (smooth) {
            binding.blockActionRecycler.smoothSnapToPosition(pos)
        } else {
            binding.blockActionRecycler.scrollToPosition(pos)
        }
    }
}