package com.agileburo.anytype.ui.page

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.features.page.BlockAdapter
import com.agileburo.anytype.core_ui.state.ControlPanelState
import com.agileburo.anytype.core_ui.widgets.toolbar.ColorToolbarWidget
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.Option
import com.agileburo.anytype.core_ui.widgets.toolbar.OptionToolbarWidget.OptionConfig
import com.agileburo.anytype.core_utils.ext.*
import com.agileburo.anytype.di.common.componentManager
import com.agileburo.anytype.ext.extractMarks
import com.agileburo.anytype.presentation.page.PageViewModel
import com.agileburo.anytype.presentation.page.PageViewModelFactory
import com.agileburo.anytype.ui.base.NavigationFragment
import kotlinx.android.synthetic.main.fragment_page.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class PageFragment : NavigationFragment(R.layout.fragment_page) {

    private val pageAdapter by lazy {
        BlockAdapter(
            blocks = mutableListOf(),
            onTextChanged = { id, editable ->
                vm.onTextChanged(id, editable.toString(), editable.extractMarks())
            },
            onSelectionChanged = { id, selection ->
                vm.onSelectionChanged(
                    id = id,
                    selection = selection
                )
            }
        )
    }

    private val vm by lazy {
        ViewModelProviders
            .of(this, factory)
            .get(PageViewModel::class.java)
    }

    @Inject
    lateinit var factory: PageViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vm.open(requireArguments().getString(ID_KEY, ID_EMPTY_VALUE))

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(this) {
                vm.onSystemBackPressed()
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = pageAdapter
        }

        toolbar
            .keyboardClicks()
            .onEach { hideSoftInput() }
            .launchIn(lifecycleScope)

        toolbar
            .addButtonClicks()
            .onEach { vm.onAddBlockToolbarClicked() }
            .launchIn(lifecycleScope)

        markupToolbar
            .markupClicks()
            .onEach { vm.onMarkupActionClicked(it) }
            .launchIn(lifecycleScope)

        markupToolbar
            .hideKeyboardClicks()
            .onEach { hideSoftInput() }
            .launchIn(lifecycleScope)

        markupToolbar
            .colorClicks()
            .onEach { vm.onMarkupToolbarColorClicked() }
            .launchIn(lifecycleScope)

        colorToolbar
            .observeClicks()
            .onEach { click ->
                if (click is ColorToolbarWidget.Click.OnTextColorClicked)
                    vm.onMarkupTextColorAction(color = click.color.hexColorCode())
                else
                    requireActivity().toast("Not implemented")
            }
            .launchIn(lifecycleScope)

        optionToolbar
            .optionClicks()
            .onEach { option ->
                when (option) {
                    is Option.Text -> {
                        if (option.type == OptionConfig.OPTION_TEXT_TEXT)
                            vm.onAddTextBlockClicked()
                        else
                            requireActivity().toast("Not implemented")
                    }
                }
            }
            .launchIn(lifecycleScope)

    }

    private fun showColorToolbar() {
        colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        colorToolbar.visible()
    }

    private fun showOptionToolbar() {
        optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        optionToolbar.visible()
    }

    private fun hideColorToolbar() {
        colorToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
        colorToolbar.invisible()
    }

    private fun hideOptionToolbar() {
        optionToolbar.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, 0)
        optionToolbar.invisible()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, Observer { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)
        vm.controlPanelViewState.observe(viewLifecycleOwner, Observer { render(it) })
    }

    private fun render(state: PageViewModel.ViewState) {
        when (state) {
            is PageViewModel.ViewState.Success -> {
                pageAdapter.updateWithDiffUtil(state.blocks)
            }
        }
    }

    private fun render(state: ControlPanelState) {
        markupToolbar.setState(state.markupToolbar)
        toolbar.setState(state.blockToolbar)
        with(state.colorToolbar) {
            if (isVisible) {
                hideSoftInput()
                lifecycleScope.launch {
                    delay(300)
                    showColorToolbar()
                }
            } else
                hideColorToolbar()
        }
        with(state.addBlockToolbar) {
            if (isVisible) {
                hideSoftInput()
                lifecycleScope.launch {
                    delay(300)
                    showOptionToolbar()
                }
            } else
                hideOptionToolbar()
        }
    }

    override fun injectDependencies() {
        componentManager().pageComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().pageComponent.release()
    }

    companion object {
        const val ID_KEY = "id"
        const val ID_EMPTY_VALUE = ""
    }
}