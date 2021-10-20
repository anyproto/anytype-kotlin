package com.anytypeio.anytype.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.subscribe
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseBottomSheetFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.presentation.settings.UserSettingsViewModel
import kotlinx.android.synthetic.main.fragment_user_settings.*
import javax.inject.Inject

class UserSettingsFragment : BaseBottomSheetFragment() {

    @Inject
    lateinit var factory: UserSettingsViewModel.Factory

    private val vm by viewModels<UserSettingsViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_user_settings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ivIconNote.setEmoji(getString(R.string.name_type_note_icon))
        ivIconPage.setEmoji(getString(R.string.name_type_page_icon))
        noteListener.setOnClickListener { vm.onNoteClicked() }
        pageListener.setOnClickListener { vm.onPageClicked() }
    }

    override fun onStart() {
        super.onStart()
        with(lifecycleScope) {
            jobs += subscribe(vm.commands) { observe(it) }
        }
    }

    private fun observe(command: UserSettingsViewModel.Command) {
        when (command) {
            UserSettingsViewModel.Command.Exit -> dismiss()
            UserSettingsViewModel.Command.NoteSelected -> setNoteSelected()
            UserSettingsViewModel.Command.PageSelected -> setPageSelected()
        }
    }

    private fun setNoteSelected() {
        ivCheckedNote.visible()
        ivCheckedPage.invisible()
    }

    private fun setPageSelected() {
        ivCheckedNote.invisible()
        ivCheckedPage.visible()
    }

    override fun injectDependencies() {
        componentManager().userSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().userSettingsComponent.release()
    }
}