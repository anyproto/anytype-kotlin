package com.anytypeio.anytype.ui.templates

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.presentation.templates.TemplateView

class TemplateSelectAdapter(
    private var items: List<TemplateView>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    fun update(newItems: List<TemplateView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return when (val templateView = items[position]) {
            is TemplateView.Blank -> TemplateBlankFragment.new(
                typeId = templateView.typeId,
                typeName = templateView.typeName,
                layout = templateView.layout
            )
            is TemplateView.Template -> TemplateFragment.new(
                templateView.id
            )
        }
    }
}