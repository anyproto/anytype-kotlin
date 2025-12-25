package com.anytypeio.anytype.analytics.base

object EventsDictionary {

    /**
     * Analytics 2.0
     */

    // Auth events
    const val startCreateAccount = "StartCreateAccount"
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
    const val screenSettingsAccount = "ScreenSettingsAccount"
    const val clickDeleteSpace = "ClickDeleteSpace"
    const val clickDeleteSpaceWarning = "ClickDeleteSpaceWarning"
    const val createSpace = "CreateSpace"
    const val switchSpace = "SwitchSpace"
    const val reorderSpace = "ReorderSpace"
    const val deleteSpace = "DeleteSpace"
    const val pinSpace = "PinSpace"
    const val unpinSpace = "UnpinSpace"
    const val screenSettingsSpaceCreate = "ScreenSettingsSpaceCreate"
    const val clickConnectOneToOne = "ClickConnectOneToOne"


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
    const val deletionWarningShow = "ShowDeletionWarning"
    const val keychainPhraseScreenShow = "ScreenKeychain"
    const val relationsScreenShow = "ScreenObjectRelation"
    const val wallpaperScreenShow = "ScreenSettingsWallpaper"
    const val screenSettingsStorage = "ScreenSettingsStorageIndex"
    const val screenSettingsStorageManage = "ScreenSettingsStorageManager"
    const val screenSettingsSpaceStorageManager = "ScreenSettingsSpaceStorageManager"
    const val screenSettingsStorageOffload = "ScreenFileOffloadWarning"
    const val settingsStorageOffload = "SettingsStorageOffload"
    const val screenSettingsDelete = "ScreenSettingsDelete"

    // Object events
    const val objectListDelete = "RemoveCompletely"
    const val searchResult = "SearchResult"
    const val searchWords = "SearchWords"
    const val objectTypeChanged = "ChangeObjectType"
    const val selectObjectType = "SelectObjectType"
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
    const val objectCreateLink = "CreateLink"

    // Blocks events
    const val blockCreate = "CreateBlock"
    const val blockDelete = "DeleteBlock"
    const val blockChangeTextStyle = "ChangeTextStyle"
    const val blockChangeBlockStyle = "ChangeBlockStyle"
    const val blockChangeBlockAlign = "ChangeBlockAlign"
    const val blockChangeBackground = "ChangeBlockBackground"
    const val blockChangeColor = "ChangeBlockColor"
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
    const val relationDeleteValue = "DeleteRelationValue"
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
    const val screenSlashMenu = "ScreenSlashMenu"
    const val clickSlashMenu = "ClickSlashMenu"
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

    const val screenWidget = "ScreenWidget"
    const val addWidget = "AddWidget"
    const val editWidgets = "EditWidget"
    const val changeWidgetSource = "ChangeWidgetSource"
    const val changeWidgetLayout = "ChangeWidgetLayout"
    const val reorderWidget = "ReorderWidget"
    const val deleteWidget = "DeleteWidget"
    const val screenHome = "ScreenHome"
    const val clickWidgetTitle = "ClickWidgetTitle"
    const val screenWidgetMenu = "ScreenWidgetMenu"
    const val openSidebarObject = "OpenSidebarObject"

    //Templates
    const val selectTemplate = "SelectTemplate"
    const val clickNewOption = "ClickNewOption"
    const val changeDefaultTemplate = "ChangeDefaultTemplate"
    const val editTemplate = "EditTemplate"
    const val duplicateTemplate = "DuplicateTemplate"
    const val createTemplate = "CreateTemplate"
    const val logScreenTemplateSelector = "ScreenTemplateSelector"

    // Onboarding events
    const val screenOnboarding = "ScreenOnboarding"
    const val clickOnboarding = "ClickOnboarding"
    const val clickLogin = "ClickLogin"
    const val screenOnboardingEnterEmail = "ScreenOnboardingEnterEmail"
    const val screenOnboardingSkipEmail = "ScreenOnboardingSkipEmail"
    const val onboardingTooltip = "OnboardingTooltip"

