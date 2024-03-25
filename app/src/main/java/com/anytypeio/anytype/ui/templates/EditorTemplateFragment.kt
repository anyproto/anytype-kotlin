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
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.control.ControlPanelState
import com.anytypeio.anytype.ui.editor.EditorFragment
import timber.log.Timber

class EditorTemplateFragment : EditorFragment() {

    private val targetTypeId get() = arg<Id>(ARG_TARGET_TYPE_ID)
    private val targetTypeKey get() = arg<Id>(ARG_TARGET_TYPE_KEY)
    private val fragmentType get() = argInt(ARG_TEMPLATE_TYPE)

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
        jobs.clear()
    }

    override fun saveAsLastOpened(): Boolean {
        return false
    }

    override fun onApplyWindowRootInsets() {}
    override fun setupWindowInsetAnimation() {}

    override fun resetDocumentTitle(state: ViewState.Success) {}

    override fun render(state: ControlPanelState) {
        super.render(state)
        when (fragmentType) {
            TYPE_TEMPLATE_SELECT, TYPE_TEMPLATE_MULTIPLE -> {
                binding.bottomToolbar.hide()
                if (state.navigationToolbar.isVisible) {
                    binding.btnSelectTemplate.visible()
                } else {
                    binding.btnSelectTemplate.gone()
                }
            }
            TYPE_TEMPLATE_EDIT -> {
                binding.bottomToolbar.hide()
                binding.btnSelectTemplate.gone()
            }
        }

    }

    private fun initializeBinding() {
        with(binding) {
            if (fragmentType == TYPE_TEMPLATE_SELECT || fragmentType == TYPE_TEMPLATE_EDIT) {
                recycler.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    topMargin = dimen(R.dimen.default_toolbar_height)
                }
                binding.topToolbar.title.text = getString(R.string.templates_menu_edit)
                binding.topToolbar.statusContainer.hide()
            } else {
                binding.topToolbar.hide()
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
                Timber.d("Select template clicked, get back to Set or Editor")
                findNavController().apply {
                    previousBackStackEntry?.savedStateHandle?.apply {
                        set(ARG_TEMPLATE_ID, ctx)
                        set(ARG_TARGET_TYPE_ID, targetTypeId)
                        set(ARG_TARGET_TYPE_KEY, targetTypeKey)
                    }
                    when (fragmentType) {
                        TYPE_TEMPLATE_SELECT -> popBackStack(R.id.editorModalScreen, true)
                        TYPE_TEMPLATE_MULTIPLE -> {
                            vm.onSelectTemplateClicked()
                            popBackStack(R.id.templatesModalScreen, true)
                        }
                    }
                }
            }
        }
    }

    override fun observeSelectingTemplate() {
        // Do nothing
    }

    fun onDocumentMenuClicked() {
        vm.onDocumentMenuClicked()
    }

    companion object {
        fun newInstance(
            id: String,
            targetTypeId: Id,
            targetTypeKey: Id,
            type: Int
        ): EditorTemplateFragment =
            EditorTemplateFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to id,
                    ARG_TARGET_TYPE_ID to targetTypeId,
                    ARG_TARGET_TYPE_KEY to targetTypeKey,
                    ARG_TEMPLATE_TYPE to type
                )
            }

        const val ARG_TEMPLATE_ID = "template_id"
        const val ARG_TARGET_TYPE_ID = "target_type_id"
        const val ARG_TARGET_TYPE_KEY = "target_type_key"
        const val ARG_TEMPLATE_TYPE = "template_type"
        const val TYPE_TEMPLATE_EDIT = 1
        const val TYPE_TEMPLATE_SELECT = 2
        const val TYPE_TEMPLATE_MULTIPLE = 3
    }
}