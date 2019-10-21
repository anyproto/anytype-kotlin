package com.agileburo.anytype.ui.auth.pin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.auth.pin.NumPadView
import com.agileburo.anytype.presentation.auth.pin.NumPadView.Companion.EMPTY
import com.agileburo.anytype.presentation.auth.pin.NumPadView.Companion.NUMBER
import com.agileburo.anytype.presentation.auth.pin.NumPadView.Companion.REMOVE
import kotlinx.android.synthetic.main.item_num_pad_number.view.*
import kotlinx.android.synthetic.main.item_num_pad_remove.view.*

class NumPadAdapter(
    private val views: List<NumPadView> = initialData(),
    private val onNumberClicked: (NumPadView.NumberView) -> Unit,
    private val onRemoveClicked: () -> Unit
) : RecyclerView.Adapter<NumPadAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            NUMBER -> ViewHolder.NumberViewHolder(
                view = inflater
                    .inflate(R.layout.item_num_pad_number, parent, false)
            )
            REMOVE -> ViewHolder.RemoveViewHolder(
                view = inflater
                    .inflate(R.layout.item_num_pad_remove, parent, false)
            )
            EMPTY -> ViewHolder.EmptyViewHolder(
                view = inflater
                    .inflate(R.layout.item_num_pad_empty, parent, false)
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
        }
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class NumberViewHolder(view: View) : ViewHolder(view) {

            private val number = itemView.number

            fun bind(
                view: NumPadView.NumberView,
                onNumberClicked: (NumPadView.NumberView) -> Unit
            ) {
                number.setOnClickListener { onNumberClicked(view) }
                number.text = view.number.toString()
            }

        }

        class RemoveViewHolder(view: View) : ViewHolder(view) {
            fun bind(onRemoveClicked: () -> Unit) {
                itemView.remove.setOnClickListener { onRemoveClicked() }
            }
        }

        class EmptyViewHolder(view: View) : ViewHolder(view)
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