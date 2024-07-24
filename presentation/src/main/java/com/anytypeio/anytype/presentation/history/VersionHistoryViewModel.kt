package com.anytypeio.anytype.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.history.Version
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
                    Timber.d("Members: $members")
                    getVersions(vmParams.objectId, members)
                }
            )
        }
    }

    private fun getVersions(objectId: String, members: List<ObjectWrapper.Basic>) {
        viewModelScope.launch {
            val params = GetVersions.Params(
                objectId = objectId
            )
            getVersions.async(params).fold(
                onSuccess = {
                    Timber.d("Versions: $it")
                    groupItems(it, members)
                },
                onFailure = {
                    _viewState.value = VersionHistoryState.Error.GetVersions(it.message.orEmpty())
                },
                onLoading = {}
            )
        }
    }

    private fun groupItems(versions: List<Version>, members: List<ObjectWrapper.Basic>) {
        // Group by day
        val versionsByDay = versions.groupBy { version ->
            val formattedDate = dateProvider.formatToDateString(
                timestamp = (version.timestamp * 1000),
                pattern = GROUP_BY_DAY_PATTERN,
                locale = localeProvider.locale()
            )
            formattedDate
        }

        // Sort days descending
        val sortedDays = versionsByDay.keys.sortedBy { it }

        // Within each day, sort all versions by timestamp descending
        val sortedVersionsByDay = sortedDays.associateWith { day ->
            versionsByDay[day]!!.sortedByDescending { it.timestamp }
        }

        // Group by author sequentially within each day
        val groupedByAuthor = sortedVersionsByDay.mapValues { (_, versions) ->
            val grouped = mutableListOf<MutableList<Version>>()
            var currentGroup = mutableListOf<Version>()

            for (version in versions) {
                if (currentGroup.isEmpty() || currentGroup.last().authorId == version.authorId) {
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

        val items = groupedByAuthor.map { (day, authorVersions) ->
            val (date, time) = dateProvider.formatTimestampToDateAndTime(
                timestamp = authorVersions.first().first().timestamp * 1000,
                locale = localeProvider.locale()
            )
            VersionHistoryGroup(
                id = authorVersions.first().first().id,
                title = date,
                icons = emptyList(),
                versions = authorVersions.map { versions ->
                    val membersObject = members.find { it.id == versions.first().authorId }
                    VersionHistoryItem(
                        id = versions.first().id,
                        authorId = versions.first().authorId,
                        authorName = membersObject?.name.orEmpty(),
                        timeStamp = versions.first().timestamp,
                        icon = null,
                        time = time,
                        versions = versions
                    )
                }
            )
        }

        _viewState.value = VersionHistoryState.Success(
            groups = items
        )
    }

    data class VmParams(
        val objectId: Id,
        val spaceId: Id
    )

    companion object {
        const val GROUP_BY_DAY_PATTERN = "d MM yyyy"
    }
}

sealed class VersionHistoryState {
    data object Loading : VersionHistoryState()
    data class Success(val groups: List<VersionHistoryGroup>) : VersionHistoryState()
    sealed class Error : VersionHistoryState() {
        data class SpaceMembers(val message: String) : Error()
        data class GetVersions(val message: String) : Error()
    }
}

data class VersionHistoryGroup(
    val id: String,
    val title: String,
    val icons: List<ObjectIcon>,
    val versions: List<VersionHistoryItem>
)

data class VersionHistoryItem(
    val id: Id,
    val authorId: Id,
    val authorName: String,
    val time: String = "",
    val timeStamp: Long,
    val icon: ObjectIcon?,
    val versions: List<Version>
)