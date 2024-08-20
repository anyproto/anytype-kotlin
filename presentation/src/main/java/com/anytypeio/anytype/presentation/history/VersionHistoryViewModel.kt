package com.anytypeio.anytype.presentation.history

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.history.SetVersion
import com.anytypeio.anytype.domain.history.ShowVersion
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.editor.Editor.Mode
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.INITIAL_INDENT
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenVersionPreview
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShowVersionHistoryScreen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsVersionHistoryRestore
import com.anytypeio.anytype.presentation.history.VersionHistoryGroup.GroupTitle
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.getRelationFormat
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class VersionHistoryViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val getVersions: GetVersions,
    private val objectSearch: SearchObjects,
    private val dateProvider: DateProvider,
    private val localeProvider: LocaleProvider,
    private val urlBuilder: UrlBuilder,
    private val showVersion: ShowVersion,
    private val setVersion: SetVersion,
    private val renderer: DefaultBlockViewRenderer
) : ViewModel(), BlockViewRenderer by renderer {

    private val _viewState = MutableStateFlow<VersionHistoryState>(VersionHistoryState.Loading)
    val viewState = _viewState.asStateFlow()
    private val _previewViewState =
        MutableStateFlow<VersionHistoryPreviewScreen>(VersionHistoryPreviewScreen.Hidden)
    val previewViewState = _previewViewState
    val navigation = MutableSharedFlow<Command>(0)

    private val _members = MutableStateFlow<List<ObjectWrapper.Basic>>(emptyList())

    //Paging
    var canPaginate by mutableStateOf(false)
    var listState by mutableStateOf(ListState.IDLE)
    var latestVisibleVersionId by mutableStateOf("")
    private val _list = MutableStateFlow<List<Version>>(emptyList())

    init {
        Timber.d("VersionHistoryViewModel created")
        getSpaceMembers()
        getHistoryVersions(objectId = vmParams.objectId, latestVersionId = "")
        viewModelScope.launch {
            sendAnalyticsShowVersionHistoryScreen(analytics)
        }
        viewModelScope.launch {
            combine(
                _members,
                _list
            ) { members, versions ->
                members to versions
            }.collectLatest { (members, versions) ->
                if (members.isNotEmpty() && versions.isNotEmpty()) {
                    handleVersionsSuccess(versions, members)
                }
            }
        }
    }

    fun onStart() {
        Timber.d("VersionHistoryViewModel started")
    }

    fun startPaging(latestVersionId: String) {
        Timber.d("Start paging, latestVersionId: $latestVersionId")
        getHistoryVersions(
            objectId = vmParams.objectId,
            latestVersionId = latestVersionId
        )
    }

    fun onGroupItemClicked(item: VersionHistoryGroup.Item) {
        viewModelScope.launch {
            _previewViewState.value = VersionHistoryPreviewScreen.Loading
            navigation.emit(Command.VersionPreview)
            proceedShowVersion(item = item)
        }
        viewModelScope.launch {
            sendAnalyticsScreenVersionPreview(analytics)
        }
    }

    fun proceedWithClick(click: ListenerType) {
        Timber.d("Click: $click")
        viewModelScope.launch {
            when (click) {
                is ListenerType.Relation.Featured -> {
                    proceedWithRelationValueNavigation(
                        relation = RelationKey(click.relation.key),
                        relationFormat = click.relation.getRelationFormat()
                    )
                }

                is ListenerType.Relation.Related -> {
                    if (click.value is BlockView.Relation.Related) {
                        proceedWithRelationValueNavigation(
                            relation = RelationKey(click.value.view.key),
                            relationFormat = click.value.view.getRelationFormat()
                        )
                    }
                }

                else -> {}
            }
        }
    }

    private suspend fun proceedWithRelationValueNavigation(
        relation: RelationKey,
        relationFormat: Relation.Format
    ) {
        val currentState =
            (_previewViewState.value as? VersionHistoryPreviewScreen.Success) ?: return
        val isSet = currentState.isSet
        when (relationFormat) {
            RelationFormat.SHORT_TEXT,
            RelationFormat.LONG_TEXT,
            RelationFormat.URL,
            RelationFormat.PHONE,
            RelationFormat.NUMBER,
            RelationFormat.EMAIL -> navigation.emit(Command.RelationText(relation, isSet))

            RelationFormat.DATE -> navigation.emit(Command.RelationDate(relation, isSet))
            Relation.Format.TAG,
            Relation.Format.STATUS -> navigation.emit(Command.RelationMultiSelect(relation, isSet))

            Relation.Format.OBJECT,
            Relation.Format.FILE -> navigation.emit(Command.RelationObject(relation, isSet))

            else -> {
                Timber.d("No interaction allowed with this relation with format:$relationFormat")
            }
        }
    }

    fun proceedWithHidePreview() {
        _previewViewState.value = VersionHistoryPreviewScreen.Hidden
        viewModelScope.launch {
            navigation.emit(Command.Main)
        }
    }

    fun proceedWithRestore() {
        val currentVersionId =
            (_previewViewState.value as? VersionHistoryPreviewScreen.Success)?.versionId ?: return
        viewModelScope.launch {
            val params = SetVersion.Params(
                objectId = vmParams.objectId,
                versionId = currentVersionId
            )
            setVersion.async(params).fold(
                onSuccess = {
                    Timber.d("Version restored")
                    _previewViewState.value = VersionHistoryPreviewScreen.Hidden
                    navigation.emit(Command.ExitToObject)
                    sendAnalyticsVersionHistoryRestore(analytics)
                },
                onFailure = {
                    Timber.e(it, "Error while restoring version")
                }
            )
        }
    }

    private fun getSpaceMembers() {
        viewModelScope.launch {
            val filters =
                ObjectSearchConstants.filterParticipants(spaces = listOf(vmParams.spaceId.id))
            objectSearch(
                SearchObjects.Params(
                    filters = filters,
                    keys = ObjectSearchConstants.spaceMemberKeys
                )
            ).process(
                failure = {
                    Timber.e(it, "Error while fetching new member")
                    _viewState.value = VersionHistoryState.Error.SpaceMembers
                },
                success = { members ->
                    if (members.isEmpty()) {
                        _viewState.value =
                            VersionHistoryState.Error.SpaceMembers
                    } else {
                        _members.value = members
                    }
                }
            )
        }
    }

    private fun getHistoryVersions(objectId: String, latestVersionId: String) {
        viewModelScope.launch {
            val params = GetVersions.Params(
                objectId = objectId,
                lastVersion = latestVersionId,
                limit = VERSIONS_MAX_LIMIT
            )
            getVersions.async(params).fold(
                onSuccess = { versions ->
                    canPaginate = versions.size == VERSIONS_MAX_LIMIT
                    if (latestVersionId.isEmpty()) {
                        if (versions.isEmpty()) {
                            _viewState.value = VersionHistoryState.Error.NoVersions
                        } else {
                            _list.value = versions
                        }
                    } else {
                        _list.value += versions
                    }
                    listState = ListState.IDLE
                    if (canPaginate) {
                        latestVisibleVersionId = versions.lastOrNull()?.id ?: ""
                    }
                },
                onFailure = {
                    _viewState.value = VersionHistoryState.Error.GetVersions
                    listState =
                        if (latestVersionId.isEmpty()) ListState.ERROR else ListState.PAGINATION_EXHAUST
                },
                onLoading = {}
            )
        }
    }

    override fun onCleared() {
        listState = ListState.IDLE
        canPaginate = false
        latestVisibleVersionId = ""
        super.onCleared()
    }

    private fun handleVersionsSuccess(versions: List<Version>, members: List<ObjectWrapper.Basic>) {
        val groupedItems = groupItems(versions, members)
        _viewState.value = VersionHistoryState.Success(groups = groupedItems)
    }

    private fun groupItems(
        versions: List<Version>,
        spaceMembers: List<ObjectWrapper.Basic>,
    ): List<VersionHistoryGroup> {

        val locale = localeProvider.locale()

        // Sort versions by timestamp (DESC) and group by day
        val versionsByDay = versions
            .sortedByDescending { it.timestamp.time }
            .groupBy { version ->
                val formattedDate = dateProvider.formatToDateString(
                    timestamp = (version.timestamp.inMillis),
                    pattern = GROUP_BY_DAY_FORMAT,
                    locale = locale
                )
                formattedDate
            }

        // Within each day, sort all versions by timestamp descending
        val sortedVersionsByDay = versionsByDay.keys.associateWith { day ->
            val versionByDay = versionsByDay[day] ?: return@associateWith emptyList()
            versionByDay.sortedByDescending { it.timestamp.time }
        }

        // Group by space member sequentially within each day
        val groupedBySpaceMember = sortedVersionsByDay.groupByMemberAndMinuteWithinADay()

        val groups = groupedBySpaceMember.mapNotNull { (_, spaceMemberVersions) ->
            if (spaceMemberVersions.isEmpty()) {
                return emptyList()
            }

            val spaceMemberLatestVersion =
                spaceMemberVersions.firstOrNull()?.firstOrNull() ?: return@mapNotNull null

            val spaceMemberOldestVersion =
                spaceMemberVersions.lastOrNull()?.lastOrNull() ?: return@mapNotNull null

            val groupItems = spaceMemberVersions.toGroupItems(
                spaceMembers = spaceMembers,
                locale = locale
            )

            VersionHistoryGroup(
                id = spaceMemberOldestVersion.id,
                title = getGroupTitle(spaceMemberLatestVersion.timestamp, locale),
                icons = groupItems.distinctBy { it.spaceMember }.mapNotNull { it.icon },
                items = groupItems
            )
        }.mapIndexed { index, versionHistoryGroup ->
            if (index == 0) versionHistoryGroup.copy(isExpanded = true) else versionHistoryGroup
        }
        return groups
    }

    private fun Map<String, List<Version>>.groupByMemberAndMinuteWithinADay(): Map<String, List<List<Version>>> {
        val groupedBySpaceMember = mapValues { (_, versions) ->
            val grouped = mutableListOf<MutableList<Version>>()
            var currentGroup = mutableListOf<Version>()

            for (version in versions) {
                if (currentGroup.isEmpty()
                    || (currentGroup.last().spaceMember == version.spaceMember
                            && dateProvider.isSameMinute(
                        currentGroup.first().timestamp.time, version.timestamp.time
                    ))
                ) {
                    currentGroup.add(version)
                } else {
                    grouped.add(currentGroup)
                    currentGroup = mutableListOf(version)
                }
            }

            if (currentGroup.isNotEmpty()) {
                grouped.add(currentGroup)
            }

            grouped
        }
        return groupedBySpaceMember
    }

    private fun Map<String, List<Version>>.groupByMemberWithinADay(): Map<String, List<List<Version>>> {
        val groupedBySpaceMember = mapValues { (_, versions) ->
            val grouped = mutableListOf<MutableList<Version>>()
            var currentGroup = mutableListOf<Version>()

            for (version in versions) {
                if (currentGroup.isEmpty() || currentGroup.last().spaceMember == version.spaceMember) {
                    currentGroup.add(version)
                } else {
                    grouped.add(currentGroup)
                    currentGroup = mutableListOf(version)
                }
            }

            if (currentGroup.isNotEmpty()) {
                grouped.add(currentGroup)
            }

            grouped
        }
        return groupedBySpaceMember
    }

    private fun getGroupTitle(timestamp: TimeInSeconds, locale: Locale): GroupTitle {
        val dateInstant = Instant.ofEpochSecond(timestamp.time)
        val givenDate = dateInstant.atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        val givenYear = givenDate.year
        val currentYear = currentDate.year
        val givenDateWithZeroTime = givenDate.atStartOfDay().toLocalDate()
        return when (givenDateWithZeroTime) {
            currentDate -> {
                GroupTitle.Today
            }

            currentDate.minusDays(1) -> {
                GroupTitle.Yesterday
            }

            else -> {
                val pattern = if (givenYear == currentYear) {
                    GROUP_DATE_FORMAT_CURRENT_YEAR
                } else {
                    GROUP_DATE_FORMAT_OTHER_YEAR
                }
                GroupTitle.Date(
                    dateProvider.formatToDateString(
                        timestamp = timestamp.inMillis,
                        pattern = pattern,
                        locale = locale
                    )
                )
            }
        }
    }

    private fun List<List<Version>>.toGroupItems(
        spaceMembers: List<ObjectWrapper.Basic>,
        locale: Locale
    ): List<VersionHistoryGroup.Item> {
        return mapNotNull { versions ->
            val latestVersion = versions.firstOrNull() ?: return@mapNotNull null
            val spaceMemberId = latestVersion.spaceMember
            val spaceMember = spaceMembers.find { it.id == spaceMemberId }
                ?: return@mapNotNull null

            val icon = ObjectIcon.from(
                obj = spaceMember,
                layout = spaceMember.layout,
                builder = urlBuilder
            )

            val (latestVersionDate, latestVersionTime) = dateProvider.formatTimestampToDateAndTime(
                timestamp = latestVersion.timestamp.inMillis,
                locale = locale
            )

            VersionHistoryGroup.Item(
                id = latestVersion.id,
                spaceMember = spaceMemberId,
                spaceMemberName = spaceMember.name.orEmpty(),
                timeStamp = latestVersion.timestamp,
                icon = icon,
                timeFormatted = latestVersionTime,
                versions = versions,
                dateFormatted = latestVersionDate
            )
        }
    }

    private suspend fun proceedShowVersion(
        item: VersionHistoryGroup.Item
    ) {
        val params = ShowVersion.Params(
            objectId = vmParams.objectId,
            versionId = item.id
        )
        showVersion.async(params).fold(
            onFailure = {
                Timber.e(it, "Error while fetching version")
                val currentState = _previewViewState.value
                if (currentState !is VersionHistoryPreviewScreen.Hidden) {
                    _previewViewState.value =
                        VersionHistoryPreviewScreen.Error(it.message.orEmpty())
                }
            },
            onSuccess = { response ->
                Timber.d("Version fetched: $response")
                val payload = response.payload
                if (payload != null) {
                    val event = payload.events
                        .filterIsInstance<Event.Command.ShowObject>()
                        .first()
                    val obj =
                        ObjectWrapper.Basic(event.details.details[vmParams.objectId]?.map.orEmpty())
                    val root = event.blocks.first { it.id == vmParams.objectId }
                    val blocks = event.blocks.asMap().render(
                        mode = Mode.Read,
                        root = root,
                        focus = Editor.Focus.empty(),
                        anchor = vmParams.objectId,
                        indent = INITIAL_INDENT,
                        details = event.details,
                        relationLinks = event.relationLinks,
                        restrictions = event.objectRestrictions,
                        selection = emptySet()
                    )
                    val currentState = _previewViewState.value
                    if (currentState !is VersionHistoryPreviewScreen.Hidden) {
                        _previewViewState.value = VersionHistoryPreviewScreen.Success(
                            versionId = item.id,
                            blocks = blocks,
                            dateFormatted = item.dateFormatted,
                            timeFormatted = item.timeFormatted,
                            icon = item.icon,
                            isSet = obj.layout.isDataView()
                        )
                    }
                }
            }
        )
    }

    data class VmParams(
        val objectId: Id,
        val spaceId: SpaceId
    )

    sealed class Command(val route: String) {
        data object Main : Command("main")
        data object VersionPreview : Command("version preview")
        data object ExitToObject : Command("")
        data class RelationMultiSelect(val relationKey: RelationKey, val isSet: Boolean) :
            Command("relation_select")

        data class RelationObject(val relationKey: RelationKey, val isSet: Boolean) :
            Command("relation_object")

        data class RelationDate(val relationKey: RelationKey, val isSet: Boolean) :
            Command("relation_date")

        data class RelationText(val relationKey: RelationKey, val isSet: Boolean) :
            Command("relation_text")
    }

    companion object {
        const val GROUP_BY_DAY_FORMAT = "d MM yyyy"
        const val GROUP_DATE_FORMAT_CURRENT_YEAR = "MMMM d"
        const val GROUP_DATE_FORMAT_OTHER_YEAR = "MMMM d, yyyy"

        const val VERSIONS_MAX_LIMIT = 10
    }
}