    //Primitives
    const val logScreenEditType = "ScreenEditType"
    const val logReorderRelation = "ReorderRelation"
    const val logClickConflictFieldHelp = "ClickConflictFieldHelp"
    const val logAddConflictRelation = "AddConflictRelation"
    const val logResetToTypeDefault = "ResetToTypeDefault"
    const val logScreenTypeTemplateSelector = "ScreenTypeTemplateSelector"

    // Vault events

    const val screenVault = "ScreenVault"

    // About-app screen

    const val MENU_HELP = "MenuHelp"
    const val MENU_HELP_WHAT_IS_NEW = "MenuHelpWhatsNew"
    const val MENU_HELP_TUTORIAL = "MenuHelpTutorial"
    const val MENU_HELP_COMMUNITY = "MenuHelpCommunity"
    const val MENU_HELP_TERMS = "MenuHelpTerms"
    const val MENU_HELP_PRIVACY = "MenuHelpPrivacy"
    const val MENU_HELP_CONTACT_US = "MenuHelpContact"

    // Sharing extension

    const val CLICK_ONBOARDING_TOOLTIP = "ClickOnboardingTooltip"
    const val CLICK_ONBOARDING_TOOLTIP_ID_SHARING_EXTENSION = "SharingExtension"
    const val CLICK_ONBOARDING_TOOLTIP_TYPE_SHARING_MENU = "ShareMenu"
    const val CLICK_ONBOARDING_TOOLTIP_TYPE_CLOSE = "Close"

    // Sharing spaces

    const val clickQuote = "ClickQuote"
    const val shareSpace = "ShareSpace"
    const val screenSettingsSpaceShare = "ScreenSettingsSpaceShare"
    const val screenStopShare = "ScreenStopShare"
    const val stopSpaceShare = "StopSpaceShare"
    const val clickSettingsSpaceShare = "ClickSettingsSpaceShare"
    const val screenRevokeShareLink = "ScreenRevokeShareLink"
    const val revokeShareLink = "RevokeShareLink"
    const val screenInviteConfirm = "ScreenInviteConfirm"
    const val approveInviteRequest = "ApproveInviteRequest"
    const val rejectInviteRequest = "RejectInviteRequest"
    const val changeSpaceMemberPermissions = "ChangeSpaceMemberPermissions"
    const val removeSpaceMember = "RemoveSpaceMember"
    const val screenInviteRequest = "ScreenInviteRequest"
    const val screenRequestSent = "ScreenRequestSent"
    const val screenSettingsSpaceMembers = "ScreenSettingsSpaceMembers"
    const val screenLeaveSpace = "ScreenLeaveSpace"
    const val leaveSpace = "LeaveSpace"
    const val approveLeaveRequest = "ApproveLeaveRequest"

    // New sharing spaces events
    const val clickShareSpaceNewLink = "ClickShareSpaceNewLink"
    const val screenQr = "ScreenQr"
    const val clickShareSpaceCopyLink = "ClickShareSpaceCopyLink"
    const val clickJoinSpaceWithoutApproval = "ClickJoinSpaceWithoutApproval"
    const val clickShareSpaceShareLink = "ClickShareSpaceShareLink"

    //Version history
    const val screenHistory = "ScreenHistory"
    const val screenHistoryVersion = "ScreenHistoryVersion"
    const val restoreFromHistory = "RestoreFromHistory"

    //All content
    const val screenAllContent = "ScreenLibrary"//+
    const val changeLibraryType = "ChangeLibraryType"
    const val changeLibraryTypeLink = "ChangeLibraryTypeLink"
    const val searchInput = "SearchInput"
    const val libraryResult = "LibraryResult"
    const val changeLibrarySort = "ChangeLibrarySort"//+
    const val screenBin = "ScreenBin"//+

