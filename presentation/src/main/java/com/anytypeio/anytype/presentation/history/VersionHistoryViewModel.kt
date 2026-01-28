package com.anytypeio.anytype.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.editor.Editor
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.history.SetVersion
import com.anytypeio.anytype.domain.history.ShowVersion
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.editor.Editor.Mode
import com.anytypeio.anytype.presentation.editor.EditorViewModel.Companion.INITIAL_INDENT
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.BlockViewRenderer
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenVersionPreview
import com.anytypeio.anytype.presentation.extension.sendAnalyticsShowVersionHistoryScreen
import com.anytypeio.anytype.presentation.extension.sendAnalyticsVersionHistoryRestore
import com.anytypeio.anytype.presentation.history.VersionHistoryGroup.GroupTitle
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.mapper.toViewerColumns
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.relations.getRelationFormat
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.viewerByIdOrFirst
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import timber.log.Timber

class VersionHistoryViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val getVersions: GetVersions,
    private val objectSearch: SearchObjects,
    private val dateProvider: DateProvider,
    private val urlBuilder: UrlBuilder,
    private val showVersion: ShowVersion,
    private val setVersion: SetVersion,
    private val renderer: DefaultBlockViewRenderer,
    private val setStateReducer: ObjectStateReducer,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModel(), BlockViewRenderer by renderer {

    private val _viewState = MutableStateFlow<VersionHistoryState>(VersionHistoryState.Loading)
    val viewState = _viewState.asStateFlow()
    private val _previewViewState =
        MutableStateFlow<VersionHistoryPreviewScreen>(VersionHistoryPreviewScreen.Hidden)
    val previewViewState = _previewViewState
    val navigation = MutableSharedFlow<Command>(0)

    private val _members = MutableStateFlow<List<ObjectWrapper.Basic>>(emptyList())

    //Paging
    private val canPaginate = MutableStateFlow(false)
    val listState = MutableStateFlow(ListState.IDLE)
    val latestVisibleVersionId = MutableStateFlow("")
    private val _versions = MutableStateFlow<List<Version>>(emptyList())

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        setStateReducer.dispatch(payload.events)
    }

    init {
        Timber.d("VersionHistoryViewModel created")
        viewModelScope.launch { setStateReducer.run() }
        getSpaceMembers()
        getHistoryVersions(objectId = vmParams.objectId)
        viewModelScope.launch {
            sendAnalyticsShowVersionHistoryScreen(analytics)
        }
        viewModelScope.launch {
            combine(
                _members,
                _versions
            ) { members, versions ->
                members to versions
            }.collectLatest { (members, versions) ->
                if (members.isNotEmpty() && versions.isNotEmpty()) {
                    handleVersionsSuccess(versions, members)
                }
            }
        }
        viewModelScope.launch {
            setStateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged()
                .collectLatest { state ->
                    val viewer = mapToViewer(state)
                    when (val currentState = _previewViewState.value) {
                        VersionHistoryPreviewScreen.Loading -> {
                            _previewViewState.value = VersionHistoryPreviewScreen.Success.Set(
                                versionId = "",
                                blocks = emptyList(),
                                dateFormatted = "",
                                timeFormatted = "",
                                viewer = viewer,
                                icon = null
                            )
                        }
                        is VersionHistoryPreviewScreen.Success.Set -> {
                            _previewViewState.value = currentState.copy(
                                viewer = viewer
                            )
                        }
                        else -> {
                            Timber.d("Version preview state is not loading or success.set, skipping state update")
                        }
                    }
                }
        }
    }

    fun onStart() {
        Timber.d("VersionHistoryViewModel started")
    }

    fun startPaging(latestVersionId: String) {
        Timber.d("Start paging, latestVersionId: $latestVersionId, canPaginate: ${canPaginate.value}")
        if (canPaginate.value) {
            getHistoryVersions(
                objectId = vmParams.objectId,
                latestVersionId = latestVersionId
            )
        }
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

                else -> {
                    Timber.d("No interaction allowed with this listener type: $click")
                }
            }
        }
    }

    private suspend fun proceedWithRelationValueNavigation(
        relation: RelationKey,
        relationFormat: Relation.Format
    ) {
        val currentState =
            (_previewViewState.value as? VersionHistoryPreviewScreen.Success) ?: return
        val isSet = currentState is VersionHistoryPreviewScreen.Success.Set
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
                Timber.d("No interaction allowed with this property with format:$relationFormat")
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
                ObjectSearchConstants.filterParticipants(vmParams.spaceId)
            objectSearch(
                SearchObjects.Params(
                    filters = filters,
                    keys = ObjectSearchConstants.spaceMemberKeys,
                    space = vmParams.spaceId
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

    private fun getHistoryVersions(objectId: String, latestVersionId: String = "") {
        viewModelScope.launch {
            val params = GetVersions.Params(
                objectId = objectId,
                lastVersion = latestVersionId,
                limit = VERSIONS_MAX_LIMIT
            )
            getVersions.async(params).fold(
                onSuccess = { versions ->
                    canPaginate.value = versions.size == VERSIONS_MAX_LIMIT
                    if (latestVersionId.isEmpty()) {
                        if (versions.isEmpty()) {
                            _viewState.value = VersionHistoryState.Error.NoVersions
                        } else {
                            _versions.value = versions
                        }
                    } else {
                        _versions.value += versions
                    }
                    listState.value = ListState.IDLE
                    if (canPaginate.value) {
                        latestVisibleVersionId.value = versions.lastOrNull()?.id ?: ""
                    }
                },
                onFailure = {
                    _viewState.value = VersionHistoryState.Error.GetVersions
                    listState.value =
                        if (latestVersionId.isEmpty()) ListState.ERROR else ListState.PAGINATION_EXHAUST
                },
                onLoading = {}
            )
        }
    }

    private suspend fun handleVersionsSuccess(versions: List<Version>, members: List<ObjectWrapper.Basic>) {
        val groupedItems = groupItems(versions, members)
        _viewState.value = VersionHistoryState.Success(groups = groupedItems)
    }

    private suspend fun groupItems(
        versions: List<Version>,
        spaceMembers: List<ObjectWrapper.Basic>,
    ): List<VersionHistoryGroup> {

        // Sort versions by timestamp (DESC) and group by day
        val versionsByDay = versions
            .sortedByDescending { it.timestamp.time }
            .groupBy { version ->
                val formattedDate = dateProvider.formatToDateString(
                    timestamp = (version.timestamp.inMillis),
                    pattern = GROUP_BY_DAY_FORMAT
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
                spaceMembers = spaceMembers
            )

            VersionHistoryGroup(
                id = spaceMemberOldestVersion.id,
                title = getGroupTitle(spaceMemberLatestVersion.timestamp),
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

    private fun getGroupTitle(timestamp: TimestampInSeconds): GroupTitle {
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
                        pattern = pattern
                    )
                )
            }
        }
    }

    private suspend fun List<List<Version>>.toGroupItems(
        spaceMembers: List<ObjectWrapper.Basic>
    ): List<VersionHistoryGroup.Item> {
        return mapNotNull { versions ->
            val latestVersion = versions.firstOrNull() ?: return@mapNotNull null
            val spaceMemberId = latestVersion.spaceMember
            val spaceMember = spaceMembers.find { it.id == spaceMemberId }
                ?: return@mapNotNull null

            val objType = storeOfObjectTypes.getTypeOfObject(spaceMember)
            val icon = spaceMember.objectIcon(urlBuilder, objType)

            val (latestVersionDate, latestVersionTime) = dateProvider.formatTimestampToDateAndTime(
                timestamp = latestVersion.timestamp.inMillis
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
                        .firstOrNull()
                    if (event != null) {
                        val obj = ObjectWrapper.Basic(event.details[vmParams.objectId].orEmpty())
                        val currentState = _previewViewState.value
                        if (currentState !is VersionHistoryPreviewScreen.Hidden) {
                            parseObject(
                                payload = payload,
                                event = event,
                                item = item,
                                obj = obj
                            )
                        }
                    } else {
                        Timber.w("No ShowObject event found in payload for version ${item.id}")
                    }
                }
            }
        )
    }

    private suspend fun parseObject(
        payload: Payload,
        //TODO: Refactoring: update ShowVersion response to include ObjectView instead of Payload
        event: Event.Command.ShowObject,
        item: VersionHistoryGroup.Item,
        obj: ObjectWrapper.Basic
    ) {
        if (obj.layout.isDataView()) {
            defaultPayloadConsumer(payload)
            val root = event.blocks.find { it.id == vmParams.objectId }
            if (root == null) {
                Timber.w("Root block with id ${vmParams.objectId} not found in event blocks")
                return
            }
            val blocks = event.blocks.asMap().render(
                context = obj.id,
                mode = Mode.Read,
                root = root,
                focus = Editor.Focus.empty(),
                anchor = vmParams.objectId,
                indent = INITIAL_INDENT,
                details = ObjectViewDetails(event.details),
                restrictions = event.objectRestrictions,
                selection = emptySet()
            ).filterNot { it is BlockView.DataView }
            when (val currentState = _previewViewState.value) {
                VersionHistoryPreviewScreen.Loading -> {
                    _previewViewState.value = VersionHistoryPreviewScreen.Success.Set(
                        versionId = item.id,
                        blocks = blocks,
                        dateFormatted = item.dateFormatted,
                        timeFormatted = item.timeFormatted,
                        viewer = null,
                        icon = item.icon
                    )
                }
                is VersionHistoryPreviewScreen.Success.Set -> {
                    _previewViewState.value = currentState.copy(
                        versionId = item.id,
                        dateFormatted = item.dateFormatted,
                        timeFormatted = item.timeFormatted,
                        icon = item.icon,
                        blocks = blocks
                    )
                }
                else -> {
                    Timber.d("Version preview state is not loading or success.set, skipping state update")
                }
            }
        } else {
            val root = event.blocks.find { it.id == vmParams.objectId }
            if (root == null) {
                Timber.w("Root block with id ${vmParams.objectId} not found in event blocks")
                return
            }
            val blocks = event.blocks.asMap().render(
                context = obj.id,
                mode = Mode.Read,
                root = root,
                focus = Editor.Focus.empty(),
                anchor = vmParams.objectId,
                indent = INITIAL_INDENT,
                details = ObjectViewDetails(event.details),
                restrictions = event.objectRestrictions,
                selection = emptySet()
            )
            _previewViewState.value = VersionHistoryPreviewScreen.Success.Editor(
                versionId = item.id,
                blocks = blocks,
                dateFormatted = item.dateFormatted,
                timeFormatted = item.timeFormatted,
                icon = item.icon
            )
        }
    }

    private suspend fun mapToViewer(objectState: ObjectState.DataView): Viewer.GridView? {
        val dvViewer = objectState.viewerByIdOrFirst(null)
        val viewerRelations = dvViewer?.viewerRelations ?: return null

        val vmap = viewerRelations.associateBy { it.key }

        val dataViewRelations = objectState.dataViewContent.relationLinks.mapNotNull {
            storeOfRelations.getByKey(it.key)
        }
        val visibleRelations = dataViewRelations.filter { relation ->
            val vr = vmap[relation.key]
            vr?.isVisible ?: false
        }
        val columns = viewerRelations.toViewerColumns(
            relations = visibleRelations,
            filterBy = listOf(ObjectSetConfig.NAME_KEY)
        )

        return Viewer.GridView(
            id = dvViewer.id,
            name = dvViewer.name,
            columns = columns,
            rows = emptyList()
        )
    }

    data class VmParams(
        val objectId: Id,
        val spaceId: SpaceId
    )

    sealed class Command {
        data object Main : Command()
        data object VersionPreview : Command()
        data object ExitToObject : Command()
        data class RelationMultiSelect(val relationKey: RelationKey, val isSet: Boolean) : Command()
        data class RelationObject(val relationKey: RelationKey, val isSet: Boolean) : Command()
        data class RelationDate(val relationKey: RelationKey, val isSet: Boolean) : Command()
        data class RelationText(val relationKey: RelationKey, val isSet: Boolean) : Command()
    }

    companion object {
        const val GROUP_BY_DAY_FORMAT = "d MM yyyy"
        const val GROUP_DATE_FORMAT_CURRENT_YEAR = "MMMM d"
        const val GROUP_DATE_FORMAT_OTHER_YEAR = "MMMM d, yyyy"

        const val VERSIONS_MAX_LIMIT = 200
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
    sealed class Success : VersionHistoryPreviewScreen() {

        abstract val versionId: Id
        abstract val icon: ObjectIcon?
        abstract val dateFormatted: String
        abstract val timeFormatted: String

        data class Editor(
            override val versionId: Id,
            override val dateFormatted: String,
            override val timeFormatted: String,
            override val icon: ObjectIcon?,
            val blocks: List<BlockView>
        ) : Success()

        data class Set(
            override val versionId: Id,
            override val dateFormatted: String,
            override val timeFormatted: String,
            override val icon: ObjectIcon?,
            val viewer: Viewer.GridView?,
            val blocks: List<BlockView>
        ) : Success()
    }

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
        val timeStamp: TimestampInSeconds,
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