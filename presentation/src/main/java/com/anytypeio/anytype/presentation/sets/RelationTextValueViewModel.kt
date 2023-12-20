package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.core_utils.intents.SystemAction
import com.anytypeio.anytype.domain.`object`.ReloadObject
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectReload
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationUrlCopy
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationUrlEdit
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationUrlOpen
import com.anytypeio.anytype.presentation.number.NumberParser
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationTextValueViewModel(
    private val relations: ObjectRelationProvider,
    private val values: ObjectValueProvider,
    private val reloadObject: ReloadObject,
    private val analytics: Analytics,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : BaseViewModel() {

    val views = MutableStateFlow<List<RelationTextValueView>>(emptyList())
    val actions = MutableStateFlow<List<RelationValueAction>>(emptyList())
    val intents = MutableSharedFlow<SystemAction>(replay = 0)
    val title = MutableStateFlow("")
    val isDismissed = MutableStateFlow<Boolean>(false)

    private val jobs = mutableListOf<Job>()

    fun onDateStart(
        name: String,
        value: Long?,
    ) {
        Timber.d("onDateStart, name:[$name], value:[$value]")
        title.value = name
        views.value = listOf(
            RelationTextValueView.Number(
                value = NumberParser.parse(value)
            )
        )
    }

    fun onStart(
        ctx: Id,
        relationKey: Key,
        objectId: Id,
        isLocked: Boolean = false
    ) {
        Timber.d("onStart, ctx:[$ctx], relationKey:[$relationKey], object:[$objectId], isLocked:[$isLocked]")
        jobs += viewModelScope.launch {
            val pipeline = combine(
                relations.observe(relationKey),
                values.subscribe(ctx = ctx, target = objectId)
            ) { relation, values ->
                Timber.d("combine, relation:[$relation], values:[$values]")
                val obj = ObjectWrapper.Basic(values)
                val value = values[relationKey]?.toString()
                val isValueReadOnly = values[Relations.IS_READ_ONLY] as? Boolean ?: false
                val isValueEditable = !(isValueReadOnly || isLocked)
                title.value = relation.name.orEmpty()
                when (relation.format) {
                    RelationFormat.SHORT_TEXT -> {
                        views.value = listOf(
                            RelationTextValueView.TextShort(
                                value = value,
                                isEditable = isValueEditable
                            )
                        )
                    }
                    RelationFormat.LONG_TEXT -> {
                        views.value = listOf(
                            RelationTextValueView.Text(
                                value = value,
                                isEditable = isValueEditable
                            )
                        )
                    }
                    RelationFormat.NUMBER -> {
                        views.value = listOf(
                            RelationTextValueView.Number(
                                value = NumberParser.parse(value),
                                isEditable = isValueEditable
                            )
                        )
                    }
                    RelationFormat.URL -> {
                        views.value = listOf(
                            RelationTextValueView.Url(
                                value = value,
                                isEditable = isValueEditable
                            )
                        )
                        if (value != null) {
                            actions.value = buildList {
                                add(RelationValueAction.Url.Browse(value))
                                add(RelationValueAction.Url.Copy(value))
                                if (relation.key == Relations.SOURCE) {
                                    val type = obj.type.firstOrNull()
                                    if (type != null) {
                                        val wrapper = storeOfObjectTypes.get(type)
                                        if (wrapper?.uniqueKey == ObjectTypeUniqueKeys.BOOKMARK) {
                                            add(RelationValueAction.Url.Reload(value))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    RelationFormat.EMAIL -> {
                        views.value = listOf(
                            RelationTextValueView.Email(
                                value = value,
                                isEditable = isValueEditable
                            )
                        )
                        if (value != null) {
                            actions.value = listOf(
                                RelationValueAction.Email.Mail(value),
                                RelationValueAction.Email.Copy(value)
                            )
                        }
                    }
                    RelationFormat.PHONE -> {
                        views.value = listOf(
                            RelationTextValueView.Phone(
                                value = value,
                                isEditable = isValueEditable
                            )
                        )
                        if (value != null) {
                            actions.value = listOf(
                                RelationValueAction.Phone.Call(value),
                                RelationValueAction.Phone.Copy(value)
                            )
                        }
                    }
                    else -> throw  IllegalArgumentException("Wrong format:${relation.format}")
                }
            }.catch { Timber.e(it, "Error getting relation") }
            pipeline.collect()
        }
    }

    fun onStop() {
        jobs.cancel()
    }

    fun onAction(
        target: Id,
        action: RelationValueAction
    ) {
        Timber.d("onAction, target:[$target], action:[$action]")
        when (action) {
            is RelationValueAction.Email.Copy -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.CopyToClipboard(
                            plain = action.email,
                            label = SystemAction.LABEL_EMAIL
                        )
                    )
                    isDismissed.value = true
                }
            }
            is RelationValueAction.Email.Mail -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.MailTo(email = action.email)
                    )
                    isDismissed.value = true
                }
            }
            is RelationValueAction.Phone.Call -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.Dial(phone = action.phone)
                    )
                    isDismissed.value = true
                }
            }
            is RelationValueAction.Phone.Copy -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.CopyToClipboard(
                            plain = action.phone,
                            label = SystemAction.LABEL_PHONE
                        )
                    )
                    isDismissed.value = true
                }
            }
            is RelationValueAction.Url.Browse -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.OpenUrl(url = action.url)
                    )
                    isDismissed.value = true
                    sendAnalyticsRelationUrlOpen(analytics)
                }
            }
            is RelationValueAction.Url.Copy -> {
                viewModelScope.launch {
                    intents.emit(
                        SystemAction.CopyToClipboard(
                            plain = action.url,
                            label = SystemAction.LABEL_URL
                        )
                    )
                    isDismissed.value = true
                    sendAnalyticsRelationUrlCopy(analytics)
                }
            }
            is RelationValueAction.Url.Reload -> {
                proceedWithReloadingObject(
                    target = target,
                    url = action.url
                )
            }
        }
    }

    private fun proceedWithReloadingObject(target: Id, url: Url) {
        viewModelScope.launch {
            reloadObject(
                ReloadObject.Params.FromUrl(
                    ctx = target,
                    url = url
                )
            ).process(
                success = {
                    isDismissed.value = true
                    sendAnalyticsObjectReload(analytics)
                },
                failure = {
                    Timber.e(it, "Error while reloading bookmark.")
                    sendToast("Something went wrong. Please, try again later.")
                }
            )
        }
    }

    fun onUrlEditEvent(hasFocus: Boolean) {
        if (hasFocus) {
            viewModelScope.sendAnalyticsRelationUrlEdit(analytics)
        }
    }

    class Factory(
        private val relations: ObjectRelationProvider,
        private val values: ObjectValueProvider,
        private val reloadObject: ReloadObject,
        private val analytics: Analytics,
        private val storeOfObjectTypes: StoreOfObjectTypes
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationTextValueViewModel(
                relations = relations,
                values = values,
                reloadObject = reloadObject,
                analytics = analytics,
                storeOfObjectTypes = storeOfObjectTypes
            ) as T
        }
    }
}

sealed class RelationTextValueView {
    abstract val value: String?
    abstract val isEditable: Boolean

    data class Text(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class TextShort(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Phone(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Url(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Email(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()

    data class Number(
        override val value: String? = null,
        override val isEditable: Boolean = true
    ) : RelationTextValueView()
}

sealed interface RelationValueAction {
    sealed class Url : RelationValueAction {
        abstract val url: String

        data class Copy(override val url: String) : Url()
        data class Browse(override val url: String) : Url()
        data class Reload(override val url: String) : Url()
    }

    sealed class Email : RelationValueAction {
        abstract val email: String

        data class Copy(override val email: String) : Email()
        data class Mail(override val email: String) : Email()
    }

    sealed class Phone : RelationValueAction {
        abstract val phone: String

        data class Copy(override val phone: String) : Phone()
        data class Call(override val phone: String) : Phone()
    }
}
