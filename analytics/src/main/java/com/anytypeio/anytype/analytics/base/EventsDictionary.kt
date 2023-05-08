package com.anytypeio.anytype.analytics.base

object EventsDictionary {

    /**
     * Analytics 2.0
     */

    // Auth events
    const val createAccount = "CreateAccount"
    const val openAccount = "OpenAccount"
    const val logout = "LogOut"
    const val deleteAccount = "DeleteAccount"
    const val cancelDeletion = "CancelDeletion"

    // Dashboard view events
    const val showHome = "ScreenHome"
    const val selectHomeTab = "SelectHomeTab"
    const val reorderObjects = "ReorderObjects" // reorder in favorite tab
    const val restoreFromBin = "RestoreFromBin"

    // Settings events
    const val wallpaperSet = "SettingsWallpaperSet"
    const val keychainCopy = "KeychainCopy"
    const val defaultTypeChanged = "DefaultTypeChanged"
    const val fileOffloadSuccess = "FileOffload"

    // Screen show events
    const val objectScreenShow = "ScreenObject"
    const val authScreenShow = "ScreenIndex"
    const val loginScreenShow = "ScreenLogin"
    const val searchScreenShow = "ScreenSearch"
    const val createObjectNavBar = "CreateObjectNavBar"
    const val createObjectCollectionsNavBar = "CreateObjectCollectionsNavBar"
    const val signupScreenShow = "ScreenAuthRegistration"
    const val invitationScreenShow = "ScreenAuthInvitation"
    const val aboutAnalyticsScreenShow = "ScreenDisclaimer"
    const val deletionWarningShow = "ShowDeletionWarning"
    const val keychainPhraseScreenShow = "ScreenKeychain"
    const val fileOffloadScreenShow = "ScreenFileOffloadWarning"
    const val manageFilesScreenShow = "ScreenManageFiles"
    const val relationsScreenShow = "ScreenObjectRelation"
    const val settingsShow = "ScreenSettings"
    const val personalisationSettingsShow = "ScreenSettingsPersonalisation"
    const val wallpaperScreenShow = "ScreenSettingsWallpaper"
    const val accountDataSettingsShow = "ScreenSettingsAccountData"
    const val aboutScreenShow = "ScreenSettingsAbout"
    const val appearanceScreenShow = "ScreenSettingsAppearance"

    // Object events
    const val objectListDelete = "RemoveCompletely"
    const val searchResult = "SearchResult"
    const val searchWords = "SearchWords"
    const val objectTypeChanged = "ChangeObjectType"
    const val objectLayoutChange = "ChangeLayout"
    const val objectSetIcon = "SetIcon"
    const val objectRemoveIcon = "RemoveIcon"
    const val objectSetCover = "SetCover"
    const val objectRemoveCover = "RemoveCover"
    const val objectAddToFavorites = "AddToFavorites"
    const val objectRemoveFromFavorites = "RemoveFromFavorites"
    const val objectLinkTo = "LinkToObject"
    const val objectMoveToBin = "MoveToBin"
    const val objectRelationFeature = "FeatureRelation"
    const val objectRelationUnfeature = "UnfeatureRelation"
    const val objectLock = "LockPage"
    const val objectUnlock = "UnlockPage"
    const val objectUndo = "Undo"
    const val objectRedo = "Redo"
    const val objectCreate = "CreateObject"
    const val objectSetTitle = "SetObjectTitle"
    const val objectSetDescription = "SetObjectDescription"
    const val objectOpenAs = "OpenAsObject "
    const val objectReload = "ReloadSourceData"

    // Blocks events
    const val blockCreate = "CreateBlock"
    const val blockDelete = "DeleteBlock"
    const val blockChangeTextStyle = "ChangeTextStyle"
    const val blockChangeBlockStyle = "ChangeBlockStyle"
    const val blockChangeBlockAlign = "ChangeBlockAlign"
    const val blockChangeBackground = "ChangeBlockBackground"
    const val blockDuplicate = "DuplicateBlock"
    const val blockPaste = "PasteBlock"
    const val blockCopy = "CopyBlock"
    const val blockReorder = "ReorderBlock"
    const val blockUpload = "UploadMedia"
    const val blockDownload = "DownloadMedia"

    // Relations
    const val relationAdd = "AddExistingRelation"
    const val relationCreate = "CreateRelation"
    const val relationChangeValue = "ChangeRelationValue"
    const val relationDelete = "DeleteRelation"
    const val relationUrlOpen = "RelationUrlOpen"
    const val relationUrlCopy = "RelationUrlCopy"
    const val relationUrlEdit = "RelationUrlEditMobile"

    // Sets
    const val setScreenShow = "ScreenSet"
    const val collectionScreenShow = "ScreenCollection"
    const val turnIntoCollection = "SetTurnIntoCollection"
    const val setSelectQuery = "SetSelectQuery"
    const val addView = "AddView"
    const val switchView = "SwitchView"
    const val removeView = "RemoveView"
    const val changeViewType = "ChangeViewType"
    const val duplicateView = "DuplicateView"
    const val addFilter = "AddFilter"
    const val changeFilterValue = "ChangeFilterValue"
    const val removeFilter = "RemoveFilter"
    const val addSort = "AddSort"
    const val changeSortValue = "ChangeSortValue"
    const val removeSort = "RemoveSort"

    // Block Actions
    const val blockAction = "BlockAction"

    const val goBack = "HistoryBack"
    const val bookmarkOpenUrl = "BlockBookmarkOpenUrl"

    // Toolbars
    const val slashMenu = "KeyboardBarSlashMenu"
    const val styleMenu = "KeyboardBarStyleMenu"
    const val selectionMenu = "KeyboardBarSelectionMenu"
    const val mentionMenu = "KeyboardBarMentionMenu"

    // Library
    const val libraryView = "LibraryView"
    const val libraryScreenType = "ScreenType"
    const val libraryScreenRelation = "ScreenRelation"
    const val librarySetTypeName = "SetTypeName"
    const val libraryCreateType = "CreateType"

    // Routes
    object Routes {
        const val home = "ScreenHome"
        const val searchScreen = "ScreenSearch"
        const val mention = "MenuMention"
        const val searchMenu = "MenuSearch"
        const val objCreateSet = "Set"
        const val objCreateHome = "Home"
        const val objCreateMention = "Mention"
        const val objPowerTool = "Powertool"
        const val objTurnInto = "TurnInto"
        const val tabFavorites = "Favorites"
        const val screenSettings = "ScreenSettings"
        const val screenDeletion = "ScreenDeletion"
    }

    object Type {
        const val screenSettings = "ScreenSettings"
        const val firstSession = "FirstSession"
        const val beforeLogout = "BeforeLogout"
        const val menu = "menu"
        const val dataView = "dataview"
        const val block = "block"
        const val bookmark = "bookmark"
    }

    object BlockAction {
        const val addBelow = "AddBelow"
        const val delete = "Delete"
        const val duplicate = "Duplicate"
        const val moveTo = "MoveTo"
        const val move = "Move"
        const val style = "Style"
        const val download = "Download"
        const val preview = "Preview"
        const val copy = "Copy"
        const val paste = "Paste"
        const val openObject = "OpenObject"
    }
}

object EventsPropertiesKey {
    const val tab = "tab"
    const val route = "route"
    const val type = "type"
    const val format = "format"
    const val objectType = "objectType"
    const val length = "length"
    const val index = "index"
    const val context = "context"
    const val layout = "layout"
    const val style = "style"
    const val count = "count"
    const val color = "color"
    const val relationKey = "relationKey"
    const val condition = "condition"
    const val align = "align"
    const val originalId = "originalId"
}