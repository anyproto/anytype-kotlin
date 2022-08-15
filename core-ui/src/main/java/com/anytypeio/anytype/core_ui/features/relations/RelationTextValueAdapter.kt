package com.anytypeio.anytype.core_ui.features.relations

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectRelationTextBinding
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationEmailHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationNumberHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationPhoneHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationTextHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationTextShortHolder
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationTextViewHolderBase
import com.anytypeio.anytype.core_ui.features.relations.holders.RelationUrlHolder
import com.anytypeio.anytype.core_utils.ext.syncFocusWithImeVisibility
import com.anytypeio.anytype.core_utils.text.ActionDoneListener
import com.anytypeio.anytype.presentation.sets.RelationValueAction
import com.anytypeio.anytype.presentation.sets.RelationTextValueView

class RelationTextValueAdapter(
    private var items: List<RelationTextValueView>,
    private val onEditCompleted: (RelationTextValueView, String) -> Unit
) : RecyclerView.Adapter<RelationTextViewHolderBase<*>>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RelationTextViewHolderBase<*> {
        val holder = createHolder(parent, viewType)
        with(holder.binding) {
            setWindowFocusController(textInputField)
            textInputField.setOnEditorActionListener(
                ActionDoneListener { txt ->
                    onEditCompleted(items[holder.bindingAdapterPosition], txt)
                }
            )
        }
        return holder
    }

    private fun createHolder(
        parent: ViewGroup,
        viewType: Int
    ): RelationTextViewHolderBase<*> {
        val binding = ItemObjectRelationTextBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return when (viewType) {
            TYPE_TEXT -> RelationTextHolder(binding, R.string.enter_text)
            TYPE_TEXT_SHORT -> RelationTextShortHolder(binding, R.string.enter_text)
            TYPE_URL -> RelationUrlHolder(binding, R.string.enter_url)
            TYPE_PHONE -> RelationPhoneHolder(binding, R.string.enter_phone)
            TYPE_EMAIL -> RelationEmailHolder(binding, R.string.enter_email)
            TYPE_NUMBER -> RelationNumberHolder(binding, R.string.enter_number)
            else -> throw IllegalArgumentException("Unexpected view type: $viewType")
        }
    }

    private fun setWindowFocusController(view: EditText) {
        if (BuildConfig.USE_NEW_WINDOW_INSET_API && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.syncFocusWithImeVisibility()
        }
    }

    override fun onBindViewHolder(holder: RelationTextViewHolderBase<*>, position: Int) {
        val item = items[position]
        when (holder) {
            is RelationTextHolder -> holder.bind(item as RelationTextValueView.Text)
            is RelationTextShortHolder -> holder.bind(item as RelationTextValueView.TextShort)
            is RelationPhoneHolder -> holder.bind(item as RelationTextValueView.Phone)
            is RelationEmailHolder -> holder.bind(item as RelationTextValueView.Email)
            is RelationUrlHolder -> holder.bind(item as RelationTextValueView.Url)
            is RelationNumberHolder -> holder.bind(item as RelationTextValueView.Number)
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