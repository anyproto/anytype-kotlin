package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.chats.ChatPreviewContainer
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.notifications.NotificationPermissionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Delegate responsible for transforming Widget models into WidgetContainer instances.
 * Handles the Widget → WidgetContainer transformation logic that was previously in HomeScreenViewModel.
 */
interface WidgetContainerDelegate {

    /**
     * Creates a widget container from a widget model.
     *
     * @param widget The widget model to transform
     * @param currentlyDisplayedViews Current views for cache optimization
     * @return A widget container, or null if the widget type is not supported by this delegate
     */
    fun createContainer(
        widget: Widget,
        currentlyDisplayedViews: List<WidgetView>
    ): WidgetContainer?
}

/**
 * Default implementation of WidgetContainerDelegate.
 * Encapsulates all dependencies needed for widget container creation.
 */
class WidgetContainerDelegateImpl(
    private val spaceId: SpaceId,
    private val chatPreviews: ChatPreviewContainer,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val notificationPermissionManager: NotificationPermissionManager,
    private val fieldParser: FieldParser,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val treeWidgetBranchStateHolder: TreeWidgetBranchStateHolder,
    private val expandedWidgetIds: StateFlow<Set<Id>>,
    private val userSettingsRepository: UserSettingsRepository,
    private val isSessionActive: Flow<Boolean>,
    private val urlBuilder: UrlBuilder,
    private val objectWatcher: ObjectWatcher,
    private val getSpaceView: GetSpaceView,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val getObject: GetObject,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val storeOfRelations: StoreOfRelations,
    private val dateProvider: DateProvider,
    private val stringResourceProvider: StringResourceProvider,
    private val dispatchers: AppCoroutineDispatchers,
    private val observeCurrentWidgetView: (Id) -> Flow<ViewId?>,
    private val isWidgetCollapsed: (Widget, Set<Id>, Set<String>) -> Boolean
) : WidgetContainerDelegate {

    override fun createContainer(
        widget: Widget,
        currentlyDisplayedViews: List<WidgetView>
    ): WidgetContainer? {
        return when (widget) {
            is Widget.Chat -> createChatContainer(widget)
            is Widget.Link -> createLinkContainer(widget)
            is Widget.Tree -> createTreeContainer(widget, currentlyDisplayedViews)
            is Widget.List -> createListContainer(widget, currentlyDisplayedViews)
            is Widget.View -> createViewContainer(widget, currentlyDisplayedViews)
            is Widget.AllObjects -> createAllObjectsContainer(widget)
            is Widget.Bin -> createBinContainer(widget)
        }
    }

    private fun createChatContainer(widget: Widget.Chat): WidgetContainer {
        return SpaceChatWidgetContainer(
            widget = widget,
            container = chatPreviews,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            notificationPermissionManager = notificationPermissionManager
        )
    }

    private fun createLinkContainer(widget: Widget.Link): WidgetContainer {
        return LinkWidgetContainer(
            space = spaceId,
            widget = widget,
            fieldParser = fieldParser,
            chatPreviewContainer = chatPreviews,
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer
        )
    }

    private fun createTreeContainer(
        widget: Widget.Tree,
        currentlyDisplayedViews: List<WidgetView>
    ): WidgetContainer {
        return TreeWidgetContainer(
            widget = widget,
            container = storelessSubscriptionContainer,
            expandedBranches = treeWidgetBranchStateHolder.stream(widget.id),
            isWidgetCollapsed = combine(
                expandedWidgetIds,
                userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
            ) { expanded, collapsedSecs ->
                isWidgetCollapsed(widget, expanded, collapsedSecs)
            },
            isSessionActive = isSessionActive,
            urlBuilder = urlBuilder,
            objectWatcher = objectWatcher,
            getSpaceView = getSpaceView,
            onRequestCache = {
                currentlyDisplayedViews.find { view ->
                    view.id == widget.id
                            && view is WidgetView.Tree
                            && view.source == widget.source
                } as? WidgetView.Tree
            },
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes
        )
    }

    /**
     * Determines if a widget displays chat objects based on its source.
     * Only Sets can be detected (via setOf field). Collections are manually curated
     * so we cannot determine their content type from the Collection object itself.
     */
    private fun isListWidgetChatWidget(source: Widget.Source): Boolean {
        return when (source) {
            is Widget.Source.Default -> {
                // For sets, check if setOf points to CHAT_DERIVED type
                val setOfType = source.obj.map[Relations.SET_OF] as? Id
                setOfType == ObjectTypeIds.CHAT_DERIVED
            }
            else -> false
        }
    }

    private fun createListContainer(
        widget: Widget.List,
        currentlyDisplayedViews: List<WidgetView>
    ): WidgetContainer {
        return if (BundledWidgetSourceIds.ids.contains(widget.source.id)) {
            ListWidgetContainer(
                widget = widget,
                subscription = widget.source.id,
                storage = storelessSubscriptionContainer,
                isWidgetCollapsed = combine(
                    expandedWidgetIds,
                    userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
                ) { expanded, collapsedSecs ->
                    isWidgetCollapsed(widget, expanded, collapsedSecs)
                },
                urlBuilder = urlBuilder,
                isSessionActive = isSessionActive,
                objectWatcher = objectWatcher,
                getSpaceView = getSpaceView,
                onRequestCache = {
                    currentlyDisplayedViews.find { view ->
                        view.id == widget.id
                                && view is WidgetView.ListOfObjects
                                && view.source == widget.source
                    } as? WidgetView.ListOfObjects
                },
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        } else {
            // Check if this is a chat widget by source type
            if (isListWidgetChatWidget(widget.source)) {
                ChatListWidgetContainer(
                    space = spaceId,
                    widget = widget,
                    storage = storelessSubscriptionContainer,
                    getObject = getObject,
                    activeView = observeCurrentWidgetView(widget.id),
                    isWidgetCollapsed = combine(
                        expandedWidgetIds,
                        userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
                    ) { expanded, collapsedSecs ->
                        isWidgetCollapsed(widget, expanded, collapsedSecs)
                    },
                    isSessionActiveFlow = isSessionActive,
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                    onRequestCache = {
                        currentlyDisplayedViews.find { view ->
                            view.id == widget.id
                                    && view is WidgetView.ChatList
                                    && view.source == widget.source
                        } as? WidgetView.ChatList
                    },
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes,
                    chatPreviewContainer = chatPreviews,
                    dateProvider = dateProvider,
                    stringResourceProvider = stringResourceProvider,
                    spaceViewSubscriptionContainer = spaceViewSubscriptionContainer
                )
            } else {
                DataViewListWidgetContainer(
                    space = spaceId,
                    widget = widget,
                    storage = storelessSubscriptionContainer,
                    getObject = getObject,
                    activeView = observeCurrentWidgetView(widget.id),
                    isWidgetCollapsed = combine(
                        expandedWidgetIds,
                        userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
                    ) { expanded, collapsedSecs ->
                        isWidgetCollapsed(widget, expanded, collapsedSecs)
                    },
                    isSessionActiveFlow = isSessionActive,
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                    onRequestCache = {
                        currentlyDisplayedViews.find { view ->
                            view.id == widget.id
                                    && view is WidgetView.SetOfObjects
                                    && view.source == widget.source
                        } as? WidgetView.SetOfObjects
                    },
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes,
                    chatPreviewContainer = chatPreviews
                )
            }
        }
    }

    private fun createViewContainer(
        widget: Widget.View,
        currentlyDisplayedViews: List<WidgetView>
    ): WidgetContainer {
        // Check if this is a chat widget by source type
        return if (isListWidgetChatWidget(widget.source)) {
            ChatListWidgetContainer(
                space = spaceId,
                widget = widget,
                storage = storelessSubscriptionContainer,
                getObject = getObject,
                activeView = observeCurrentWidgetView(widget.id),
                isWidgetCollapsed = combine(
                    expandedWidgetIds,
                    userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
                ) { expanded, collapsedSecs ->
                    isWidgetCollapsed(widget, expanded, collapsedSecs)
                },
                isSessionActiveFlow = isSessionActive,
                urlBuilder = urlBuilder,
                coverImageHashProvider = coverImageHashProvider,
                onRequestCache = {
                    currentlyDisplayedViews.find { view ->
                        view.id == widget.id
                                && view is WidgetView.ChatList
                                && view.source == widget.source
                    } as? WidgetView.ChatList
                },
                storeOfRelations = storeOfRelations,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                chatPreviewContainer = chatPreviews,
                dateProvider = dateProvider,
                stringResourceProvider = stringResourceProvider,
                spaceViewSubscriptionContainer = spaceViewSubscriptionContainer
            )
        } else {
            DataViewListWidgetContainer(
                space = spaceId,
                widget = widget,
                storage = storelessSubscriptionContainer,
                getObject = getObject,
                activeView = observeCurrentWidgetView(widget.id),
                isWidgetCollapsed = combine(
                    expandedWidgetIds,
                    userSettingsRepository.getCollapsedSectionIds(spaceId).map { it.toSet() }
                ) { expanded, collapsedSecs ->
                    isWidgetCollapsed(widget, expanded, collapsedSecs)
                },
                isSessionActiveFlow = isSessionActive,
                urlBuilder = urlBuilder,
                coverImageHashProvider = coverImageHashProvider,
                onRequestCache = {
                    currentlyDisplayedViews.find { view ->
                        when (view) {
                            is WidgetView.SetOfObjects -> view.id == widget.id && view.source == widget.source
                            is WidgetView.Gallery -> view.id == widget.id && view.source == widget.source
                            else -> false
                        }
                    }
                },
                storeOfRelations = storeOfRelations,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes,
                chatPreviewContainer = chatPreviews
            )
        }
    }

    private fun createAllObjectsContainer(widget: Widget.AllObjects): WidgetContainer {
        return AllContentWidgetContainer(widget = widget)
    }

    private fun createBinContainer(widget: Widget.Bin): WidgetContainer {
        return BinWidgetContainer(widget = widget)
    }
}