    //Date Object
    const val screenDate = "ScreenDate"
    const val switchRelationDate = "SwitchRelationDate"
    const val clickDateForward = "ClickDateForward"
    const val clickDateBack = "ClickDateBack"
    const val clickDateCalendarView = "ClickDateCalendarView"
    const val objectListSort = "ObjectListSort"

    //ObjectType
    const val screenObjectType = "ScreenType"
    const val editType = "EditType"
    const val changeRecommendedLayout = "ChangeRecommendedLayout"
    const val changeTypeSort = "ChangeTypeSort"
    const val screenTemplate = "ScreenTemplate"


    const val searchBacklink = "SearchBacklink"

    // Deep link events
    const val openObjectByLink = "OpenObjectByLink"

    object SharingSpacesTypes {
        const val shareTypeQR = "Qr"
        const val shareTypeMoreInfo = "MoreInfo"
        const val shareTypeRevoke = "Revoke"
        const val shareTypeShareLink = "ShareLink"
        const val shareTypeShareQr = "ShareQr"
    }

    object ShareSpaceLinkTypes {
        const val EDITOR = "Editor"
        const val VIEWER = "Viewer"
        const val MANUAL = "Manual"
    }

    object ScreenQrRoutes {
        const val INVITE_LINK = "InviteLink"
        const val SETTINGS_SPACE = "SettingsSpace"
        const val CHAT = "Chat"
    }

    object CopyLinkRoutes {
        const val BUTTON = "Button"
        const val MENU = "Menu"
    }

    object InviteRequestTypes {
        const val APPROVAL = "Approval"
        const val WITHOUT_APPROVAL = "WithoutApproval"
    }

    object SharingInviteRequest {
        const val reader = "Read"
        const val writer = "Write"
    }

    // Network mode
    const val selectNetwork = "SelectNetwork"
    const val uploadNetworkConfiguration = "UploadNetworkConfiguration"

    //Gallery experience
    const val screenGalleryInstall = "ScreenGalleryInstall"
    const val clickGalleryInstall = "ClickGalleryInstall"
    const val clickGalleryInstallSpace = "ClickGalleryInstallSpace"
    const val galleryInstallSuccess = "GalleryInstall"
    const val galleryParamNew = "New"
    const val galleryParamExisting = "Existing"

    //Membership
    const val screenMembership = "ScreenMembership"
    const val clickMembership = "ClickMembership"
    const val changePlan = "ChangePlan"

    //region --- Chats ---

    // Screen + open
    const val chatScreenChat = "ScreenChat" // props: route('Push'|'Navigation'), unreadMessageCount(Int), hasMentions(Boolean)
    const val chatOpenChatByPush = "OpenChatByPush" // fallback if route can't be added on ScreenChat

    // Attach menu flow
    const val chatScreenChatAttach = "ScreenChatAttach"
    const val chatClickScreenChatAttach = "ClickScreenChatAttach" // props: type('Object'|'Photo'|'File'|'Camera'), objectType(String)
    const val chatAttachItemChat = "AttachItemChat" // props: type('Object'|'Photo'|'File'|'Camera'), count(Int)
    const val chatDetachItemChat = "DetachItemChat"

    // Mentions
    const val chatMention = "Mention" // when user adds a mention; include route same as ScreenChat

    // Message menu clicks
    const val chatClickMessageMenuReply = "ClickMessageMenuReply"
    const val chatClickMessageMenuEdit = "ClickMessageMenuEdit"
    const val chatClickMessageMenuDelete = "ClickMessageMenuDelete"
    const val chatClickMessageMenuCopy = "ClickMessageMenuCopy"
    const val chatClickMessageMenuReaction = "ClickMessageMenuReaction"
    const val chatClickMessageMenuLink = "ClickMessageMenuLink"

