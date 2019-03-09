package com.agileburo.anytype

import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.text.*
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.item_day.view.*
import timber.log.Timber

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

    private var isBoldActive = false
    private var isItalicActive = false

    fun setBold() {
        isBoldActive = !isBoldActive
    }

    fun setItalic() {
        isItalicActive = !isItalicActive
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): DaysAdapter.DayViewHolder {
        return DayViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        )
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: DaysAdapter.DayViewHolder, p1: Int) {
        holder.apply {
            with(days[p1]) {
                Timber.d("onBind : $this")
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

        fun bind(day: Day, click: (Day) -> Unit) {
            itemView.setOnClickListener {
                click(
                    Day(
                        month = day.month,
                        day = day.day,
                        content = day.content,
                        size = 1.3f
                    )
                )
            }
            tvContent.addTextChangedListener(object : TextWatcher {

                var spannableText: SpannableString? = null
                var spanBold: StyleSpan? = null
                var spanItalic: StyleSpan? = null


                override fun afterTextChanged(s: Editable?) {
                    spannableText?.let {
                        val boldStart = it.getSpanStart(spanBold)
                        val boldEnd = it.getSpanEnd(spanBold)
                        if (boldStart > -1 && boldEnd > -1) {
                            s?.setSpan(
                                spanBold,
                                boldStart,
                                boldEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                        val italicStart = it.getSpanStart(spanItalic)
                        val italicEnd = it.getSpanEnd(spanItalic)
                        if (italicStart > -1 && italicEnd > -1) {
                            s?.setSpan(
                                spanItalic,
                                italicStart,
                                italicEnd,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                    spannableText = null
                    spanBold = null
                    spanItalic = null
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                    //Timber.d("beforeTextChanged, s:$s, start:$start, count:$count, after:$after")
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    spannableText = SpannableString(s).apply {
                        if (isBoldActive) {
                            spanBold = StyleSpan(Typeface.BOLD)
                            setSpan(spanBold, start, start + count, Spanned.SPAN_COMPOSING)
                        }
                        if (isItalicActive) {
                            spanItalic = StyleSpan(Typeface.ITALIC)
                            setSpan(spanItalic, start, start + count, Spanned.SPAN_COMPOSING)
                        }
                    }
                }
            })
        }
    }
}