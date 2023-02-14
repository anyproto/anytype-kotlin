package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import javax.inject.Inject
import javax.inject.Qualifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class TypeEditViewModel(
    private val urlBuilder: UrlBuilder,
    private val id: Id,
    private val name: String,
    private val icon: String
) : NavigationViewModel<TypeEditViewModel.Navigation>() {

    private val unicodeIconFlow = MutableStateFlow(icon)
    private val originalNameFlow = MutableStateFlow(name)

    val uiState: StateFlow<TypeEditState> = combine(
        unicodeIconFlow,
        originalNameFlow
    ) { icon, name ->
        val objectIcon = icon.orNull()?.let {
            ObjectIcon.from(
                obj = ObjectWrapper.Basic(mapOf(Relations.ICON_EMOJI to icon)),
                builder = urlBuilder,
                layout = ObjectType.Layout.OBJECT_TYPE
            )
        }
        TypeEditState.Data(
            typeName = name,
            emojiUnicode = icon,
            objectIcon = objectIcon ?: ObjectIcon.None
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
        TypeEditState.Idle
    )

    fun openEmojiPicker() {
        navigate(Navigation.SelectEmoji)
    }

    fun setEmoji(emojiUnicode: String) {
        unicodeIconFlow.value = emojiUnicode
    }

    fun removeEmoji() {
        unicodeIconFlow.value = ""
    }

    fun uninstallType() {
        navigate(Navigation.BackWithUninstall(id))
    }

    fun updateObjectDetails(name: String) {
        navigate(Navigation.BackWithModify(id, name, unicodeIconFlow.value))
    }

    sealed class Navigation {
        object Back : Navigation()
        object SelectEmoji : Navigation()
        class BackWithUninstall(val typeId: String): Navigation()
        class BackWithModify(
            val typeId: String,
            val typeName: String,
            val typeIcon: String
        ): Navigation()
    }

    class Factory @Inject constructor(
        private val urlBuilder: UrlBuilder,
        @TypeId private val id: Id,
        @TypeName private val name: String,
        @TypeIcon private val icon: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TypeEditViewModel(
                urlBuilder = urlBuilder,
                id = id,
                name = name,
                icon = icon
            ) as T
        }
    }

}

sealed class TypeEditState {
    data class Data(
        val typeName: String,
        val emojiUnicode: String? = null,
        val objectIcon: ObjectIcon = ObjectIcon.None
    ): TypeEditState()
    object Idle: TypeEditState()
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeId

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeName

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class TypeIcon



private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L