    // Message mutations
    const val chatDeleteMessage = "DeleteMessage"
    const val chatAddReaction = "AddReaction"
    const val chatRemoveReaction = "RemoveReaction"
    const val chatSentMessage = "SentMessage" // props: type('Text'|'Attachment'|'Mixed')

    // Scroll intents
    const val chatClickScrollToBottom = "ClickScrollToBottom"
    const val chatClickScrollToMention = "ClickScrollToMention"
    const val chatClickScrollToReply = "ClickScrollToReply"

    // --- Vault menu ---
    const val chatScreenVaultCreateMenu = "ScreenVaultCreateMenu"
    const val chatClickVaultCreateMenuChat = "ClickVaultCreateMenuChat"
    const val chatClickVaultCreateMenuSpace = "ClickVaultCreateMenuSpace"

    // --- Helper enums for strongly-typed values (optional) ---
    enum class ChatRoute(val value: String) {
        PUSH("Push"),
        NAVIGATION("Navigation")
    }

    enum class ChatAttachType(val value: String) {
        OBJECT("Object"),
        PHOTO("Photo"),
        FILE("File"),
        CAMERA("Camera")
    }

    enum class ChatSentMessageType(val value: String) {
        TEXT("Text"),
        ATTACHMENT("Attachment"),
        MIXED("Mixed")
    }

    enum class UXType(val value: String) {
        CHAT("Chat"),
        SPACE("Space")
    }

    enum class OpenObjectByLinkType(val value: String) {
        OBJECT("Object"),
        INVITE("Invite")
    }

    //endregion

    enum class MembershipTierButton(val value: String) {
        INFO("LearnMore"),
        MANAGE("ManagePayment"),
        PAY("Pay"),
        SUBMIT("Submit"),
        CHANGE_EMAIL("ChangeEmail"),
        CONTACT_US("ContactUs")
    }

    enum class ScreenOnboardingStep(val value: String) {
        SOUL("Soul"),
        PHRASE("Phrase"),
        EMAIL("Email"),
        PERSONA("Persona"),
        USECASE("Usecase"),
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
        const val home = "HomeScreen"
        const val chat = "ScreenChat"
        const val widget = "Widget"
        const val searchScreen = "ScreenSearch"
        const val mention = "MenuMention"
        const val searchMenu = "MenuSearch"
        const val objCreateSet = "Set"
        const val objCreateHome = "Home"
        const val objDate = "Date"
        const val objCreateCollection = "Collection"
        const val allContentRoute = "Library"
        const val objCreateMention = "Mention"
        const val objPowerTool = "Powertool"
        const val objLink = "Link"
        const val keyboardBar = "KeyboardBar"
        const val slash = "Slash"
        const val slashMenu = "SlashMenu"
        const val screenSettings = "ScreenSettings"
        const val settings = "Settings"
        const val screenDeletion = "ScreenDeletion"
        const val navigation = "Navigation"
        const val longTap = "LongTap"
        const val sharingExtension = "SharingExtension"
        const val gallery = "Gallery"
        const val notification = "Notification"
        const val featuredRelations = "FeaturedRelations"
        const val objectRoute = "Object"
        const val typeRoute = "Type"
    }

    object Type {
        const val screenSettings = "ScreenSettings"
        const val firstSession = "FirstSession"
        const val beforeLogout = "BeforeLogout"
        const val menu = "Menu"
        const val general = "General"
        const val dataView = "dataview"
        const val block = "block"
        const val bookmark = "bookmark"
        const val anytype = "Anytype"
        const val localOnly = "LocalOnly"
        const val selfHost = "SelfHost"
        const val dateObject = "Date"
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
    const val id = "id"
    const val tab = "tab"
    const val route = "route"
    const val type = "type"
    const val uxType = "uxType"
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
    const val view = "view"
    const val step = "step"
    const val name = "name"
    const val spaceType = "spaceType"
    const val permissions = "permissions"
    const val sort = "sort"
}