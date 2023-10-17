package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.ui.editor.EditorFragment
import timber.log.Timber

class EditorTemplateFragment : EditorFragment() {

    private val targetObjectType get() = arg<Id>(ARG_TARGET_OBJECT_TYPE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeBinding()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        binding.topToolbar.setStyle(false)
    }

    override fun onStart() {
        super.onStart()
        binding.topToolbar.title.text = getString(R.string.templates_menu_edit)
        jobs.clear()
    }

    override fun onApplyWindowRootInsets() {}
    override fun setupWindowInsetAnimation() {}

    override fun resetDocumentTitle(state: ViewState.Success) {}

    override fun render(state: ControlPanelState) {
        super.render(state)
        if (state.navigationToolbar.isVisible) {
            binding.btnSelectTemplate.visible()
        } else {
            binding.btnSelectTemplate.gone()
        }
    }

    private fun initializeBinding() {
        with(binding) {
            root.background = null
            recycler.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topMargin = dimen(R.dimen.default_toolbar_height)
            }
            topToolbar.apply {
                container.alpha = 1f
            }
            recycler.removeOnScrollListener(titleVisibilityDetector)
            topToolbar.title.updateLayoutParams<LinearLayout.LayoutParams> {
                width = LinearLayout.LayoutParams.WRAP_CONTENT
                height = LinearLayout.LayoutParams.MATCH_PARENT
            }
            topToolbar.title.setTextAppearance(R.style.TextView_UXStyle_Titles_2_Medium)
            btnSelectTemplate.setOnClickListener {
                Timber.d("Select template clicked, get back to Set")
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.apply {
                        set(ARG_TEMPLATE_ID, ctx)
                        set(ARG_TARGET_OBJECT_TYPE, targetObjectType)
                    }
                    popBackStack(R.id.editorModalScreen, true)
                }
            }
        }
    }

    override fun observeSelectingTemplate() {
        // Do nothing
    }

    companion object {
        fun newInstance(id: String, targetObjectType: Id): EditorTemplateFragment =
            EditorTemplateFragment().apply {
                arguments = bundleOf(
                    ID_KEY to id,
                    ARG_TARGET_OBJECT_TYPE to targetObjectType
                )
            }

        const val ARG_TEMPLATE_ID = "template_id"
        const val ARG_TARGET_OBJECT_TYPE = "target_object_type"
    }
}