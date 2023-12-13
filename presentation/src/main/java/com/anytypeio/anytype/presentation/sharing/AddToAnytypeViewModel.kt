package com.anytypeio.anytype.presentation.sharing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.core_utils.ext.msg
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.objects.CreateBookmarkObject
import com.anytypeio.anytype.domain.objects.CreatePrefilledNote
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.spaces.SelectSpaceViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class AddToAnytypeViewModel(
    private val createBookmarkObject: CreateBookmarkObject,
    private val createPrefilledNote: CreatePrefilledNote,
    private val spaceManager: SpaceManager,
    private val container: StorelessSubscriptionContainer
) : BaseViewModel() {

    private val spaces: Flow<List<ObjectWrapper.Basic>> = container.subscribe(
        StoreSearchParams(
            subscription = SelectSpaceViewModel.SELECT_SPACE_SUBSCRIPTION,
            keys = listOf(
                Relations.ID,
                Relations.TARGET_SPACE_ID,
                Relations.NAME,
                Relations.ICON_IMAGE,
                Relations.ICON_EMOJI,
                Relations.ICON_OPTION,
                Relations.SPACE_ACCOUNT_STATUS
            ),
            filters = listOf(
                DVFilter(
                    relation = Relations.LAYOUT,
                    value = ObjectType.Layout.SPACE_VIEW.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                ),
                DVFilter(
                    relation = Relations.SPACE_ACCOUNT_STATUS,
                    value = SpaceStatus.SPACE_DELETED.code.toDouble(),
                    condition = DVFilterCondition.NOT_EQUAL
                ),
                DVFilter(
                    relation = Relations.SPACE_LOCAL_STATUS,
                    value = SpaceStatus.OK.code.toDouble(),
                    condition = DVFilterCondition.EQUAL
                )
            ),
            sorts = listOf(
                DVSort(
                    relationKey = Relations.LAST_OPENED_DATE,
                    type = DVSortType.DESC,
                    includeTime = true
                )
            )
        )
    ).catch {
        emit(emptyList())
    }.map { spaces ->
        spaces.distinctBy { it.id }
    }

    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    fun onCreateBookmark(url: String) {
        viewModelScope.launch {
            createBookmarkObject(
                CreateBookmarkObject.Params(
                    space = spaceManager.get(),
                    url = url
                )
            ).process(
                success = { obj ->
                    navigation.emit(OpenObjectNavigation.OpenEditor(obj))
                },
                failure = {
                    Timber.d(it, "Error while creating bookmark")
                    sendToast("Error while creating bookmark: ${it.msg()}")
                }
            )
        }
    }

    fun onCreateNote(text: String) {
        viewModelScope.launch {
            createPrefilledNote.async(
                CreatePrefilledNote.Params(text)
            ).fold(
                onSuccess = { result ->
                    navigation.emit(OpenObjectNavigation.OpenEditor(result))
                },
                onFailure = {
                    Timber.d(it, "Error while creating note")
                    sendToast("Error while creating note: ${it.msg()}")
                }
            )
        }
    }

    class Factory @Inject constructor(
        private val createBookmarkObject: CreateBookmarkObject,
        private val createPrefilledNote: CreatePrefilledNote,
        private val spaceManager: SpaceManager,
        private val container: StorelessSubscriptionContainer
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AddToAnytypeViewModel(
                createBookmarkObject = createBookmarkObject,
                spaceManager = spaceManager,
                createPrefilledNote = createPrefilledNote,
                container = container
            ) as T
        }
    }
}