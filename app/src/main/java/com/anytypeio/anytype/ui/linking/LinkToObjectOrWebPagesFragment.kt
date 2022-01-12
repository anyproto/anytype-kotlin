package com.anytypeio.anytype.ui.linking

import android.content.res.Resources
import android.graphics.Color
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
import com.anytypeio.anytype.core_ui.reactive.textChanges
import com.anytypeio.anytype.core_ui.widgets.toolbar.adapter.ObjectLinksAdapter
import com.anytypeio.anytype.core_utils.ext.*
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModel
import com.anytypeio.anytype.presentation.linking.LinkToObjectOrWebViewModelFactory
import com.anytypeio.anytype.ui.editor.OnFragmentInteractionListener
import com.anytypeio.anytype.ui.search.ObjectSearchFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_link_to_object_or_web.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LinkToObjectOrWebPagesFragment() : BaseBottomSheetFragment() {

    private val vm by viewModels<LinkToObjectOrWebViewModel> { factory }

    @Inject
    lateinit var factory: LinkToObjectOrWebViewModelFactory

    private val clearSearchText: View get() = searchView.findViewById(R.id.clearSearchText)
    private val filterInputField: EditText get() = searchView.findViewById(R.id.filterInputField)
    private val uri get() = arg<String>(LINK_TO_OBJ_OR_WEB_FILTER_ARG)

    private val objectLinksAdapter by lazy {
        ObjectLinksAdapter(onClicked = { vm.onClicked(it) })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_link_to_object_or_web, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFullHeight()
        setTransparent()
        BottomSheetBehavior.from(sheet).apply {
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
        recyclerView.invisible()
        tvScreenStateMessage.invisible()
        progressBar.invisible()
        clearSearchText.setOnClickListener {
            filterInputField.setText(ObjectSearchFragment.EMPTY_FILTER_TEXT)
            clearSearchText.invisible()
        }
        with(recyclerView) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = objectLinksAdapter
        }
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
        vm.onStart(uri)
        expand()
    }

    private fun execute(command: LinkToObjectOrWebViewModel.Command) {
        Timber.d("execute, command:[$command]")
        when (command) {
            LinkToObjectOrWebViewModel.Command.Exit -> {
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.SetWebLink -> {
                withParent<OnFragmentInteractionListener> { onSetWebLink(command.url) }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.SetObjectLink -> {
                withParent<OnFragmentInteractionListener> { onSetObjectLink(command.target) }
                hideSoftInput()
                dismiss()
            }
            is LinkToObjectOrWebViewModel.Command.CreateObject -> {
                withParent<OnFragmentInteractionListener> { onCreateObject(command.name) }
                hideSoftInput()
                dismiss()
            }
        }
    }

    private fun state(state: LinkToObjectOrWebViewModel.ViewState) {
        when (state) {
            LinkToObjectOrWebViewModel.ViewState.Init -> {
                recyclerView.invisible()
            }
            is LinkToObjectOrWebViewModel.ViewState.Success -> {
                recyclerView.visible()
                objectLinksAdapter.submitList(state.items)
            }
            is LinkToObjectOrWebViewModel.ViewState.SetFilter -> {
                filterInputField.setText(state.filter)
            }
        }
    }

    private fun setupFullHeight() {
        val lp = (root.layoutParams as FrameLayout.LayoutParams)
        val metrics = Resources.getSystem().displayMetrics
        lp.height = metrics.heightPixels - requireActivity().statusBarHeight
        root.layoutParams = lp
    }

    private fun setTransparent() {
        with(root) {
            background = null
            (parent as? View)?.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun injectDependencies() {
        componentManager().linkToObjectOrWebComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().linkToObjectOrWebComponent.release()
    }

    companion object {
        const val LINK_TO_OBJ_OR_WEB_FILTER_ARG = "link-to-object-or-web.filter.arg"

        fun newInstance(filter: String) = LinkToObjectOrWebPagesFragment().apply {
            arguments = bundleOf(LINK_TO_OBJ_OR_WEB_FILTER_ARG to filter)
        }
    }
}