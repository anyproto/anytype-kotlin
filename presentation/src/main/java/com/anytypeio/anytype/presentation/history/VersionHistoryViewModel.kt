package com.anytypeio.anytype.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class VersionHistoryViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val getVersions: GetVersions,
    private val objectSearch: SearchObjects,
    private val dateProvider: DateProvider,
    private val localeProvider: LocaleProvider

) : ViewModel() {

    private val _viewState = MutableStateFlow<VersionHistoryState>(VersionHistoryState.Loading)
    val viewState = _viewState

    init {
        Timber.d("VersionHistoryViewModel created")
        getSpaceMembers()
    }

    fun onStart() {
        Timber.d("VersionHistoryViewModel started")
    }

    private fun getSpaceMembers() {
        viewModelScope.launch {
            val filters =
                ObjectSearchConstants.filterParticipants(spaces = listOf(vmParams.spaceId))
            objectSearch(
                SearchObjects.Params(
                    filters = filters,
                    keys = ObjectSearchConstants.spaceMemberKeys
                )
            ).process(
                failure = {
                    Timber.e(it, "Error while fetching new member")
                    _viewState.value = VersionHistoryState.Error.SpaceMembers(it.message.orEmpty())
                },
                success = { members ->
                    if (members.isEmpty()) {
                        _viewState.value =
                            VersionHistoryState.Error.SpaceMembers("No members found")
                    } else {
                        getHistoryVersions(vmParams.objectId, members)
                    }
                }
            )
        }
    }

    private fun getHistoryVersions(objectId: String, members: List<ObjectWrapper.Basic>) {
        viewModelScope.launch {
            val params = GetVersions.Params(objectId = objectId)
            getVersions.async(params).fold(
                onSuccess = { versions ->
                    if (versions.isEmpty()) {
                        _viewState.value = VersionHistoryState.Error.NoVersions
                    } else {
                        handleVersionsSuccess(versions, members)
                    }
                },
                onFailure = {
                    _viewState.value = VersionHistoryState.Error.GetVersions(it.message.orEmpty())
                },
                onLoading = {}
            )
        }
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

        // Group by day
        val versionsByDay = versions.groupBy { version ->
            val formattedDate = dateProvider.formatToDateString(
                timestamp = (version.timestamp.inMillis),
                pattern = GROUP_BY_DAY_FORMAT,
                locale = locale
            )
            formattedDate
        }

        // Sort days descending
        val sortedDays = versionsByDay.keys.sortedBy { it }

        // Within each day, sort all versions by timestamp descending
        val sortedVersionsByDay = sortedDays.associateWith { day ->
            val versionByDay = versionsByDay[day] ?: return@associateWith emptyList()
            versionByDay.sortedByDescending { it.timestamp.time }
        }

        // Group by space member sequentially within each day
        val groupedBySpaceMember = sortedVersionsByDay.mapValues { (_, versions) ->
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

        val groups = groupedBySpaceMember.mapNotNull { (_, spaceMemberVersions) ->
            if (spaceMemberVersions.isEmpty()) {
                return emptyList()
            }
            val spaceMemberLatestVersion =
                spaceMemberVersions.firstOrNull()?.firstOrNull() ?: return@mapNotNull null
            val (latestVersionDate, latestVersionTime) = dateProvider.formatTimestampToDateAndTime(
                timestamp = spaceMemberLatestVersion.timestamp.inMillis,
                locale = locale
            )
            val groupItems = spaceMemberVersions.toGroupItems(
                spaceMembers = spaceMembers,
                latestVersionTime = latestVersionTime
            )
            VersionHistoryGroup(
                id = spaceMemberLatestVersion.id,
                title = latestVersionDate,
                icons = emptyList(),
                items = groupItems
            )
        }
        return groups
    }

    private fun List<List<Version>>.toGroupItems(
        spaceMembers: List<ObjectWrapper.Basic>,
        latestVersionTime: String
    ): List<VersionHistoryGroup.Item> {
        return mapNotNull { versions ->
            val latestVersion = versions.firstOrNull() ?: return@mapNotNull null
            val spaceMemberId = latestVersion.spaceMember
            val spaceMember = spaceMembers.find { it.id == spaceMemberId }
                ?: return@mapNotNull null

            VersionHistoryGroup.Item(
                id = latestVersion.id,
                spaceMember = spaceMemberId,
                spaceMemberName = spaceMember.name.orEmpty(),
                timeStamp = latestVersion.timestamp,
                icon = null,
                timeFormatted = latestVersionTime,
                versions = versions
            )
        }
    }

    data class VmParams(
        val objectId: Id,
        val spaceId: Id
    )

    companion object {
        const val GROUP_BY_DAY_FORMAT = "d MM yyyy"
    }
}

sealed class VersionHistoryState {
    data object Loading : VersionHistoryState()
    data class Success(val groups: List<VersionHistoryGroup>) : VersionHistoryState()
    sealed class Error : VersionHistoryState() {
        data class SpaceMembers(val message: String) : Error()
        data class GetVersions(val message: String) : Error()
        data object NoVersions : Error()
    }
}

data class VersionHistoryGroup(
    val id: String,
    val title: String,
    val icons: List<ObjectIcon>,
    val items: List<Item>,
    val isExpanded: Boolean = false
) {
    data class Item(
        val id: Id,
        val spaceMember: Id,
        val spaceMemberName: String,
        val timeFormatted: String,
        val timeStamp: TimeInSeconds,
        val icon: ObjectIcon?,
        val versions: List<Version>
    )
}