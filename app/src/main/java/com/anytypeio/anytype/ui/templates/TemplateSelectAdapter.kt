package com.anytypeio.anytype.ui.templates

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.presentation.templates.TemplateSelectView

class TemplateSelectAdapter(
    private var items: List<TemplateSelectView>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    fun update(newItems: List<TemplateSelectView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return when (val templateView = items[position]) {
            is TemplateSelectView.Blank -> TemplateBlankFragment.new(
                typeId = templateView.typeId,
                typeName = templateView.typeName,
                layout = templateView.layout
            )
            is TemplateSelectView.Template -> TemplateFragment.new(
                templateView.id
            )
        }
    }
}