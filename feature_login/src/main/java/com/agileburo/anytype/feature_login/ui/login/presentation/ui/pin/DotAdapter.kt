package com.agileburo.anytype.feature_login.ui.login.presentation.ui.pin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.feature_login.R
import kotlinx.android.synthetic.main.item_dot.view.*

class DotAdapter(
    val dots: MutableList<DotView> = initialData().toMutableList()
) : RecyclerView.Adapter<DotAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_dot, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dots.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(model = dots[position])
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val dot = itemView.dot

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