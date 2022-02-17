package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
import com.anytypeio.anytype.core_ui.features.relations.holders.*
import com.anytypeio.anytype.core_utils.text.ActionDoneListener
import com.anytypeio.anytype.presentation.sets.EditGridCellAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView

class RelationTextValueAdapter(
    private var items: List<RelationTextValueView>,
    private val actionClick: (EditGridCellAction) -> Unit,
    private val onEditCompleted: (RelationTextValueView, String) -> Unit
) : RecyclerView.Adapter<RelationBaseHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationBaseHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_object_relation_text, parent, false)
        return when (viewType) {
            TYPE_TEXT -> RelationTextHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_text)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_TEXT_SHORT -> RelationTextShortHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_text)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_URL -> RelationUrlHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_url)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_PHONE -> RelationPhoneHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_phone)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_EMAIL -> RelationEmailHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_email)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            TYPE_NUMBER -> RelationNumberHolder(
                binding = ItemObjectRelationTextBinding.inflate(
                    inflater, parent, false
                )
            ).apply {
                binding.textInputField.setHint(R.string.enter_number)
                binding.textInputField.setOnEditorActionListener(
                    ActionDoneListener { txt ->
                        onEditCompleted(items[bindingAdapterPosition], txt)
                    }
                )
            }
            else -> throw IllegalArgumentException("Wrong relation type:$view")
        }
    }

    override fun onBindViewHolder(holder: RelationBaseHolder, position: Int) {
        when (holder) {
            is RelationTextHolder -> holder.bind(
                items[position] as RelationTextValueView.Text
            )
            is RelationTextShortHolder -> holder.bind(
                items[position] as RelationTextValueView.TextShort
            )
            is RelationPhoneHolder -> holder.bind(
                items[position] as RelationTextValueView.Phone,
                actionClick
            )
            is RelationEmailHolder -> holder.bind(
                items[position] as RelationTextValueView.Email,
                actionClick
            )
            is RelationUrlHolder -> holder.bind(
                items[position] as RelationTextValueView.Url,
                actionClick
            )
            is RelationNumberHolder -> holder.bind(
                items[position] as RelationTextValueView.Number
            )
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is RelationTextValueView.Text -> TYPE_TEXT
        is RelationTextValueView.TextShort -> TYPE_TEXT_SHORT
        is RelationTextValueView.Email -> TYPE_EMAIL
        is RelationTextValueView.Number -> TYPE_NUMBER
        is RelationTextValueView.Phone -> TYPE_PHONE
        is RelationTextValueView.Url -> TYPE_URL
        else -> throw IllegalArgumentException("Wrong relation type:${items[position]}")
    }

    fun update(update: List<RelationTextValueView>) {
        items = update
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_TEXT = 1
        const val TYPE_PHONE = 2
        const val TYPE_EMAIL = 3
        const val TYPE_URL = 4
        const val TYPE_NUMBER = 5
        const val TYPE_TEXT_SHORT = 6
    }
}