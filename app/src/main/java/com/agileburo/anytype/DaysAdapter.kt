package com.agileburo.anytype

import android.support.v7.widget.RecyclerView
import android.text.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.agileburo.anytype.editor.EditorTextWatcher
import kotlinx.android.synthetic.main.item_day.view.*

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 06.03.2019.
 */
class DaysAdapter(
    private var days: ArrayList<Day>,
    val onClick: (Day) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    fun update(day: Day) {
        val index = days.indexOf(day)
        days[index] = Day(day = day.day, month = day.month, content = day.content, size = 1.3f)
        notifyItemChanged(index)
    }

    var isBoldActive = false
    var isItalicActive = false
    var isStrokeThroughActive = false

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DaysAdapter.DayViewHolder {
        return DayViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        )
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DaysAdapter.DayViewHolder, p1: Int) {
        holder.apply {
            with(days[p1]) {
                tvMonth.text = month.toString()
                tvDay.text = day
                val spannable = SpannableString(content)
                tvContent.setText(spannable, TextView.BufferType.SPANNABLE)
            }
            bind(day = days[p1], click = onClick)
        }
    }

    inner class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val tvMonth = view.month
        val tvDay = view.day
        val tvContent = view.content
        val textWatcher: EditorTextWatcher = EditorTextWatcher()

        fun bind(day: Day, click: (Day) -> Unit) {
            itemView.setOnClickListener {
                click(
                    Day(
                        month = day.month,
                        day = day.day,
                        content = day.content,
                        size = 1.0f
                    )
                )
            }
            tvContent.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    (v as? EditText)?.addTextChangedListener(textWatcher)
                } else {
                    (v as? EditText)?.removeTextChangedListener(textWatcher)
                }
            }
        }
    }
}