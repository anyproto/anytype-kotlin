package com.anytypeio.anytype.ui.page.modals.actions

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.View.OVER_SCROLL_NEVER
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_utils.ext.arg
import com.anytypeio.anytype.core_utils.ext.parsePath
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_EMOJI
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_RANDOM_EMOJI
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_CHOOSE_UPLOAD_PHOTO
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuAdapter.Companion.OPTION_REMOVE
import com.anytypeio.anytype.library_page_icon_picker_widget.ui.ActionMenuDivider
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModel
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModel.Contract
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModel.ViewState
import com.anytypeio.anytype.presentation.page.picker.DocumentIconActionMenuViewModelFactory
import com.anytypeio.anytype.ui.page.modals.ObjectIconPickerFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.action_toolbar_page_icon.*
import timber.log.Timber
import javax.inject.Inject

@Deprecated("To be deleted")
class DocumentIconActionMenuFragment : BaseFragment(R.layout.action_toolbar_page_icon),
    Observer<ViewState> {

    private val ctx get() = arg<Id>(ARG_CONTEXT_ID_KEY)

    private val target: String
        get() = requireArguments()
            .getString(ARG_TARGET_ID_KEY)
            ?: throw IllegalStateException(MISSING_TARGET_ERROR)

    @Inject
    lateinit var factory: DocumentIconActionMenuViewModelFactory

    @Inject
    lateinit var analytics: Analytics

    private val vm by viewModels<DocumentIconActionMenuViewModel> { factory }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        vm.state.observe(viewLifecycleOwner, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container.setOnClickListener { exit() }
        setIcon()
        setupLogoTranslation()
        setupAdapter()
        showMenuWithAnimation()
    }

    private fun setIcon() {
        arguments?.getString(EMOJI_KEY)?.let { unicode ->
            try {
                Glide
                    .with(emojiIconImage)
                    .load(Emojifier.uri(unicode))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(emojiIconImage)
            } catch (e: Throwable) {
                Timber.e(e, "Error while setting emoji icon for: $unicode")
            }
        }
        arguments?.getString(IMAGE_KEY)?.let { url ->
            Glide
                .with(icon)
                .load(url)
                .centerInside()
                .circleCrop()
                .into(imageIcon)
        }
    }

    private fun setupLogoTranslation() {
        val y = arguments?.getFloat(Y_KEY)
        if (y != null && y != 0.0f) {
            val delta = y - icon.y
            icon.y = y
            menu.y = menu.y + delta
        }
    }

    private fun showMenuWithAnimation() {
        val xAnim = menu.animate().scaleX(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }
        val yAnim = menu.animate().scaleY(1f).apply {
            duration = ANIM_DURATION
            startDelay = ANIM_START_DELAY
            interpolator = OvershootInterpolator()
        }

        xAnim.start()
        yAnim.start()
    }

    private fun setupAdapter() {

        val drawable = requireContext().getDrawable(R.drawable.action_menu_divider)

        checkNotNull(drawable)

        val divider = ActionMenuDivider(drawable)

        recycler.apply {
            addItemDecoration(divider)
            overScrollMode = OVER_SCROLL_NEVER
            adapter = ActionMenuAdapter(
                options = intArrayOf(
                    OPTION_CHOOSE_EMOJI,
                    OPTION_CHOOSE_RANDOM_EMOJI,
                    OPTION_CHOOSE_UPLOAD_PHOTO,
                    OPTION_REMOVE
                )
            ) { option ->
                when (option) {
                    OPTION_CHOOSE_EMOJI -> {
                        parentFragment?.childFragmentManager?.let { manager ->
                            manager.popBackStack()
                            ObjectIconPickerFragment.new(
                                context = target,
                                target = target
                            ).show(manager, null)
                        }
                        lifecycleScope.sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.POPUP_CHOOSE_EMOJI
                        )
                    }
                    OPTION_REMOVE -> {
                        vm.onEvent(
                            Contract.Event.OnRemoveEmojiSelected(
                                context = target,
                                target = target
                            )
                        )
                        lifecycleScope.sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.BTN_REMOVE_EMOJI
                        )
                    }
                    OPTION_CHOOSE_RANDOM_EMOJI -> {
                        vm.onEvent(
                            Contract.Event.OnSetRandomEmojiClicked(
                                context = target,
                                target = target
                            )
                        )
                        lifecycleScope.sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.BTN_RANDOM_EMOJI
                        )
                    }
                    OPTION_CHOOSE_UPLOAD_PHOTO -> {
                        proceedWithImagePick()
                        lifecycleScope.sendEvent(
                            analytics = analytics,
                            eventName = EventsDictionary.BTN_UPLOAD_PHOTO
                        )
                    }
                    else -> toast("Not implemented")
                }
            }
        }
    }

    override fun onChanged(state: ViewState) {
        when (state) {
            is ViewState.Exit -> exit()
            is ViewState.Error -> toast(state.message)
            is ViewState.Loading -> toast(getString(R.string.loading))
            is ViewState.Uploading -> progress.visible()
        }
    }

    private fun proceedWithImagePick() {
        if (!hasExternalStoragePermission())
            requestExternalStoragePermission()
        else
            openGallery()
    }

    private fun requestExternalStoragePermission() {
        requestPermissions(
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun hasExternalStoragePermission() = ContextCompat.checkSelfPermission(
        requireActivity(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ).let { result -> result == PackageManager.PERMISSION_GRANTED }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == SELECT_IMAGE_CODE) {
            data?.data?.let { uri ->
                try {
                    val path = uri.parsePath(requireContext())
                    vm.onEvent(
                        Contract.Event.OnImagePickedFromGallery(
                            context = target,
                            path = path
                        )
                    )
                } catch (e: Exception) {
                    Timber.e(COULD_NOT_PARSE_PATH_ERROR)
                    toast(COULD_NOT_PARSE_PATH_ERROR)
                }
            }
        }
    }

    private fun exit() {
        parentFragment?.childFragmentManager?.popBackStack()
    }

    private fun openGallery() {
        try {
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI
            ).let { intent ->
                startActivityForResult(intent, SELECT_IMAGE_CODE)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to open gallery")
            toast("Failed to open gallery. Please, try again later.")
        }
    }

    override fun injectDependencies() {
        componentManager().documentIconActionMenuComponent.get(ctx).inject(this)
    }

    override fun releaseDependencies() {
        componentManager().documentIconActionMenuComponent.release(ctx)
    }

    companion object {
        fun new(
            y: Float?,
            emoji: String?,
            image: String?,
            target: String,
            ctx: Id
        ): DocumentIconActionMenuFragment = DocumentIconActionMenuFragment().apply {
            arguments = bundleOf(
                Y_KEY to y,
                EMOJI_KEY to emoji,
                IMAGE_KEY to image,
                ARG_TARGET_ID_KEY to target,
                ARG_CONTEXT_ID_KEY to ctx
            )
        }

        private const val SELECT_IMAGE_CODE = 1
        private const val REQUEST_PERMISSION_CODE = 2
        private const val Y_KEY = "y"
        private const val EMOJI_KEY = "emoji"
        private const val IMAGE_KEY = "image_key"
        private const val ANIM_START_DELAY = 200L
        private const val ANIM_DURATION = 200L
        private const val ARG_TARGET_ID_KEY = "arg.picker.target.id"
        private const val ARG_CONTEXT_ID_KEY = "arg.picker.target.id"
        private const val MISSING_TARGET_ERROR = "Missing target id"
        private const val COULD_NOT_PARSE_PATH_ERROR = "Could not parse path to your image"
    }
}