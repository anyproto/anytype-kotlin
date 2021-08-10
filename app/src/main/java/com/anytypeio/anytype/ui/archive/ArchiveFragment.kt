package com.anytypeio.anytype.ui.archive

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.features.archive.ArchiveAdapter
import com.anytypeio.anytype.core_ui.reactive.clicks
import com.anytypeio.anytype.core_ui.tools.FirstItemInvisibilityDetector
import com.anytypeio.anytype.core_utils.ext.hideSoftInput
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.editor.archive.ArchiveViewModel
import com.anytypeio.anytype.presentation.editor.archive.ArchiveViewModelFactory
import com.anytypeio.anytype.presentation.editor.archive.ArchiveViewState
import com.anytypeio.anytype.ui.base.NavigationFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.fragment_archive.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

open class ArchiveFragment : NavigationFragment(R.layout.fragment_archive) {

    @Inject
    lateinit var factory: ArchiveViewModelFactory

    private val vm by viewModels<ArchiveViewModel> { factory }

    private val archiveAdapter by lazy {
        ArchiveAdapter(
            blocks = mutableListOf(),
            onClickListener = { vm.onPageClicked(it) }
        )
    }

    private val titleVisibilityDetector by lazy {
        FirstItemInvisibilityDetector { isVisible ->
            if (isVisible) {
                topToolbar.title.invisible()
                topToolbar.container.invisible()
            } else {
                topToolbar.title.visible()
                topToolbar.container.visible()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm.state.observe(viewLifecycleOwner, { render(it) })
        vm.navigation.observe(viewLifecycleOwner, navObserver)

        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            isHideable = true
            addBottomSheetCallback(
                object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                            activity?.hideSoftInput()
                            vm.onBottomSheetHidden()
                        }
                    }
                }
            )
        }

        topToolbar.menu.invisible()

        topToolbar.back.clicks().onEach {
            hideSoftInput()
            vm.onBackButtonPressed()
        }.launchIn(lifecycleScope)

        topToolbar.undo.invisible()
        topToolbar.redo.invisible()

        with(bottomMenu) {
            update(COUNTER_INIT)
            findViewById<TextView>(R.id.btnPutBack).setOnClickListener {
                vm.onPutBackClicked()
            }
        }

        recycler.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = archiveAdapter
            addOnScrollListener(titleVisibilityDetector)
        }
    }

    private fun render(state: ArchiveViewState) {
        when (state) {
            ArchiveViewState.Loading -> {}
            is ArchiveViewState.Success -> {
                archiveAdapter.update(state.blocks)
                bottomMenu.update(state.selections)
            }
        }
    }

    override fun onStart() {
        vm.onStart(extractDocumentId())
        super.onStart()
    }

    override fun onStop() {
        vm.onStop()
        super.onStop()
    }

    override fun injectDependencies() {
        componentManager().archiveComponent.get(extractDocumentId()).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().archiveComponent.release(extractDocumentId())
    }

    private fun extractDocumentId(): String =
        requireArguments()
            .getString(ID_KEY)
            ?: throw IllegalStateException("Document id missing")

    companion object {
        const val ID_KEY = "args.id"
        const val COUNTER_INIT = 0
    }
}