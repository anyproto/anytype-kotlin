package com.anytypeio.anytype.ui.templates

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.features.editor.BlockAdapter
import com.anytypeio.anytype.core_ui.features.editor.DragAndDropAdapterDelegate
import com.anytypeio.anytype.core_ui.tools.ClipboardInterceptor
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.argInt
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.databinding.FragmentTemplateBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.templates.TemplateBlankViewModel
import com.anytypeio.anytype.presentation.templates.TemplateBlankViewModelFactory
import java.util.LinkedList
import javax.inject.Inject
import timber.log.Timber

class TemplateBlankFragment : BaseFragment<FragmentTemplateBinding>(R.layout.fragment_template),
    ClipboardInterceptor {

    @Inject
    lateinit var factory: TemplateBlankViewModelFactory

    val vm by viewModels<TemplateBlankViewModel> { factory }

    private val typeId: String get() = arg(OBJECT_TYPE_ID_KEY)
    private val typeName: String get() = arg(OBJECT_TYPE_NAME_KEY)
    private val layout: Int get() = argInt(OBJECT_LAYOUT_KEY)

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
            clipboardInterceptor = object : ClipboardInterceptor {
                override fun onClipboardAction(action: ClipboardInterceptor.Action) {
                    TODO("Not yet implemented")
                }

                override fun onBookmarkPasted(url: Url) {
                    TODO("Not yet implemented")
                }
            },
            onMentionEvent = {},
            onSlashEvent = {},
            onBackPressedCallback = { false },
            onKeyPressedEvent = {},
            onDragAndDropTrigger = { _, _ -> false },
            onDragListener = { _, _ -> false },
            lifecycle = lifecycle,
            dragAndDropSelector = DragAndDropAdapterDelegate(),
            onCellSelectionChanged = { _, _ -> }
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
        with(lifecycleScope) {
            jobs += subscribe(vm.state) {
                Timber.d("TemplateBlankFragment: $it")
                templateAdapter.updateWithDiffUtil(it)
            }
        }
        super.onStart()
        vm.onStart(typeId, typeName, layout)
    }

    override fun injectDependencies() {
        componentManager().templateBlankComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().templateBlankComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTemplateBinding = FragmentTemplateBinding.inflate(
        inflater, container, false
    )

    override fun onClipboardAction(action: ClipboardInterceptor.Action) {}
    override fun onBookmarkPasted(url: Url) {}

    companion object {
        fun new(
            typeId: String,
            typeName: String,
            layout: Int
        ) = TemplateBlankFragment().apply {
            arguments = bundleOf(
                OBJECT_TYPE_ID_KEY to typeId,
                OBJECT_TYPE_NAME_KEY to typeName,
                OBJECT_LAYOUT_KEY to layout
            )
        }

        const val OBJECT_TYPE_ID_KEY = "arg.template.object_type_id"
        const val OBJECT_TYPE_NAME_KEY = "arg.template.object_type"
        const val OBJECT_LAYOUT_KEY = "arg.template.object_layout"
    }
}