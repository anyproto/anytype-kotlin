package com.anytypeio.anytype.ui.templates

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.anytypeio.anytype.presentation.templates.TemplateSelectViewModel

class TemplateSelectAdapter(
    private var templateViews: List<TemplateSelectViewModel.TemplateView>,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    fun update(templates: List<TemplateSelectViewModel.TemplateView>) {
        templateViews = templateViews + templates
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = templateViews.size

    override fun createFragment(position: Int): Fragment {
        return when (val templateView = templateViews[position]) {
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