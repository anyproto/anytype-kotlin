package com.anytypeio.anytype.ui.auth.pin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.databinding.ItemDotBinding

class DotAdapter(
    val dots: MutableList<DotView> = initialData().toMutableList()
) : RecyclerView.Adapter<DotAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemDotBinding.inflate(inflater, parent, false))
    }

    override fun getItemCount(): Int = dots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(model = dots[position])
    }

    class ViewHolder(val bindind: ItemDotBinding) : RecyclerView.ViewHolder(bindind.root) {

        private val dot = bindind.dot

        fun bind(model: DotView) {
            dot.isSelected = model.active
        }
    }

    companion object {
        fun initialData(): List<DotView> {
            return listOf(
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                ),
                DotView(
                    active = false
                )
            )
        }
    }
}