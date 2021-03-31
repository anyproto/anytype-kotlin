package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.relations.holders.*
import com.anytypeio.anytype.core_utils.text.ActionDoneListener
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.ObjectRelationTextValueView
import kotlinx.android.synthetic.main.item_object_relation_text.view.*

class ObjectRelationTextValueAdapter(
    private var items: List<ObjectRelationTextValueView>,
    private val actionClick: (EditGridCellAction) -> Unit,
    private val onEditCompleted: (ObjectRelationTextValueView, String) -> Unit
) : RecyclerView.Adapter<ObjectRelationBaseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectRelationBaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_object_relation_text, parent, false)
        return when (viewType) {
            TYPE_TEXT -> ObjectRelationTextHolder(view).apply {
                itemView.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_URL -> ObjectRelationUrlHolder(view).apply {
                itemView.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_PHONE -> ObjectRelationPhoneHolder(view).apply {
                itemView.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_EMAIL -> ObjectRelationEmailHolder(view).apply {
                itemView.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_NUMBER -> ObjectRelationNumberHolder(view).apply {
                itemView.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            else -> throw IllegalArgumentException("Wrong relation type:$view")
        }
    }

    override fun onBindViewHolder(holder: ObjectRelationBaseHolder, position: Int) {
        when (holder) {
            is ObjectRelationTextHolder -> holder.bind(items[position] as ObjectRelationTextValueView.Text)
            is ObjectRelationPhoneHolder -> holder.bind(
                items[position] as ObjectRelationTextValueView.Phone,
                actionClick
            )
            is ObjectRelationEmailHolder -> holder.bind(
                items[position] as ObjectRelationTextValueView.Email,
                actionClick
            )
            is ObjectRelationUrlHolder -> holder.bind(
                items[position] as ObjectRelationTextValueView.Url,
                actionClick
            )
            is ObjectRelationNumberHolder -> holder.bind(items[position] as ObjectRelationTextValueView.Number)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ObjectRelationTextValueView.Text -> TYPE_TEXT
        is ObjectRelationTextValueView.Email -> TYPE_EMAIL
        is ObjectRelationTextValueView.Number -> TYPE_NUMBER
        is ObjectRelationTextValueView.Phone -> TYPE_PHONE
        is ObjectRelationTextValueView.Url -> TYPE_URL
        else -> throw IllegalArgumentException("Wrong relation type:${items[position]}")
    }

    fun update(update: List<ObjectRelationTextValueView>) {
        items = update
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_PHONE = 2
        const val TYPE_EMAIL = 3
        const val TYPE_URL = 4
        const val TYPE_NUMBER = 5
    }
}