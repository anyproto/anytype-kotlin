package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentTemplateBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.templates.TemplateViewModel
import java.util.*
import javax.inject.Inject

class TemplateFragment : BaseFragment<FragmentTemplateBinding>(R.layout.fragment_template),
    ClipboardInterceptor, View.OnDragListener {

    private val ctx: String get() = arg(CTX_KEY)

    @Inject
    lateinit var factory: TemplateViewModel.Factory

    private val vm by viewModels<TemplateViewModel> { factory }

    private val templateAdapter by lazy {
        BlockAdapter(
            restore = LinkedList(),
            initialBlock = mutableListOf(),
            onTextChanged = { _, _ -> },
            onTextBlockTextChanged = {},
            onDescriptionChanged = { },
            onTitleBlockTextChanged = { _, _ -> },
            onSelectionChanged = { _, _ -> },
            onCheckboxClicked = {},
            onTitleCheckboxClicked = {},
            onFocusChanged = { _, _ -> },
            onSplitLineEnterClicked = { _, _, _ -> },
            onSplitDescription = { _, _, _ -> },
            onEmptyBlockBackspaceClicked = {},
            onNonEmptyBlockBackspaceClicked = { _, _ -> },
            onTextInputClicked = { },
            onPageIconClicked = {},
            onCoverClicked = { },
            onTogglePlaceholderClicked = { },
            onToggleClicked = {},
            onTitleTextInputClicked = {},
            onClickListener = {},
            clipboardInterceptor = this,
            onMentionEvent = {},
            onSlashEvent = {},
            onBackPressedCallback = { false },
            onKeyPressedEvent = {},
            onDragAndDropTrigger = { _, _ -> false },
            onDragListener = this,
            lifecycle = lifecycle,
            dragAndDropSelector = DragAndDropAdapterDelegate()
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.templateRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = templateAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart(ctx)
    }

    override fun onResume() {
        super.onResume()
        with(lifecycleScope) {
            jobs += subscribe(vm.state) { templateAdapter.updateWithDiffUtil(it) }
        }
    }

    override fun injectDependencies() {
        componentManager().templateComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().templateComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTemplateBinding = FragmentTemplateBinding.inflate(
        inflater, container, false
    )

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
    override fun onBookmarkPasted(url: Url) {}
    override fun onDrag(v: View?, event: DragEvent?) = false

    companion object {
        fun new(ctx: Id) = TemplateFragment().apply {
            arguments = bundleOf(CTX_KEY to ctx)
        }

        const val CTX_KEY = "arg.template.ctx"
    }
}