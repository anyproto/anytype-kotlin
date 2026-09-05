package com.anytypeio.anytype.ui.quickcapture

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.updatePadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.syncstatus.SpaceSyncStatusScreen
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.core_utils.ui.proceed
import com.anytypeio.anytype.databinding.FragmentQuickCaptureBinding
import com.anytypeio.anytype.di.common.componentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.anytypeio.anytype.presentation.main.MainViewModel
import com.anytypeio.anytype.presentation.quickcapture.QuickCaptureViewModel
import com.anytypeio.anytype.ui.editor.EditorFragment
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Quick-capture sheet over the vault (spec: docs/quick-capture-android-spec.md):
 * a full-height bottom sheet with a sheet-owned header row and the real editor
 * embedded headerless as a child fragment. Swipe-down keeps the draft.
 */
class QuickCaptureFragment : BaseBottomSheetFragment<FragmentQuickCaptureBinding>() {

    @Inject
    lateinit var factory: QuickCaptureViewModel.Factory

    private val vm by viewModels<QuickCaptureViewModel> { factory }

    private val mainVm: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSecondarySheetDialogTheme)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        skipCollapsed()
        setFullHeightSheet()
        // The sheet's own rounded background paints the status-bar inset strip grey
        // (background_secondary). The layout paints its own surface, so drop it and let
        // that strip stay transparent over the dimmed vault.
        sheet?.background = null
        // Push the vault further back: the transparent strip above the sheet makes the
        // Material default dim read as too light here. Scoped to this dialog so other
        // sheets sharing the theme are unaffected.
        dialog?.window?.setDimAmount(SCRIM_DIM_AMOUNT)
        // Without this a programmatic dismiss() skips the sheet animation entirely and
        // plays the window exit animation instead — which we cannot sequence against.
        (dialog as? BottomSheetDialog)?.dismissWithAnimation = true
        // Spec V1: these sheet themes declare adjustResize, but the window does NOT resize
        // for the IME, and window insets don't reliably reach the embedded editor's views
        // (both verified on device). Measure the keyboard from the visible display frame
        // instead and pad the editor container, so the editor's bottom-gravity type bar
        // and style toolbar ride the keyboard.
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(imeLayoutListener)
        // Dismissing with the keyboard up looks broken if the IME collapses only after the
        // sheet has gone: two unsynchronized animations plus a window resize. Retract the
        // IME the instant the drag starts, so it travels with the sheet.
        sheet?.let { view ->
            BottomSheetBehavior.from(view).addBottomSheetCallback(dragCallback)
        }
        binding.quickCaptureHeader.setContent {
            QuickCaptureChrome()
        }
    }

    private val imeLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (!hasBinding()) return@OnGlobalLayoutListener
        val root = binding.root
        val frame = Rect()
        root.getWindowVisibleDisplayFrame(frame)
        // Everything below the visible frame — the IME when it is up, otherwise just the
        // gesture/navigation bar. Padding by the full amount puts the editor's bottom
        // widgets exactly on the keyboard's top edge, and clear of the gesture bar when
        // the keyboard is down.
        val covered = (root.rootView.height - frame.bottom).coerceAtLeast(0)
        if (binding.quickCaptureEditorContainer.paddingBottom != covered) {
            binding.quickCaptureEditorContainer.updatePadding(bottom = covered)
        }
        // frame.top is the status-bar height: keep the sheet surface below it so the strip
        // above the drag handle stays transparent.
        if (root.paddingTop != frame.top) {
            root.updatePadding(top = frame.top)
        }
    }

    private val dragCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            // Only on an actual dismissal. STATE_DRAGGING is not a commitment to close — the
            // behavior enters it as soon as the sheet moves at all, including the flick that
            // springs straight back — and hideKeyboardNow() also clears focus, so reacting to
            // it destroyed the keyboard *and* the caret on every cancelled drag. The recycler
            // sits at the top of a fresh draft, so it hands that gesture to the sheet.
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                hideKeyboardNow()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            // Fade the scrim with the sheet's travel so the vault comes back gradually
            // while the sheet slides, instead of snapping in when the window is removed.
            // slideOffset runs 1 (expanded) → -1 (hidden); the scrim must track it because
            // it is painted by the window, behind the sheet, and cannot be a child view
            // (a child would slide away together with the sheet).
            val visibleFraction = ((slideOffset + 1f) / 2f).coerceIn(0f, 1f)
            applyScrimDim(SCRIM_DIM_AMOUNT * visibleFraction)
            // Retire the keyboard once the sheet has travelled far enough that it is heading
            // for a dismissal, rather than waiting for the window to go — otherwise the IME
            // lingers and collapses in a second, separate step after the sheet has gone.
            // Keyed to distance, not to STATE_DRAGGING, so a small drag that springs back
            // keeps both the keyboard and the caret.
            if (slideOffset < KEYBOARD_RETIRE_SLIDE_OFFSET) hideKeyboardNow()
        }
    }

    private var currentDim = SCRIM_DIM_AMOUNT

    /** Quantized: a window attribute change triggers a relayout, so skip invisible deltas. */
    private fun applyScrimDim(target: Float) {
        if (kotlin.math.abs(target - currentDim) < DIM_UPDATE_THRESHOLD) return
        currentDim = target
        dialog?.window?.setDimAmount(target)
    }

    /**
     * Retracts the IME as part of dismissal (AOSP Launcher3 does exactly this from its own
     * sheet's drag start). Two details matter:
     *  - clearFocus() first, or a still-focused editor can have the IME re-shown while the
     *    window tears down (Material's SearchView pairs the two for the same reason);
     *  - WindowInsetsController rather than InputMethodManager: on API 30+ it drives a real,
     *    observable IME animation, and it still works when the input connection has already
     *    been finished.
     */
    private fun hideKeyboardNow() {
        val view = view ?: return
        val window = dialog?.window ?: return
        val imeVisible = ViewCompat.getRootWindowInsets(view)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        view.findFocus()?.clearFocus()
        if (imeVisible) {
            WindowCompat.getInsetsController(window, view)
                .hide(WindowInsetsCompat.Type.ime())
        }
    }

    private fun hasBinding(): Boolean = view != null

    override fun onDestroyView() {
        binding.root.viewTreeObserver.removeOnGlobalLayoutListener(imeLayoutListener)
        sheet?.let { view -> BottomSheetBehavior.from(view).removeBottomSheetCallback(dragCallback) }
        super.onDestroyView()
    }

    @Composable
    private fun QuickCaptureChrome() {
        val selectedSpace by vm.selectedSpace.collectAsStateWithLifecycle()
        val syncStatus by vm.syncStatus.collectAsStateWithLifecycle()
        val isDraftEmpty by vm.isDraftEmpty.collectAsStateWithLifecycle()
        QuickCaptureHeader(
            selectedSpace = selectedSpace,
            syncStatus = syncStatus,
            isDraftEmpty = isDraftEmpty,
            onSpaceChipClicked = vm::onSpaceChipClicked,
            onSyncStatusClicked = vm::onSyncStatusBadgeClicked,
            onMenuClicked = { editor()?.onDocumentMenuClicked() },
            onClearDraftClicked = vm::onClearDraftClicked,
            onSendClicked = vm::onSendClicked
        )
        val showSpacePicker by vm.showSpacePicker.collectAsStateWithLifecycle()
        if (showSpacePicker) {
            QuickCaptureSpacePicker(
                spaces = vm.spaces.collectAsStateWithLifecycle().value,
                onSpaceClicked = { target ->
                    // Wait for queued text writes to reach the middleware BEFORE selecting:
                    // a cross-space move reads the source server-side and then permanently
                    // deletes it, so acting on a stale document would drop the tail of the
                    // note. Only then read the editor's live emptiness, rather than the
                    // lagging details subscription.
                    val editor = editor()
                    viewLifecycleOwner.lifecycleScope.launch {
                        editor?.flushPendingText()
                        vm.onSpaceSelected(
                            target = target,
                            sourceHasContent = editor?.hasContent()
                        )
                    }
                },
                onDismiss = vm::onSpacePickerDismissed
            )
        }
        val draftConflict by vm.draftConflict.collectAsStateWithLifecycle()
        draftConflict?.let { conflict ->
            DraftConflictDialog(
                spaceName = conflict.spaceName,
                onKeepBoth = vm::onKeepCurrentDraftChosen,
                onDiscardCurrent = vm::onDiscardCurrentDraftChosen,
                onCancel = vm::onDraftConflictCancelled
            )
        }
        val showClearDraftConfirmation by vm.showClearDraftConfirmation.collectAsStateWithLifecycle()
        if (showClearDraftConfirmation) {
            ClearDraftConfirmation(
                onConfirm = vm::onClearDraftConfirmed,
                onCancel = vm::onClearDraftCancelled
            )
        }
        SpaceSyncStatusScreen(
            uiState = vm.syncStatusWidget.collectAsStateWithLifecycle().value,
            onDismiss = vm::onSyncWidgetDismiss,
            onUpdateAppClick = {}
        )
    }

    override fun onStart() {
        super.onStart()
        expand()
        proceed(vm.screenState) { state -> render(state) }
        proceed(vm.commands) { command -> execute(command) }
        proceed(vm.toasts) { toast(it) }
        vm.onStart()
    }

    private fun render(state: QuickCaptureViewModel.ScreenState) {
        when (state) {
            QuickCaptureViewModel.ScreenState.Loading -> {
                // Detach the editor during transitions: keeping it live while the draft moves
                // spaces would let the user type into a document that is about to be deleted,
                // against type/relation stores that have already switched space.
                detachEditor()
            }
            is QuickCaptureViewModel.ScreenState.Ready -> {
                attachEditor(state)
            }
        }
    }

    private fun detachEditor() {
        val current = childFragmentManager.findFragmentByTag(TAG_EDITOR) ?: return
        childFragmentManager.beginTransaction()
            .remove(current)
            .commitAllowingStateLoss()
    }

    private fun attachEditor(state: QuickCaptureViewModel.ScreenState.Ready) {
        val current = childFragmentManager.findFragmentByTag(TAG_EDITOR)
        val currentCtx = current?.arguments?.getString(EditorFragment.CTX_KEY)
        if (current != null && currentCtx == state.draft) return
        childFragmentManager.beginTransaction()
            .replace(
                R.id.quickCaptureEditorContainer,
                EditorQuickCaptureFragment.newInstance(ctx = state.draft, space = state.space.id),
                TAG_EDITOR
            )
            .commitAllowingStateLoss()
    }

    private fun execute(command: QuickCaptureViewModel.Command) {
        when (command) {
            is QuickCaptureViewModel.Command.Dismiss -> {
                val result = command.result
                if (result != null) {
                    view?.let { view ->
                        ViewCompat.performHapticFeedback(
                            view,
                            HapticFeedbackConstantsCompat.CONFIRM
                        )
                    }
                    parentFragmentManager.setFragmentResult(
                        RESULT_KEY,
                        bundleOf(
                            RESULT_OBJECT_ID to result.objectId,
                            RESULT_SPACE_ID to result.spaceId,
                            RESULT_TYPE_NAME to result.typeName,
                            RESULT_SPACE_NAME to result.spaceName
                        )
                    )
                }
                hideKeyboardNow()
                dismiss()
            }
        }
    }

    /** Called by the embedded editor when the user follows a link/mention out of the draft. */
    fun onOpenObjectRequested(target: Id, space: Id, isChat: Boolean) {
        hideKeyboardNow()
        dismiss()
        mainVm.onOpenSharedObject(
            objectId = target,
            spaceId = space,
            isChat = isChat
        )
    }

    private fun editor(): EditorQuickCaptureFragment? =
        childFragmentManager.findFragmentByTag(TAG_EDITOR) as? EditorQuickCaptureFragment

    // No onDismiss override on purpose: DialogFragment also calls onDismiss on plain view
    // destruction (config changes), which must NOT tear down the global space state while
    // the retained VM stays Ready. Cleanup lives in QuickCaptureViewModel.onCleared().

    override fun injectDependencies() {
        componentManager().quickCaptureComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().quickCaptureComponent.release()
    }

    override fun inflateBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuickCaptureBinding = FragmentQuickCaptureBinding.inflate(
        inflater, container, false
    )

    companion object {
        /**
         * Material's bottom-sheet default (~0.32) leaves the vault distractingly readable
         * behind the capture sheet, and the transparent strip above the sheet shows more of
         * it than a normal sheet would. 0.9 keeps the vault as context, not as content.
         */
        private const val SCRIM_DIM_AMOUNT = 0.9f

        /** Smallest dim change worth a window relayout during a drag. */
        private const val DIM_UPDATE_THRESHOLD = 0.02f

        /**
         * Sheet travel at which the keyboard is retired. Past roughly half, the drag is
         * committed to a dismissal (this is also where BottomSheetBehavior's own hide
         * threshold sits), so the IME can start leaving with the sheet instead of after it.
         */
        private const val KEYBOARD_RETIRE_SLIDE_OFFSET = 0.5f
        private const val TAG_EDITOR = "quick-capture-editor"
        const val RESULT_KEY = "quick_capture.result"
        const val RESULT_OBJECT_ID = "quick_capture.result.object_id"
        const val RESULT_SPACE_ID = "quick_capture.result.space_id"
        const val RESULT_TYPE_NAME = "quick_capture.result.type_name"
        const val RESULT_SPACE_NAME = "quick_capture.result.space_name"
    }
}
