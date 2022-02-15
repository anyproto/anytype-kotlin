package com.anytypeio.anytype.ui.auth.pin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.databinding.ItemNumPadEmptyBinding
import com.anytypeio.anytype.databinding.ItemNumPadNumberBinding
import com.anytypeio.anytype.databinding.ItemNumPadRemoveBinding
import com.anytypeio.anytype.presentation.auth.pin.NumPadView
import com.anytypeio.anytype.presentation.auth.pin.NumPadView.Companion.EMPTY
import com.anytypeio.anytype.presentation.auth.pin.NumPadView.Companion.NUMBER
import com.anytypeio.anytype.presentation.auth.pin.NumPadView.Companion.REMOVE

class NumPadAdapter(
    private val views: List<NumPadView> = initialData(),
    private val onNumberClicked: (NumPadView.NumberView) -> Unit,
    private val onRemoveClicked: () -> Unit
) : RecyclerView.Adapter<NumPadAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            NUMBER -> ViewHolder.NumberViewHolder(
                ItemNumPadNumberBinding.inflate(inflater, parent, false)
            )
            REMOVE -> ViewHolder.RemoveViewHolder(
                ItemNumPadRemoveBinding.inflate(inflater, parent, false)
            )
            EMPTY -> ViewHolder.EmptyViewHolder(
                ItemNumPadEmptyBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = views[position].getViewType()

    override fun getItemCount(): Int = views.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.NumberViewHolder -> {
                holder.bind(
                    view = views[position] as NumPadView.NumberView,
                    onNumberClicked = onNumberClicked
                )
            }
            is ViewHolder.RemoveViewHolder -> {
                holder.bind(
                    onRemoveClicked = onRemoveClicked
                )
            }
            is ViewHolder.EmptyViewHolder -> {
                // Do nothing.
            }
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class NumberViewHolder(val binding: ItemNumPadNumberBinding) : ViewHolder(binding.root) {

            private val number = binding.number

            fun bind(
                view: NumPadView.NumberView,
                onNumberClicked: (NumPadView.NumberView) -> Unit
            ) {
                number.setOnClickListener { onNumberClicked(view) }
                number.text = view.number.toString()
            }

        }

        class RemoveViewHolder(val binding: ItemNumPadRemoveBinding) : ViewHolder(binding.root) {
            fun bind(onRemoveClicked: () -> Unit) {
                binding.remove.setOnClickListener { onRemoveClicked() }
            }
        }

        class EmptyViewHolder(val binding: ItemNumPadEmptyBinding) : ViewHolder(binding.root)
    }

    companion object {

        fun initialData(): List<NumPadView> {
            return listOf(
                NumPadView.NumberView(1),
                NumPadView.NumberView(2),
                NumPadView.NumberView(3),
                NumPadView.NumberView(4),
                NumPadView.NumberView(5),
                NumPadView.NumberView(6),
                NumPadView.NumberView(7),
                NumPadView.NumberView(8),
                NumPadView.NumberView(9),
                NumPadView.EmptyView,
                NumPadView.NumberView(0),
                NumPadView.RemoveView
            )
        }
    }
}