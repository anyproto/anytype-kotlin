package com.anytypeio.anytype.sample

import android.os.Bundle
import android.text.Annotation
import android.text.Spannable
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_test.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val annotation = Annotation("key", "rounded")
        val list = mutableListOf<Item>()

        val range = IntRange(0, 50)
        range.forEach {

            var t = "I am block number $it"
            val spannable = SpannableString(t)
            if (it == 10) {
                spannable.setSpan(annotation, 0, t.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                list.add(Item(text = spannable))
            } else {
                list.add(Item(text = spannable))
            }
        }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@MainActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
            adapter = MarkupAdapter(list) { pos: Int ->
                (this.adapter as MarkupAdapter).setData(
                    Item(SpannableString("I am block number $pos")),
                    pos
                )

            }
        }

    }

    class MarkupAdapter(private val data: MutableList<Item>, private val listener: (Int) -> Unit) :
        RecyclerView.Adapter<MarkupAdapter.MarkupViewHolder>() {

        fun setData(item: Item, position: Int) {
            data[position] = item
            notifyItemChanged(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkupViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_test, parent, false)
            return MarkupViewHolder(view)
        }

        override fun getItemCount(): Int = data.size

        override fun onBindViewHolder(holder: MarkupViewHolder, position: Int) {
            holder.bind(data[position].text, listener)
        }

        class MarkupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bind(text: SpannableString, listener: (Int) -> Unit) {
                itemView.item.text = text
                itemView.setOnClickListener {
                    listener.invoke(adapterPosition)
                }
            }
        }
    }
}

data class Item(val text: SpannableString)
