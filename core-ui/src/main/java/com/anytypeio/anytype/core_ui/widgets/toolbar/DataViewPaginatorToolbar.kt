package com.anytypeio.anytype.core_ui.widgets.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemPaginatorPageBinding
import com.anytypeio.anytype.core_ui.databinding.WidgetDataViewPaginationToolbarBinding
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration

class DataViewPaginatorToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    var onNumberClickCallback: (Pair<Int, Boolean>) -> Unit = {}
    var onNext: () -> Unit = {}
    var onPrevious: () -> Unit = {}

    private val paginatorAdapter = Adapter { onNumberClickCallback(it) }

    val binding = WidgetDataViewPaginationToolbarBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        setBackgroundColor(resources.getColor(R.color.background_primary, null))
        binding.rvPaginator.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            val spacing = resources.getDimension(R.dimen.dp_12).toInt()
            addItemDecoration(
                SpacingItemDecoration(
                    spacingStart = spacing,
                    spacingEnd = spacing,
                    firstItemSpacingStart = spacing * 2,
                    lastItemSpacingEnd = spacing * 2
                )
            )
            adapter = paginatorAdapter
        }
        binding.ivNextPage.setOnClickListener { onNext() }
        binding.ivPreviousPage.setOnClickListener { onPrevious() }
    }

    fun set(count: Int, index: Int) = with(binding) {
        val update = mutableListOf<Pair<Int, Boolean>>()
        repeat(count) {
            val number = it
            update.add(
                Pair(number, index == number)
            )
        }
        paginatorAdapter.submitList(update)
        if (index > 0) rvPaginator.smoothScrollToPosition(index)
        if (index == 0) {
            ivPreviousPage.isEnabled = false
            ivPreviousPage.alpha = 0.2f
        } else {
            ivPreviousPage.isEnabled = true
            ivPreviousPage.alpha = 1.0f
        }
        if (index == count - 1) {
            ivNextPage.isEnabled = false
            ivNextPage.alpha = 0.2f
        } else {
            ivNextPage.isEnabled = true
            ivNextPage.alpha = 1f
        }
    }

    class Adapter(
        private val onNumberClicked: (Pair<Int, Boolean>) -> Unit
    ) : ListAdapter<Pair<Int, Boolean>, Adapter.ViewHolder>(PageNumberDiffer) {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): ViewHolder = ViewHolder(
            ItemPaginatorPageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onNumberClicked(getItem(pos))
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        class ViewHolder(
            val binding: ItemPaginatorPageBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: Pair<Int, Boolean>) {
                val (num, isSelected) = item
                binding.tvNumber.text = num.inc().toString()
                binding.tvNumber.isSelected = isSelected
            }
        }
    }

    object PageNumberDiffer : DiffUtil.ItemCallback<Pair<Int, Boolean>>() {
        override fun areItemsTheSame(
            oldItem: Pair<Int, Boolean>,
            newItem: Pair<Int, Boolean>
        ): Boolean = newItem.first == oldItem.first

        override fun areContentsTheSame(
            oldItem: Pair<Int, Boolean>,
            newItem: Pair<Int, Boolean>
        ): Boolean = newItem.first == oldItem.first && newItem.second == oldItem.second
    }
}