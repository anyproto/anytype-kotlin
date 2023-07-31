package com.anytypeio.anytype.ui.templates

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel

class TemplateSelectAdapter(
    private var items: List<TemplateSelectViewModel.TemplateView>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    fun update(newItems: List<TemplateSelectViewModel.TemplateView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun createFragment(position: Int): Fragment {
        return when (val templateView = items[position]) {
            is TemplateSelectViewModel.TemplateView.Blank -> TemplateBlankFragment.new(
                typeId = templateView.typeId,
                typeName = templateView.typeName,
                layout = templateView.layout
            )
            is TemplateSelectViewModel.TemplateView.Template -> TemplateFragment.new(
                templateView.id
            )
        }
    }
}