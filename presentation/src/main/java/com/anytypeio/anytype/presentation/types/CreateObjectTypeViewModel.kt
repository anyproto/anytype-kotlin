package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.libraryCreateType
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.types.CreateObjectType
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateObjectTypeViewModel(
    private val createObjectType: CreateObjectType,
    private val urlBuilder: UrlBuilder,
    private val emojiProvider: EmojiProvider,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
) : NavigationViewModel<CreateObjectTypeViewModel.Navigation>(),
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    private val unicodeIconFlow = MutableStateFlow("")

    val uiState: StateFlow<TypeCreationIconState> = unicodeIconFlow.map { icon ->
        val objectIcon = icon.orNull()?.let {
            ObjectIcon.from(
                obj = ObjectWrapper.Basic(mapOf(Relations.ICON_EMOJI to icon)),
                builder = urlBuilder,
                layout = ObjectType.Layout.OBJECT_TYPE
            )
        }
        TypeCreationIconState(
            emojiUnicode = icon,
            objectIcon = objectIcon ?: ObjectIcon.None
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        TypeCreationIconState()
    )

    fun createType(name: String) {
        if (name.isEmpty()) {
            sendToast("Name should not be empty")
            return
        }
        viewModelScope.launch {
            createObjectType.execute(
                CreateObjectType.Params(
                    space = spaceManager.get(),
                    name = name,
                    emojiUnicode = uiState.value.emojiUnicode.orNull()
                )
            ).fold(
                onSuccess = {
                    val spaceParams = provideParams(spaceManager.get())
                    viewModelScope.sendEvent(
                        analytics = analytics,
                        eventName = libraryCreateType,
                        props = Props(
                            mapOf(
                                EventsPropertiesKey.permissions to spaceParams.permission,
                                EventsPropertiesKey.spaceType to spaceParams.spaceType
                            )
                        )
                    )
                    navigate(Navigation.BackWithCreatedType)
                },
                onFailure = {
                    Timber.e(it, "Error while creating type $name")
                    sendToast("Something went wrong. Please, try again later.")
                }
            )
        }
    }

    fun openEmojiPicker() {
        navigate(Navigation.SelectEmoji(unicodeIconFlow.value.isNotEmpty()))
    }

    fun setEmoji(emojiUnicode: String) {
        unicodeIconFlow.value = emojiUnicode
    }

    fun removeEmoji() {
        unicodeIconFlow.value = ""
    }

    fun onPreparedString(preparedName: Id) {
        if (preparedName.isNotEmpty()) {
            // todo: not random but random
            unicodeIconFlow.value = emojiProvider.emojis.random().random()
        }
    }

    sealed class Navigation {
        object BackWithCreatedType: Navigation()
        class SelectEmoji(val showRemove: Boolean) : Navigation()
    }

    class Factory @Inject constructor(
        private val createObjectType: CreateObjectType,
        private val urlBuilder: UrlBuilder,
        private val emojiProvider: EmojiProvider,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager,
        private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateObjectTypeViewModel(
                createObjectType = createObjectType,
                urlBuilder = urlBuilder,
                emojiProvider = emojiProvider,
                analytics = analytics,
                spaceManager = spaceManager,
                analyticSpaceHelperDelegate = analyticSpaceHelperDelegate
            ) as T
        }
    }

}

data class TypeCreationIconState(
    val emojiUnicode: String? = null,
    val objectIcon: ObjectIcon = ObjectIcon.None
)

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L