package com.anytypeio.anytype.ui.linking

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_ui.widgets.toolbar.adapter.ObjectLinksAdapter
import com.anytypeio.anytype.core_utils.clipboard.parseUrlFromClipboard
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.databinding.FragmentLinkToObjectOrWebBinding
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModel
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModelFactory
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LinkToObjectOrWebPagesFragment() :
    BaseBottomSheetFragment<FragmentLinkToObjectOrWebBinding>() {

    private val vm by viewModels<LinkToObjectOrWebViewModel> { factory }

    @Inject
    lateinit var factory: LinkToObjectOrWebViewModelFactory

    private val clearSearchText: View get() = binding.searchView.root.findViewById(R.id.clearSearchText)
    private val filterInputField: EditText get() = binding.searchView.root.findViewById(R.id.filterInputField)

    private val ctx get() = arg<String>(CTX_KEY)
    private val blockId get() = arg<String>(BLOCK_KEY)
    private val rangeStart get() = arg<Int>(RANGE_START_KEY)
    private val rangeEnd get() = arg<Int>(RANGE_END_KEY)
    private val isWholeBlockMarkup get() = arg<Boolean>(UPDATE_WHOLE_BLOCK_KEY)

    private val objectLinksAdapter by lazy {
        ObjectLinksAdapter(onClicked = { vm.onClicked(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        setTransparent()
        BottomSheetBehavior.from(binding.sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            skipCollapsed = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) dismiss()
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                }
            )
        }
        with(filterInputField) {
            setHint(R.string.paste_link_or_search)
            imeOptions = EditorInfo.IME_ACTION_DONE
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return@setOnEditorActionListener false
                }
                true
            }
        }
        binding.recyclerView.invisible()
        binding.tvScreenStateMessage.invisible()
        binding.progressBar.invisible()
        clearSearchText.setOnClickListener {
            filterInputField.setText(ObjectSearchFragment.EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = objectLinksAdapter
        }
        filterInputField.showKeyboard()
    }

    override fun onStart() {
        lifecycleScope.launch {
            jobs += subscribe(vm.viewState) { state(it) }
            jobs += subscribe(vm.commands) { execute(it) }
            jobs += subscribe(filterInputField.textChanges()) { newText ->
                vm.onSearchTextChanged(newText.toString())
                if (newText.isEmpty()) {
                    clearSearchText.invisible()
                } else {
                    clearSearchText.visible()
                }
            }
        }
        super.onStart()
        vm.onStart(
            blockId = blockId,
            rangeStart = rangeStart,
            rangeEnd = rangeEnd,
            clipboardUrl = context?.parseUrlFromClipboard()
        )
        expand()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    private fun execute(command: LinkToObjectOrWebViewModel.Command) {
        Timber.d("execute, command:[$command]")
        when (command) {
            LinkToObjectOrWebViewModel.Command.Exit -> {
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.SetUrlAsLink -> {
                withParent<OnFragmentInteractionListener> {
                    if (isWholeBlockMarkup) {
                        onSetBlockWebLink(
                            blockId = blockId,
                            link = command.url
                        )
                    } else {
                        onSetWebLink(
                            link = command.url
                        )
                    }
                }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.SetObjectAsLink -> {
                withParent<OnFragmentInteractionListener> {
                    if (isWholeBlockMarkup) {
                        onSetBlockObjectLink(blockId = blockId, objectId = command.objectId)
                    } else {
                        onSetObjectLink(
                            objectId = command.objectId
                        )
                    }
                }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.CreateAndSetObjectAsLink -> {
                withParent<OnFragmentInteractionListener> {
                    onCreateObject(name = command.objectName)
                }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.CopyLink -> {
                withParent<OnFragmentInteractionListener> { onCopyLink(command.link) }
                hideSoftInput()
                dismiss()
            }
            LinkToObjectOrWebViewModel.Command.RemoveLink -> {
                withParent<OnFragmentInteractionListener> {
                    onRemoveMarkupLinkClicked(
                        blockId = blockId,
                        range = IntRange(rangeStart, rangeEnd)
                    )
                }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.OpenObject -> {
                withParent<OnFragmentInteractionListener> { onMentionClicked(target = command.objectId) }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.OpenUrl -> {
                try {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(command.url)
                    }.let {
                        startActivity(it)
                    }
                } catch (e: Throwable) {
                    toast("Couldn't parse url: ${command.url}")
                }
                hideSoftInput()
                dismiss()
            }
        }
    }

    private fun state(state: LinkToObjectOrWebViewModel.ViewState) {
        when (state) {
            LinkToObjectOrWebViewModel.ViewState.Init -> {
                binding.recyclerView.invisible()
            }
            is LinkToObjectOrWebViewModel.ViewState.Success -> {
                binding.recyclerView.visible()
                objectLinksAdapter.submitList(state.items)
            }
            is LinkToObjectOrWebViewModel.ViewState.SetFilter -> {
                filterInputField.setText(state.filter)
            }
            is LinkToObjectOrWebViewModel.ViewState.ErrorSelectedBlock -> {
                toast(getString(R.string.error_find_block))
                dismiss()
            }
            LinkToObjectOrWebViewModel.ViewState.ErrorSelection -> {
                toast(getString(R.string.error_block_selection))
                dismiss()
            }
        }
    }

    private fun setupFullHeight() {
        val lp = (binding.root.layoutParams as FrameLayout.LayoutParams)
        val metrics = Resources.getSystem().displayMetrics
        lp.height = metrics.heightPixels - requireActivity().statusBarHeight
        binding.root.layoutParams = lp
    }

    private fun setTransparent() {
        with(binding.root) {
            background = null
            (parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun injectDependencies() {
        componentManager().linkToObjectOrWebComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkToObjectOrWebComponent.release(ctx)
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLinkToObjectOrWebBinding = FragmentLinkToObjectOrWebBinding.inflate(
        inflater, container, false
    )

    companion object {
        const val CTX_KEY = "arg.link-to.ctx"
        const val BLOCK_KEY = "arg.link-to.block.id"
        const val RANGE_START_KEY = "arg.link-to.start"
        const val RANGE_END_KEY = "arg.link-to.end"
        const val UPDATE_WHOLE_BLOCK_KEY = "arg.link-to.update.block"

        fun newInstance(
            ctx: Id,
            blockId: Id,
            rangeStart: Int,
            rangeEnd: Int,
            isWholeBlockMarkup: Boolean
        ) =
            LinkToObjectOrWebPagesFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to ctx,
                    BLOCK_KEY to blockId,
                    RANGE_START_KEY to rangeStart,
                    RANGE_END_KEY to rangeEnd,
                    UPDATE_WHOLE_BLOCK_KEY to isWholeBlockMarkup
                )
            }
    }
}