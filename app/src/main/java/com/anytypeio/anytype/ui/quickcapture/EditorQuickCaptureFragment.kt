package com.anytypeio.anytype.ui.quickcapture

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.hide
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.ui.editor.EditorFragment
import timber.log.Timber

/**
 * The real block editor embedded headerless in the quick-capture sheet
 * (spec: docs/quick-capture-android-spec.md, decision D1). The sheet owns the chrome;
 * in-editor navigation closes the sheet and routes through the activity-level
 * deep-link path — nothing is pushed inside the sheet.
 *
 * Keyboard handling (spec V1): the host sheet measures the keyboard from the window's
 * visible display frame and pads the editor container, so the type bar and toolbars ride
 * the IME; the base inset-translation sync is disabled here because it does not position
 * these views inside a dialog window.
 */
class EditorQuickCaptureFragment : EditorFragment() {

    override val navigationDestinationId: Int = R.id.quickCaptureScreen

    // The sheet owns its chrome; the editor's create/search FABs and the discussion button
    // would float over the type-selection bar.
    override val showsBottomActionButtons: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        vm.enableQuickCaptureMode()
        super.onViewCreated(view, savedInstanceState)
        binding.topToolbar.hide()
        binding.recycler.addItemDecoration(firstItemPullUpDecoration)
        // The editor's generic loading skeleton shimmers a full-width cover and a 72dp icon
        // placeholder (object_loading_state.xml). A quick-capture draft never has either, so
        // that preview flashes chrome the object cannot have. The draft is created/validated
        // before the editor is attached, so there is nothing to wait on visually.
        binding.loadingContainer.root.gone()
    }

    /**
     * The title block item reserves ~80dp above the title for the full-screen editor's
     * icon/cover area; inside the sheet (own compact header) that reads as a hole.
     * Pull the first item up so the document starts ~24dp below the sheet header.
     */
    private val firstItemPullUpDecoration = object : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = (-56 * resources.displayMetrics.density).toInt()
            }
        }
    }

    override fun saveAsLastOpened(): Boolean = false

    override fun observeSelectingTemplate() {
        // Quick capture never opens the template picker (no ShouldSelectTemplate flag), and
        // the base implementation expects pageScreen on the back stack, which this graph
        // does not have.
    }

    override fun onApplyWindowRootInsets() {
        // No inset handling here: window insets do not reliably reach this child view
        // inside the sheet (verified on device), and the base deferring-callback would
        // also pad by system bars. QuickCaptureFragment measures the keyboard from the
        // window's visible display frame and pads the editor container instead (spec V1).
    }

    override fun setupWindowInsetAnimation() {
        // See onApplyWindowRootInsets — the host's container padding positions the
        // bottom widgets; the base translation sync would shift them off-screen.
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.navigation.removeObserver(navObserver)
        vm.navigation.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { command ->
                proceedWithQuickCaptureNavigation(command)
            }
        }
    }

    private fun proceedWithQuickCaptureNavigation(command: AppNavigation.Command) {
        when (command) {
            is AppNavigation.Command.OpenObject -> openOutside(command.target, command.space)
            is AppNavigation.Command.LaunchDocument -> openOutside(command.target, command.space)
            is AppNavigation.Command.OpenSetOrCollection -> openOutside(command.target, command.space)
            is AppNavigation.Command.LaunchObjectSet -> openOutside(command.target, command.space)
            is AppNavigation.Command.OpenChat -> openOutside(command.target, command.space, isChat = true)
            is AppNavigation.Command.OpenDiscussion -> openOutside(command.target, command.space, isChat = true)
            is AppNavigation.Command.OpenDateObject -> openOutside(command.objectId, command.space)
            is AppNavigation.Command.OpenTypeObject -> openOutside(command.target, command.space)
            is AppNavigation.Command.OpenParticipant -> openOutside(command.objectId, command.space)
            else -> {
                Timber.d("Quick capture: ignoring in-sheet navigation command: $command")
            }
        }
    }

    private fun openOutside(target: Id, space: Id, isChat: Boolean = false) {
        (parentFragment as? QuickCaptureFragment)?.onOpenObjectRequested(
            target = target,
            space = space,
            isChat = isChat
        )
    }

    fun onDocumentMenuClicked() {
        vm.onDocumentMenuClicked()
    }

    /** Live emptiness of the draft, ahead of the editor's text debounce. */
    fun hasContent(): Boolean = vm.hasQuickCaptureContent()

    /**
     * Whether the user typed into this draft during this session — a restored draft reports
     * content but no edits, which is what separates "carry my note across" from "I only
     * opened it".
     */
    fun hasEdits(): Boolean = vm.hasQuickCaptureEdits()

    suspend fun flushPendingText() = vm.flushPendingTextChanges()

    companion object {
        fun newInstance(ctx: Id, space: Id): EditorQuickCaptureFragment =
            EditorQuickCaptureFragment().apply {
                arguments = bundleOf(
                    CTX_KEY to ctx,
                    SPACE_ID_KEY to space
                )
            }
    }
}
