package com.agileburo.anytype

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_day.view.*

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 06.03.2019.
 */
class DaysAdapter(
    private val days: List<Day>,
    val onClick: (Day) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DaysAdapter.DayViewHolder {
        return DayViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        )
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DaysAdapter.DayViewHolder, p1: Int) {
        holder.apply {
            bind(day = days[p1], click = onClick)
        }
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvMonth = view.month
        val tvDay = view.day
        val tvContent = view.content

        fun bind(day: Day, click: (Day) -> Unit) {
            tvMonth.text = day.month.toString()
            tvDay.text = day.day
            tvContent.text = day.content
            itemView.setOnClickListener { click(day) }
        }
    }
}