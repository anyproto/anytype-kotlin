//package com.anytypeio.anytype.feature_object_type
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.Dispatchers
//
///**
// * A Kotlin version of the Swift TypeFieldsViewModel.
// */
//class SwiftTypeFieldsViewModel(
//    // You can inject these via constructor parameters or a DI framework:
//    private val document: BaseDocumentProtocol,
//    private val fieldsDataBuilder: TypeFieldsRowBuilder,
//    private val moveHandler: TypeFieldsMoveHandler,
//    private val relationsService: RelationsServiceProtocol,
//    private val relationDetailsStorage: RelationDetailsStorageProtocol,
//    private val relationsBuilder: SingleRelationBuilderProtocol
//) : ViewModel() {
//
//    // region Observables (analogous to Swift @Published)
//
//    private val _canEditRelationsList = MutableStateFlow(false)
//    val canEditRelationsList: StateFlow<Boolean> get() = _canEditRelationsList
//
//    private val _showConflictingInfo = MutableStateFlow(false)
//    val showConflictingInfo: StateFlow<Boolean> get() = _showConflictingInfo
//
//    private val _relationRows = MutableStateFlow<List<TypeFieldsRow>>(emptyList())
//    val relationRows: StateFlow<List<TypeFieldsRow>> get() = _relationRows
//
//    private val _relationsSearchData = MutableStateFlow<RelationsSearchData?>(null)
//    val relationsSearchData: StateFlow<RelationsSearchData?> get() = _relationsSearchData
//
//    private val _relationData = MutableStateFlow<RelationInfoData?>(null)
//    val relationData: StateFlow<RelationInfoData?> get() = _relationData
//
//    private val _conflictRelations = MutableStateFlow<List<RelationDetails>>(emptyList())
//    val conflictRelations: StateFlow<List<RelationDetails>> get() = _conflictRelations
//
//    private val _systemConflictRelations = MutableStateFlow<List<Relation>>(emptyList())
//    val systemConflictRelations: StateFlow<List<Relation>> get() = _systemConflictRelations
//
//    // endregion
//
//    private var parsedRelations: ParsedRelations = ParsedRelations.empty
//
//    /**
//     * Use this method to initialize all needed subscriptions
//     * that mirror Swift’s async tasks.
//     */
//    fun setupSubscriptions() {
//        viewModelScope.launch(Dispatchers.Main) {
//            // Launch each subscription in parallel.
//            launch { setupRelationsSubscription() }
//            launch { setupPermissionSubscription() }
//            launch { setupDetailsSubscription() }
//        }
//    }
//
//    /**
//     * Subscribes to the document’s parsedRelationsPublisherForType (Flow or similar).
//     */
//    private suspend fun setupRelationsSubscription() {
//        // Assuming `document.parsedRelationsFlowForType()` returns Flow<ParsedRelations>
//        document.parsedRelationsFlowForType()
//            .collect { relations ->
//                parsedRelations = relations
//                val newRows = fieldsDataBuilder.build(
//                    relations = relations.sidebarRelations,
//                    featured = relations.featuredRelations,
//                    systemConflictedRelations = _systemConflictRelations.value
//                )
//                // In Swift, you used withAnimation, but in Compose/Android you might just update state:
//                _relationRows.value = newRows
//            }
//    }
//
//    /**
//     * Subscribes to permission changes from the document.
//     */
//    private suspend fun setupPermissionSubscription() {
//        // Assuming `document.permissionsFlow()` returns Flow<Permissions>
//        document.permissionsFlow()
//            .collect { permissions ->
//                _canEditRelationsList.value = permissions.canEditRelationsList
//            }
//    }
//
//    /**
//     * Subscribes to details changes from the document.
//     */
//    private suspend fun setupDetailsSubscription() {
//        // Assuming `document.detailsFlow()` returns Flow<ObjectDetails>
//        document.detailsFlow()
//            .collect { details ->
//                try {
//                    updateConflictRelations(details)
//                } catch (_: Exception) {
//                    // Swift had `try?` which silently ignores errors
//                }
//            }
//    }
//
//    /**
//     * Mirrors Swift’s onRelationTap(_:)
//     */
//    fun onRelationTap(data: TypeFieldsRelationRow) {
//        val format = data.relation.format ?: return
//
//        _relationData.value = RelationInfoData(
//            name = data.relation.name,
//            objectId = document.objectId,
//            spaceId = document.spaceId,
//            target = RelationInfoData.Target.Type(isFeatured = data.relation.isFeatured),
//            mode = RelationInfoData.Mode.Edit(
//                relationId = data.relation.id,
//                format = format,
//                limitedObjectTypes = data.relation.limitedObjectTypes
//            )
//        )
//    }
//
//    /**
//     * Mirrors Swift’s onAddRelationTap(section:)
//     */
//    fun onAddRelationTap(section: TypeFieldsSectionRow) {
//        val excludedIds = _relationRows.value.mapNotNull { it.relationId }
//
//        _relationsSearchData.value = RelationsSearchData(
//            objectId = document.objectId,
//            spaceId = document.spaceId,
//            excludedRelationsIds = excludedIds,
//            target = RelationsSearchData.Target.Type(isFeatured = section.isHeader),
//            onRelationSelect = { details, isNew ->
//                // Log or do something
//                AnytypeAnalytics.instance().logAddExistingOrCreateRelation(
//                    format = details.format,
//                    isNew = isNew,
//                    type = AnytypeAnalytics.Type.TYPE,
//                    key = details.analyticsKey,
//                    spaceId = document.spaceId
//                )
//            }
//        )
//    }
//
//    /**
//     * Mirrors Swift’s onDeleteRelation(_:)
//     */
//    fun onDeleteRelation(row: TypeFieldsRelationRow) {
//        viewModelScope.launch {
//            val relationId = row.relation.id
//
//            document.details?.recommendedFeaturedRelations
//                ?.filter { it != relationId }
//                ?.let {
//                    relationsService.updateRecommendedFeaturedRelations(
//                        typeId = document.objectId,
//                        relationIds = it
//                    )
//                }
//
//            document.details?.recommendedRelations
//                ?.filter { it != relationId }
//                ?.let {
//                    relationsService.updateRecommendedRelations(
//                        typeId = document.objectId,
//                        relationIds = it
//                    )
//                }
//
//            val format = row.relation.format?.format
//            if (format == null) {
//                // Swift had `anytypeAssertionFailure("...")`
//                // You could do a Log or check():
//                println("Empty relation format for onDeleteRelation")
//                return@launch
//            }
//
//            AnytypeAnalytics.instance().logDeleteRelation(
//                spaceId = document.spaceId,
//                format = format,
//                route = AnytypeAnalytics.Route.OBJECT
//            )
//        }
//    }
//
//    /**
//     * Mirrors Swift’s onAddConflictRelation(_:)
//     */
//    fun onAddConflictRelation(relation: RelationDetails) {
//        viewModelScope.launch {
//            val newRecommendedRelations = parsedRelations.sidebarRelations.map { it.id }.toMutableList()
//            newRecommendedRelations.add(relation.id)
//
//            relationsService.updateRecommendedRelations(
//                typeId = document.objectId,
//                relationIds = newRecommendedRelations
//            )
//
//            // Update conflict relations after adding
//            document.details?.let {
//                updateConflictRelations(it)
//            }
//        }
//    }
//
//    /**
//     * Mirrors Swift’s updateConflictRelations(details:)
//     */
//    @Throws(Exception::class)
//    private suspend fun updateConflictRelations(details: ObjectDetails) {
//        val relationKeys = relationsService.getConflictRelationsForType(
//            typeId = document.objectId,
//            spaceId = document.spaceId
//        )
//
//        val allConflictRelations = relationDetailsStorage
//            .relationsDetails(relationKeys, document.spaceId)
//            .filter { !it.isHidden && !it.isDeleted }
//
//        // Filter out system vs. non-system
//        val nonSystem = allConflictRelations.filter {
//            !BundledRelationKey.systemKeys.map { key -> key.rawValue }.contains(it.key)
//        }
//        val system = allConflictRelations.filter {
//            BundledRelationKey.systemKeys.map { key -> key.rawValue }.contains(it.key)
//        }
//
//        _conflictRelations.value = nonSystem
//
//        // Build system conflict relations from details
//        _systemConflictRelations.value = system.mapNotNull { relationDetails ->
//            relationsBuilder.relation(
//                relationDetails = relationDetails,
//                details = details,
//                isFeatured = false,
//                relationValuesIsLocked = true,
//                storage = document.detailsStorage
//            )
//        }
//
//        // Rebuild relation rows
//        val newRows = fieldsDataBuilder.build(
//            relations = parsedRelations.sidebarRelations,
//            featured = parsedRelations.featuredRelations,
//            systemConflictedRelations = _systemConflictRelations.value
//        )
//        _relationRows.value = newRows
//    }
//}