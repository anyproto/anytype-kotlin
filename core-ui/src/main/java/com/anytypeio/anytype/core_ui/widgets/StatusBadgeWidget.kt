package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncError
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncUpdate
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible


class StatusBadgeWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    fun bind(status: SpaceSyncAndP2PStatusState?) {
        when (status) {
            is SpaceSyncAndP2PStatusState.Error -> {
                visible()
                setImageResource(R.drawable.ic_sync_error_10)
            }
            SpaceSyncAndP2PStatusState.Init -> {
                gone()
            }
            is SpaceSyncAndP2PStatusState.Success -> {
                when (val spaceSyncUpdate = status.spaceSyncUpdate) {
                    is SpaceSyncUpdate.Update -> {
                        val error = spaceSyncUpdate.error
                        if (error != SpaceSyncError.NULL) {
                            visible()
                            setImageResource(R.drawable.ic_sync_error_10)
                        } else {
                            return when (spaceSyncUpdate.status) {
                                SpaceSyncStatus.SYNCED -> {
                                    visible()
                                    setImageResource(R.drawable.ic_synced_10)
                                }

                                SpaceSyncStatus.SYNCING -> {
                                    visible()
                                    setImageResource(R.drawable.ic_syncing)
                                }

                                SpaceSyncStatus.ERROR -> {
                                    visible()
                                    setImageResource(R.drawable.ic_sync_error_10)
                                }

                                SpaceSyncStatus.OFFLINE -> {
                                    visible()
                                    setImageResource(R.drawable.ic_sync_grey_10)
                                }
                            }
                        }
                    }

                    SpaceSyncUpdate.Initial -> {
                        gone()
                    }
                }
            }

            null -> {
                gone()
            }
        }
    }
}