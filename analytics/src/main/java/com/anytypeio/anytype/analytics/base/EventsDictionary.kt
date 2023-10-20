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
    const val reorderObjects = "ReorderObjects" // reorder in favorite tab
    const val restoreFromBin = "RestoreFromBin"

    // Settings events

    const val screenSettingSpacesSpaceIndex = "ScreenSettingsSpaceIndex"


    const val wallpaperSet = "SettingsWallpaperSet"
    const val keychainCopy = "KeychainCopy"
    const val defaultTypeChanged = "DefaultTypeChange"
    const val fileOffloadSuccess = "FileOffload"
    const val changeTheme = "ThemeSet"
    const val getMoreSpace = "GetMoreSpace"

    // Screen show events
    const val objectScreenShow = "ScreenObject"
    const val authScreenShow = "ScreenIndex"
    const val loginScreenShow = "ScreenLogin"
    const val searchScreenShow = "ScreenSearch"
    const val createObjectCollectionsNavBar = "CreateObjectCollectionsNavBar"
    const val signupScreenShow = "ScreenAuthRegistration"
    const val invitationScreenShow = "ScreenAuthInvitation"
    const val aboutAnalyticsScreenShow = "ScreenDisclaimer"
    const val deletionWarningShow = "ShowDeletionWarning"
    const val keychainPhraseScreenShow = "ScreenKeychain"
    const val relationsScreenShow = "ScreenObjectRelation"
    const val personalisationSettingsShow = "ScreenSettingsPersonal"
    const val wallpaperScreenShow = "ScreenSettingsWallpaper"
    const val accountDataSettingsShow = "ScreenSettingsAccount"
    const val aboutScreenShow = "ScreenSettingsAbout"
    const val appearanceScreenShow = "ScreenSettingsAppearance"
    const val screenSettingsStorage = "ScreenSettingsStorageIndex"
    const val screenSettingsStorageManage = "ScreenSettingsStorageManager"
    const val screenSettingsStorageOffload = "ScreenFileOffloadWarning"
    const val settingsStorageOffload = "SettingsStorageOffload"
    const val screenSettingsDelete = "ScreenSettingsDelete"

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
    const val objectDuplicate = "DuplicateObject"

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
    const val blockMove = "MoveBlock"

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
    const val repositionView = "RepositionView"
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
    const val hideKeyboard = "KeyboardBarHideKeyboardMenu"

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

    // Widgets

    const val addWidget = "AddWidget"
    const val editWidgets = "EditWidget"
    const val changeWidgetSource = "ChangeWidgetSource"
    const val changeWidgetLayout = "ChangeWidgetLayout"
    const val reorderWidget = "ReorderWidget"
    const val deleteWidget = "DeleteWidget"
    const val screenHome = "ScreenHome"
    const val selectHomeTab = "SelectHomeTab"

    //Templates
    const val selectTemplate = "SelectTemplate"
    const val clickNewOption = "ClickNewOption"
    const val changeDefaultTemplate = "ChangeDefaultTemplate"
    const val editTemplate = "EditTemplate"
    const val duplicateTemplate = "DuplicateTemplate"
    const val createTemplate = "CreateTemplate"

    // Onboarding events
    const val screenOnboarding = "ScreenOnboarding"
    const val clickOnboarding = "ClickOnboarding"
    const val clickLogin = "ClickLogin"

    // About-app screen

    const val MENU_HELP = "MenuHelp"
    const val MENU_HELP_WHAT_IS_NEW = "MenuHelpWhatsNew"
    const val MENU_HELP_TUTORIAL = "MenuHelpTutorial"
    const val MENU_HELP_COMMUNITY = "MenuHelpCommunity"
    const val MENU_HELP_TERMS = "MenuHelpTerms"
    const val MENU_HELP_PRIVACY = "MenuHelpPrivacy"
    const val MENU_HELP_CONTACT_US = "MenuHelpContact"

    enum class ScreenOnboardingStep(val value: String) {
        VOID("Void"),
        PHRASE("Phrase"),
        SOUL("Soul"),
        SOUL_CREATING("SoulCreating"),
        SPACE_CREATING("SpaceCreating")
    }

    enum class ClickOnboardingButton(val value: String) {
        SHOW_AND_COPY("ShowAndCopy"),
        CHECK_LATER("CheckLater")
    }

    enum class ClickLoginButton(val value: String) {
        PHRASE("Phrase"),
        QR("Qr"),
    }

    // Routes
    object Routes {
        const val home = "ScreenHome"
        const val searchScreen = "ScreenSearch"
        const val mention = "MenuMention"
        const val searchMenu = "MenuSearch"
        const val objCreateSet = "Set"
        const val objCreateHome = "Home"
        const val objCreateCollection = "Collection"
        const val objCreateMention = "Mention"
        const val objPowerTool = "Powertool"
        const val objTurnInto = "TurnInto"
        const val screenSettings = "ScreenSettings"
        const val screenDeletion = "ScreenDeletion"
        const val navigation = "Navigation"
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

    /**
     * This parameter is used to separate the use of the navigation bar from the dashboard or from the editor/sets
     */
    object View {
        const val viewHome = "Home"
        const val viewNavbar = "Navbar"
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
    const val view = "view"
    const val step = "step"
}