package com.anytypeio.anytype.ui.settings

import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_utils.ext.toast
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.BaseFragment
import com.anytypeio.anytype.di.common.componentManager
import com.anytypeio.anytype.domain.config.GetDebugSettings
import com.anytypeio.anytype.domain.config.UseCustomContextMenu
import com.anytypeio.anytype.domain.dataview.interactor.DebugLocalStore
import com.anytypeio.anytype.domain.dataview.interactor.DebugSync
import kotlinx.android.synthetic.main.fragment_debug_settings.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class DebugSettingsFragment : BaseFragment(R.layout.fragment_debug_settings) {

    @Inject
    lateinit var useCustomContextMenu: UseCustomContextMenu

    @Inject
    lateinit var getDebugSettings: GetDebugSettings

    @Inject
    lateinit var debugSync: DebugSync

    @Inject
    lateinit var debugLocalStore: DebugLocalStore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            getDebugSettings(Unit).proceed(
                failure = {},
                success = {}
            )
        }

        btnSync.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                debugSync.invoke(Unit).proceed(
                    failure = {},
                    success = { status -> saveToFile(status) }
                )
            }
        }

        btnLocalStore.setOnClickListener {
            val directory = File(requireContext().getExternalFilesDir(null), "debugLocalStore")
            directory.mkdir()
            viewLifecycleOwner.lifecycleScope.launch {
                debugLocalStore.invoke(DebugLocalStore.Params(directory.path)).proceed(
                    failure = {},
                    success = { path -> showStatus("Exported local store is saved to $path") }
                )
            }
        }

        tvSync.setOnClickListener {
            val cm = activity?.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            cm.text = tvSync.text
            requireContext().toast("Sync status is copied to the clipboard")
        }
    }

    private fun showStatus(msg: String) {
        scrollContainer.visible()
        tvSync.text = msg
    }

    private fun saveToFile(status: String) {
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd-MM-yyyy-HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        val fileName = "DebugSync$formattedDate"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val resolver = requireContext().contentResolver
                val cv = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
                }
                val imageUri = resolver.insert(MediaStore.Files.getContentUri("external"), cv)
                resolver.openOutputStream(imageUri!!).use { out ->
                    out?.write(status.toByteArray())
                }
                Timber.d("Save debug sync to : Documents/$fileName.txt")
                showStatus("DebugSync is saved to Documents/$fileName.txt")
            } catch (e: Exception) {
                Timber.d(e, "Can't create file")
                showStatus("DebugSync error ${e.localizedMessage}")
            }
        } else {
            try {
                val fileDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                        .toString()
                val file = File(fileDir, "$fileName.txt")
                file.bufferedWriter().use { out ->
                    out.write(status)
                }
                Timber.d("Save debug sync to : Documents/$fileName.txt")
                showStatus("DebugSync is saved to Documents/$fileName.txt")
            } catch (e: Exception) {
                Timber.d(e, "Can't create file")
                showStatus("DebugSync error ${e.localizedMessage}")
            }
        }
    }

    override fun injectDependencies() {
        componentManager().debugSettingsComponent.get().inject(this)
    }

    override fun releaseDependencies() {
        componentManager().debugSettingsComponent.release()
    }
}