sealed class VersionHistoryState {
    data object Loading : VersionHistoryState()
    data class Success(
        val groups: List<VersionHistoryGroup>
    ) : VersionHistoryState()

    sealed class Error : VersionHistoryState() {
        data object SpaceMembers : Error()
        data object GetVersions : Error()
        data object NoVersions : Error()
    }
}

sealed class VersionHistoryPreviewScreen {
    data object Hidden : VersionHistoryPreviewScreen()
    data object Loading : VersionHistoryPreviewScreen()
    data class Success(
        val versionId: Id,
        val blocks: List<BlockView>,
        val dateFormatted: String,
        val timeFormatted: String,
        val icon: ObjectIcon?,
        val isSet: Boolean
    ) :
        VersionHistoryPreviewScreen()

    data class Error(val message: String) : VersionHistoryPreviewScreen()
}

data class VersionHistoryGroup(
    val id: String,
    val title: GroupTitle,
    val icons: List<ObjectIcon>,
    val items: List<Item>,
    val isExpanded: Boolean = false
) {
    data class Item(
        val id: Id,
        val spaceMember: Id,
        val spaceMemberName: String,
        val dateFormatted: String,
        val timeFormatted: String,
        val timeStamp: TimeInSeconds,
        val icon: ObjectIcon?,
        val versions: List<Version>
    )

    sealed class GroupTitle {
        data object Today : GroupTitle()
        data object Yesterday : GroupTitle()
        data class Date(val date: String) : GroupTitle()
    }
}

enum class ListState {
    IDLE,
    LOADING,
    PAGINATING,
    ERROR,
    PAGINATION_EXHAUST,
}