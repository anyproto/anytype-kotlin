package com.anytypeio.anytype.core_ui.widgets.objectIcon.custom_icons

import androidx.compose.ui.graphics.vector.ImageVector

object CustomIcons {

    fun getImageVector(name: String): ImageVector? {
        return iconsMap[name]
    }

    val iconsMap: Map<String, ImageVector> by lazy {
        mapOf(
            "accessibility" to CiAccessibility,
            "add-circle" to CiAddCircle,
            "airplane" to CiAirplane,
            "alarm" to CiAlarm,
            "albums" to CiAlbums,
            "alert-circle" to CiAlertCircle,
            "american-football" to CiAmericanFootball,
            "analytics" to CiAnalytics,
            "aperture" to CiAperture,
            "apps" to CiApps,
            "archive" to CiArchive,
            "arrow-back-circle" to CiArrowBackCircle,
            "arrow-down-circle" to CiArrowDownCircle,
            "arrow-forward-circle" to CiArrowForwardCircle,
            "arrow-redo" to CiArrowRedo,
            "arrow-redo-circle" to CiArrowRedoCircle,
            "arrow-undo" to CiArrowUndo,
            "arrow-undo-circle" to CiArrowUndoCircle,
            "arrow-up-circle" to CiArrowUpCircle,
            "at-circle" to CiAtCircle,
            "attach" to CiAttach,
            "backspace" to CiBackspace,
            "bag" to CiBag,
            "bag-add" to CiBagAdd,
            "bag-check" to CiBagCheck,
            "bag-handle" to CiBagHandle,
            "bag-remove" to CiBagRemove,
            "balloon" to CiBalloon,
            "ban" to CiBan,
            "bandage" to CiBandage,
            "bar-chart" to CiBarChart,
            "barbell" to CiBarbell,
            "barcode" to CiBarcode,
            "baseball" to CiBaseball,
            "basket" to CiBasket,
            "basketball" to CiBasketball,
            "battery-charging" to CiBatteryCharging,
            "battery-dead" to CiBatteryDead,
            "battery-full" to CiBatteryFull,
            "battery-half" to CiBatteryHalf,
            "beaker" to CiBeaker,
            "bed" to CiBed,
            "beer" to CiBeer,
            "bicycle" to CiBicycle,
            "binoculars" to CiBinoculars,
            "bluetooth" to CiBluetooth,
            "boat" to CiBoat,
            "body" to CiBody,
            "bonfire" to CiBonfire,
            "book" to CiBook,
            "bookmark" to CiBookmark,
            "bookmarks" to CiBookmarks,
            "bowling-ball" to CiBowlingBall,
            "briefcase" to CiBriefcase,
            "browsers" to CiBrowsers,
            "brush" to CiBrush,
            "bug" to CiBug,
            "build" to CiBuild,
            "bulb" to CiBulb,
            "bus" to CiBus,
            "business" to CiBusiness,
            "cafe" to CiCafe,
            "calculator" to CiCalculator,
            "calendar" to CiCalendar,
            "calendar-clear" to CiCalendarClear,
            "calendar-number" to CiCalendarNumber,
            "call" to CiCall,
            "camera" to CiCamera,
            "camera-reverse" to CiCameraReverse,
            "car" to CiCar,
            "car-sport" to CiCarSport,
            "card" to CiCard,
            "caret-back" to CiCaretBack,
            "caret-back-circle" to CiCaretBackCircle,
            "caret-down" to CiCaretDown,
            "caret-down-circle" to CiCaretDownCircle,
            "caret-forward" to CiCaretForward,
            "caret-forward-circle" to CiCaretForwardCircle,
            "caret-up" to CiCaretUp,
            "caret-up-circle" to CiCaretUpCircle,
            "cart" to CiCart,
            "cash" to CiCash,
            "cellular" to CiCellular,
            "chatbox" to CiChatbox,
            "chatbox-ellipses" to CiChatboxEllipses,
            "chatbubble" to CiChatbubble,
            "chatbubble-ellipses" to CiChatbubbleEllipses,
            "chatbubbles" to CiChatbubbles,
            "checkbox" to CiCheckbox,
            "checkmark-circle" to CiCheckmarkCircle,
            "checkmark-done-circle" to CiCheckmarkDoneCircle,
            "chevron-back-circle" to CiChevronBackCircle,
            "chevron-down-circle" to CiChevronDownCircle,
            "chevron-forward-circle" to CiChevronForwardCircle,
            "chevron-up-circle" to CiChevronUpCircle,
            "clipboard" to CiClipboard,
            "close-circle" to CiCloseCircle,
            "cloud" to CiCloud,
            "cloud-circle" to CiCloudCircle,
            "cloud-done" to CiCloudDone,
            "cloud-download" to CiCloudDownload,
            "cloud-offline" to CiCloudOffline,
            "cloud-upload" to CiCloudUpload,
            "cloudy" to CiCloudy,
            "cloudy-night" to CiCloudyNight,
            "code" to CiCode,
            "code-slash" to CiCodeSlash,
            "cog" to CiCog,
            "color-fill" to CiColorFill,
            "color-filter" to CiColorFilter,
            "color-palette" to CiColorPalette,
            "color-wand" to CiColorWand,
            "compass" to CiCompass,
            "construct" to CiConstruct,
            "contract" to CiContract,
            "contrast" to CiContrast,
            "copy" to CiCopy,
            "create" to CiCreate,
            "crop" to CiCrop,
            "cube" to CiCube,
            "cut" to CiCut,
            "desktop" to CiDesktop,
            "diamond" to CiDiamond,
            "dice" to CiDice,
            "disc" to CiDisc,
            "document" to CiDocument,
            "document-attach" to CiDocumentAttach,
            "document-lock" to CiDocumentLock,
            "document-text" to CiDocumentText,
            "documents" to CiDocuments,
            "download" to CiDownload,
            "duplicate" to CiDuplicate,
            "ear" to CiEar,
            "earth" to CiEarth,
            "easel" to CiEasel,
            "egg" to CiEgg,
            "ellipse" to CiEllipse,
            "ellipsis-horizontal-circle" to CiEllipsisHorizontalCircle,
            "ellipsis-vertical-circle" to CiEllipsisVerticalCircle,
            "enter" to CiEnter,
            "exit" to CiExit,
            "expand" to CiExpand,
            "extension-puzzle" to CiExtensionPuzzle,
            "eye" to CiEye,
            "eye-off" to CiEyeOff,
            "eyedrop" to CiEyedrop,
            "fast-food" to CiFastFood,
            "female" to CiFemale,
            "file-tray" to CiFileTray,
            "file-tray-full" to CiFileTrayFull,
            "file-tray-stacked" to CiFileTrayStacked,
            "film" to CiFilm,
            "filter-circle" to CiFilterCircle,
            "finger-print" to CiFingerPrint,
            "fish" to CiFish,
            "fitness" to CiFitness,
            "flag" to CiFlag,
            "flame" to CiFlame,
            "flash" to CiFlash,
            "flash-off" to CiFlashOff,
            "flashlight" to CiFlashlight,
            "flask" to CiFlask,
            "flower" to CiFlower,
            "folder" to CiFolder,
            "folder-open" to CiFolderOpen,
            "football" to CiFootball,
            "footsteps" to CiFootsteps,
            "funnel" to CiFunnel,
            "game-controller" to CiGameController,
            "gift" to CiGift,
            "git-branch" to CiGitBranch,
            "git-commit" to CiGitCommit,
            "git-compare" to CiGitCompare,
            "git-merge" to CiGitMerge,
            "git-network" to CiGitNetwork,
            "git-pull-request" to CiGitPullRequest,
            "glasses" to CiGlasses,
            "globe" to CiGlobe,
            "golf" to CiGolf,
            "grid" to CiGrid,
            "hammer" to CiHammer,
            "hand-left" to CiHandLeft,
            "hand-right" to CiHandRight,
            "happy" to CiHappy,
            "hardware-chip" to CiHardwareChip,
            "headset" to CiHeadset,
            "heart" to CiHeart,
            "heart-circle" to CiHeartCircle,
            "heart-dislike" to CiHeartDislike,
            "heart-dislike-circle" to CiHeartDislikeCircle,
            "heart-half" to CiHeartHalf,
            "help-buoy" to CiHelpBuoy,
            "help-circle" to CiHelpCircle,
            "home" to CiHome,
            "hourglass" to CiHourglass,
            "ice-cream" to CiIceCream,
            "id-card" to CiIdCard,
            "image" to CiImage,
            "images" to CiImages,
            "infinite" to CiInfinite,
            "information-circle" to CiInformationCircle,
            "invert-mode" to CiInvertMode,
            "journal" to CiJournal,
            "key" to CiKey,
            "keypad" to CiKeypad,
            "language" to CiLanguage,
            "laptop" to CiLaptop,
            "layers" to CiLayers,
            "leaf" to CiLeaf,
            "library" to CiLibrary,
            "link" to CiLink,
            "list" to CiList,
            "list-circle" to CiListCircle,
            "locate" to CiLocate,
            "location" to CiLocation,
            "lock-closed" to CiLockClosed,
            "lock-open" to CiLockOpen,
            "log-in" to CiLogIn,
            "log-out" to CiLogOut,
            "logo-alipay" to CiLogoAlipay,
            "logo-amazon" to CiLogoAmazon,
            "logo-amplify" to CiLogoAmplify,
            "logo-android" to CiLogoAndroid,
            "magnet" to CiMagnet,
            "mail" to CiMail,
            "mail-open" to CiMailOpen,
            "mail-unread" to CiMailUnread,
            "male" to CiMale,
            "male-female" to CiMaleFemale,
            "man" to CiMan,
            "map" to CiMap,
            "medal" to CiMedal,
            "medical" to CiMedical,
            "medkit" to CiMedkit,
            "megaphone" to CiMegaphone,
            "menu" to CiMenu,
            "mic" to CiMic,
            "mic-circle" to CiMicCircle,
            "mic-off" to CiMicOff,
            "mic-off-circle" to CiMicOffCircle,
            "moon" to CiMoon,
            "move" to CiMove,
            "musical-note" to CiMusicalNote,
            "musical-notes" to CiMusicalNotes,
            "navigate" to CiNavigate,
            "navigate-circle" to CiNavigateCircle,
            "newspaper" to CiNewspaper,
            "notifications" to CiNotifications,
            "notifications-circle" to CiNotificationsCircle,
            "notifications-off" to CiNotificationsOff,
            "notifications-off-circle" to CiNotificationsOffCircle,
            "nuclear" to CiNuclear,
            "nutrition" to CiNutrition,
            "options" to CiOptions,
            "paper-plane" to CiPaperPlane,
            "partly-sunny" to CiPartlySunny,
            "pause" to CiPause,
            "pause-circle" to CiPauseCircle,
            "paw" to CiPaw,
            "pencil" to CiPencil,
            "people" to CiPeople,
            "people-circle" to CiPeopleCircle,
            "person" to CiPerson,
            "person-add" to CiPersonAdd,
            "person-circle" to CiPersonCircle,
            "person-remove" to CiPersonRemove,
            "phone-landscape" to CiPhoneLandscape,
            "phone-portrait" to CiPhonePortrait,
            "pie-chart" to CiPieChart,
            "pin" to CiPin,
            "pint" to CiPint,
            "pizza" to CiPizza,
            "planet" to CiPlanet,
            "play" to CiPlay,
            "play-back" to CiPlayBack,
            "play-back-circle" to CiPlayBackCircle,
            "play-circle" to CiPlayCircle,
            "play-forward" to CiPlayForward,
            "play-forward-circle" to CiPlayForwardCircle,
            "play-skip-back" to CiPlaySkipBack,
            "play-skip-back-circle" to CiPlaySkipBackCircle,
            "play-skip-forward" to CiPlaySkipForward,
            "play-skip-forward-circle" to CiPlaySkipForwardCircle,
            "podium" to CiPodium,
            "power" to CiPower,
            "pricetag" to CiPricetag,
            "pricetags" to CiPricetags,
            "print" to CiPrint,
            "prism" to CiPrism,
            "pulse" to CiPulse,
            "push" to CiPush,
            "qr-code" to CiQrCode,
            "radio" to CiRadio,
            "radio-button-off" to CiRadioButtonOff,
            "radio-button-on" to CiRadioButtonOn,
            "rainy" to CiRainy,
            "reader" to CiReader,
            "receipt" to CiReceipt,
            "recording" to CiRecording,
            "refresh" to CiRefresh,
            "refresh-circle" to CiRefreshCircle,
            "reload" to CiReload,
            "reload-circle" to CiReloadCircle,
            "remove-circle" to CiRemoveCircle,
            "repeat" to CiRepeat,
            "resize" to CiResize,
            "restaurant" to CiRestaurant,
            "ribbon" to CiRibbon,
            "rocket" to CiRocket,
            "rose" to CiRose,
            "sad" to CiSad,
            "save" to CiSave,
            "scale" to CiScale,
            "scan" to CiScan,
            "scan-circle" to CiScanCircle,
            "school" to CiSchool,
            "search" to CiSearch,
            "search-circle" to CiSearchCircle,
            "send" to CiSend,
            "server" to CiServer,
            "settings" to CiSettings,
            "shapes" to CiShapes,
            "share" to CiShare,
            "share-social" to CiShareSocial,
            "shield" to CiShield,
            "shield-checkmark" to CiShieldCheckmark,
            "shield-half" to CiShieldHalf,
            "shirt" to CiShirt,
            "shuffle" to CiShuffle,
            "skull" to CiSkull,
            "snow" to CiSnow,
            "sparkles" to CiSparkles,
            "speedometer" to CiSpeedometer,
            "square" to CiSquare,
            "star" to CiStar,
            "star-half" to CiStarHalf,
            "stats-chart" to CiStatsChart,
            "stop" to CiStop,
            "stop-circle" to CiStopCircle,
            "stopwatch" to CiStopwatch,
            "storefront" to CiStorefront,
            "subway" to CiSubway,
            "sunny" to CiSunny,
            "swap-horizontal" to CiSwapHorizontal,
            "swap-vertical" to CiSwapVertical,
            "sync" to CiSync,
            "sync-circle" to CiSyncCircle,
            "tablet-landscape" to CiTabletLandscape,
            "tablet-portrait" to CiTabletPortrait,
            "telescope" to CiTelescope,
            "tennisball" to CiTennisball,
            "terminal" to CiTerminal,
            "text" to CiText,
            "thermometer" to CiThermometer,
            "thumbs-down" to CiThumbsDown,
            "thumbs-up" to CiThumbsUp,
            "thunderstorm" to CiThunderstorm,
            "ticket" to CiTicket,
            "time" to CiTime,
            "timer" to CiTimer,
            "today" to CiToday,
            "toggle" to CiToggle,
            "trail-sign" to CiTrailSign,
            "train" to CiTrain,
            "transgender" to CiTransgender,
            "trash" to CiTrash,
            "trash-bin" to CiTrashBin,
            "trending-down" to CiTrendingDown,
            "trending-up" to CiTrendingUp,
            "triangle" to CiTriangle,
            "trophy" to CiTrophy,
            "tv" to CiTv,
            "umbrella" to CiUmbrella,
            "unlink" to CiUnlink,
            "videocam" to CiVideocam,
            "videocam-off" to CiVideocamOff,
            "volume-high" to CiVolumeHigh,
            "volume-low" to CiVolumeLow,
            "volume-medium" to CiVolumeMedium,
            "volume-mute" to CiVolumeMute,
            "volume-off" to CiVolumeOff,
            "walk" to CiWalk,
            "wallet" to CiWallet,
            "warning" to CiWarning,
            "watch" to CiWatch,
            "water" to CiWater,
            "wifi" to CiWifi,
            "wine" to CiWine,
            "woman" to CiWoman
        )
    }
}

