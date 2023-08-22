package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.ui.editor.EditorFragment

class EditorTemplateFragment: EditorFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recycler.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = dimen(R.dimen.default_toolbar_height)
        }
        binding.topToolbar.apply {
            container.alpha = 1f
        }
        binding.recycler.apply {
            removeOnScrollListener(titleVisibilityDetector)
        }
        binding.topToolbar.title.updateLayoutParams<LinearLayout.LayoutParams> {
            width = LinearLayout.LayoutParams.WRAP_CONTENT
            height = LinearLayout.LayoutParams.MATCH_PARENT
        }
        binding.topToolbar.title.apply {
            setTextAppearance(R.style.TextView_UXStyle_Titles_2_Medium)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.topToolbar.setStyle(false)
    }

    override fun onStart() {
        super.onStart()
        binding.topToolbar.title.text = "Edit Template"
        jobs.clear()
    }

    override fun resetDocumentTitle(state: ViewState.Success) {}

    companion object {
        fun newInstance(id: String): EditorTemplateFragment = EditorTemplateFragment().apply {
            arguments = bundleOf(ID_KEY to id)
        }
    }
}