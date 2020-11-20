# Change log for Android @Anytype app.

## Version 0.1.4

### Fixes & tech 🚒

* Do not crash while DND on home dashboard if dropped item's index is invalid (#1111)
* When failed to fetch account after logging in with mnemonic phrase, return to keychain-login screen (#1112)
* Do not crash when failed to normalize url in markup (#1113)

### Middleware ⚙

* Updated middleware protocol to `0.13.26` (#1110)

## Version 0.1.3

### New features & enhancements 🚀

* Show loading state when referenced documents are being synced on dashboard, inside a page or in mention (#1072)
* Start scrolling-and-moving a block via action menu (no longer need to enter multi-selection mode in order to scroll-and-move a block) (#1055)
* Added loading state for logout operation (#1099)

### Fixes & tech 🚒

* Should save the latest text input if you close the page quickly (#1067)
* Keyboard won't show up in code snippet on Android 7 (#1024)
* Do not crash if document's title is broken after paste (#1108)

### Middleware ⚙

* Updated middleware protocol to `0.13.24` (#1100)

## Version 0.1.2

### New features & enhancements 🚀

* Code-snippet syntax highlighting. Our first iteration supports native syntax highlighting for Kotlin, Javascript, Go, Python, Typescript, JSON, and CSS. (#989)
* When creating a new page via mentions, the new page's name will be the text typed afterwards. (#994)
* When creating a new list item, it will inherit style properties from the previous list item after pressing the return key (#1017)
* Enabled search-on-page for profile document (#1027)
* Search text through media blocks (bookmark, link, file) (#1008)
* Users can change a code block's background colour (#1013)

### Fixes & tech 🚒

* Should not delete a text block's style properties when creating a mention inside this block (#699)
* When the checkbox is checked, all text should have the same color, even if this text block has markup (#833)
* When switching from reading mode to edit mode, create-mention trigger does not work (#1037)
* When copying a text block to the clipboard, style properties are now also copied (#1015)
* It's impossible to open the linked text just after adding a link to this text (#883)
* When navigating to a document via the navigation-structure screen, open this new document without passing by dashboard-screen (#1010)
* Prevent from moving the link into the document that this link is pointing to (#1025)
* Fix soft input visibility/focusing issues on Android 7.1 (#1029)
* Keyboard won't show up in code snippet (#1024)
* When changing image block's indentation, image is not scaled correctly (#1021)
* Setup for the upcoming login-with-QR-code feature (#822)

### Middleware ⚙

* Updated middleware protocol to `0.13.22` (#1045)

## Version 0.1.1

### New features & enhancements 🚀

* Search on page. Search through text blocks (except code block) on page (#990)
* Images can be full screen when tapped, allowing users to zoom in (#968)
* Disable animation for edit-mode in order to increase editor performance (#884)
* Should close style toolbar (instead of closing document) when back button pressed (#973)
* Create a new line inside code snippet on enter press (#970)
* Integrate three dots divider (#978)

### Design & UX 🔳

* Navigation icons updated (#992)

### Fixes & tech 🚒

* Survive process death and restore screen state when app returns from background (#985)
* When scroll-and-move is enabled, should move blocks according to document order, not selection order (#971)
* Emoji cross-platform sync issues (#969)
* Fix soft input visibility/focusing issues on Android 7 (#966)
* Change min sdk to Android 24 (#976)
* Cannot set carriage into an empty text block in large documents (#906)
* When changing icon for a document, link block should not have overlaying images (#890)
* Should show a correct error message when trying to move a block on the same position (#1007)

## Version 0.1.0

### New features 🚀

* Allow using code block in multi-select and scroll-and-move modes (#892)
* Enable mentions for all text blocks (except code block) (#939)
* Create both a new page and a mention pointing to this new page via mention bottom sheet (#631)
* Allow closing bottom sheet with mention suggests by pressing back button (#631)

### Design & UX 🔳

* Orange color as new accent color (#956)
* New design for add-link-markup bottom sheet (#774)
* Changed object names in add-block and turn-into panels (#752)

### Fixes & tech 🚒

* Prevent user from archiving profile document (#962)
* Show toast message when editor's content is copied to in-app clipboard (#958)
* Prevent view without focus from gaining focus on long click (#954)
* Support and render title as simple text block (#799)
* Url markup should have the same color as text block's text color (#818)
* Hiding keyboard via bottom toolbar just after typing results in losing text on screen (#953)
* Removing image from document results in broken set-icon-flow logic (#951)
* Set default text color if value is not present when processing text color's granular change (#946)
* Include checked/unchecked changes in granular-change-update mechanism (#945)
* New state handling for errors (observable subject). Show error only once (#943)
* Remove flickering effect when opening navigation screen (#941)
* Disable Crashlytics crash reporting for debug builds (#940)
* Should focus the last empty text block when clicking under document's blocks (#935)
* Clicking on empty space before document is loaded should not crash application (#930)
* Focusing on start may crash application by some users (#931)
* Send analytics for popup screens and button click events (#592)
* Prevent data racing issues while calculating the diff between two lists on home dashboard (#933)

### Middleware ⚙

* Updated middleware protocol to `0.13.20` (#851)

## Version 0.0.49

### Fixes & tech 🚒

* Render emoji apple icon in page top toolbar, fallback to system emoji in case of exception (#926)
* Disable DND for profile header on home dashboard (#923)
* Exclude text color and background marks where param value is equal to default value (#786)
* Try/catch instead of crashing for issues from 0.0.48 (#925)

## Version 0.0.48

### New features 🚀

* Automatically select the first loaded account when signing in (#869)

### Design & UX 🔳

* Indent aware scroll-and-move (#820)
* Enter multi-select mode via document's main menu (#896)
* Redesigned multi-select bottom panel (#872)
* Redesigned markup url-link bottom sheet (#774)
* Other design\layout fixes (#870)

### Fixes & tech 🚒

* Profile's empty name results in app's crash (#905)
* Update application package (#917)
* New config for Crashlytics for release project (#917)
* Invalidate incorrect ranges for markup (#908)
* Amplitude analytics for basic events (#592)
* Block-merge operations for documents containing sections (aka divs) (#912)
* App crashes when opening action menu for link block, which was created by turning a text block into a page (#910)
* Should create a new toggle on enter press at the end of the non-empty toggle block (#907)
* Should convert toggle block to paragraph on enter press if toggle block's text is empty (#886)
* When creating a new document and focusing its title, cursor should be visible (#903)
* Should not crash Android client when changing media block's background color on Desktop client (#814)
* Stretched background cover affects app's performance on home dashboard screen (#901)
* Remove Archive from Navigation links (inbound, outbound) (#919)
* Links on home dashboard sometimes disappear behind the center of the screen (#829)

### Middleware ⚙

* Updated middleware protocol to `0.13.13` (#851)

## Version 0.0.47

### New features 🚀

* Archive (#547)
* Link to existing object (#770)
* Move-to from one document to other document (#770)
* Invite code screen (#772)
* Allow user to add a block below via action menu (#771)

### Design & UX 🔳

* New cover for home dashboard (#839)
* Enhanced action-menu animation + background blur (#812)

### Fixes & tech 🚒

* Show alert dialog when failing to open a document (#823)
* Enhanced split-block operations (#731)
* Design fixes (#806)
* Action menu fixes (#665)
* Safely setting text color and background color (#858)
* Handle exceptions when emojifier fails to provide uri for emoji icon (#856)
* Render children for all text blocks (#846)
* Scroll-and-move restrictions issues (#847)
* Cannot add block after document's title via add-block-menu (#827)
* When navigating to a document via search-screen, open this new document without passing by dashboard-screen (#830)
* Inconsistent logic when adding markup in certain corner cases (#509)
* If you change checkbox's text color and then check off this checkbox, its text color always becomes black whereas it should have the color that you've set before (#785)

### Middleware ⚙

* Updated middleware protocol to `0.13.8` (#851)

## Version 0.0.46

### New features 🚀

* Mentions are rendered with images and emojis (#658)
* When adding new block via add-block screen, should replace current text block instead of adding a new block after this text block if this text block is empty (#325)

### Fixes & tech 🚒

* Prevent text block from gaining focus when opening its action-menu on long click (#776)
* Refactor navigation toolbar state handling inside `ControlPanelMachine` (#792)
* Remove turn-into action from page block's action menu (#787)
* Should change number color when changing numbered block's text color (#797)
* Document's image icon (uploaded from device's gallery) isn't visible in the mention suggester (#789)
* App crashes on setup-selected-account screen due to incorrect icon id (#739)
* Divider block should be selectable in multi-select and scroll-and-move mode (#778)
* Remove legacy selection param from `ControlPanelMachine` (#795)

## Version 0.0.45

### New features 🚀

* Test flight for turn-into restrictions in edit-mode and multi-select mode (#376)
* Styling panel | Switching between block-mode and markup-mode based on selection changes (#594)

### Design & UX 🔳

* Apple emojis or uploaded image in block-action-toolbar link's preview (#630)
* Uploading state for upload-image-for-document flow (#765)
* Update Graphik font file with the official one (#768)
* Navigate from context menu to styling panel (#594)

### Fixes & tech 🚒

* Removed archived pages from mention suggester and search results (#766)
* Checkbox state in action-menu preview is not synced with editor state (#748)
* User interactions with checkbox button are not synced correctly with the middleware (#749, #668)
* When searching for pages, if filter text is empty space, query returns only pages where title contains empty spaces (#746)
* Regression. Text is not always set when creating a lot of text blocks (#741)
* Respective theme colors should differ for text color and background colors in action menu (#738)
* Inconsistent DND-behavior on dashboard due to incorrect drop-target position calculation (#657)
* Fix app configuration lifetime (#735)
* Avatar image is not displayed after registration started after logout (#692)
* Editor business logic (event detection for backspace and enter press, checkbox button click detection, etc.) is broken for text blocks, whose style was changed via `turn-into-toolbar` in `multi-select` mode after returning to `edit` mode (#514)
* Should not show toast when clicking on markup url (#698)

## Version 0.0.44

### New features 🚀

* Download media (video and images) (#681)
* Turn-selected-block(s)-into-page(s) in edit and multi-select mode (#671)

### Design & UX 🔳

* Design fixes pack (profile, search, style toolbar, bookmark block, block icons on add-block/turn-into toolbar, etc.) (#602)
* Fix action toolbar constraints (#611)

### Fixes & tech 🚒

* Refactoring | Decomposed monolithic `BlockViewHolder`: DRY, better inheritance and composition (#645)
* Scroll-and-move restriction: prevent from moving parent into child (#696)
* Dot is missing after number in numbered block when its number and indent gets updated by payload change (#704)
* Nested block on-backspace-pressed deletion issues (#697)
* Library/framework updates (#687)
    * AAC `Navigation`,
    * AndroidX `Core`, `ConstraintLayout`, `RecyclerView`, `Fragment`, `Lifecycle`
    * Kotlin `DOKKA`
    * `Protobuf` Gradle plugin
    * Firebase `Crashlytics` Gradle plugin
* Updated `Kotlin` (`1.4.0`) and `Coroutines` (`1.3.9`) (#682)
* Image size issues (#648)
* Toggle's button stops working when switching from multi-select to edit mode (#643)
* Removed task-block-related legacy (#679)
* Removed "Color", "Background" actions for media blocks (#611)
* Removed "Add Caption", "Replace", "Rename" actions (#611)
* Render-state syncing for all text blocks (#719) 

## Version 0.0.43

### New features 🚀

* Support block indentation for paragraphs, checkboxes, bulleted and numbered lists (#617)
* Scroll-and-move restrictions (#616)

### Design & UX 🔳

* Changed main bottom-toolbar background (#660)
* Added app icon (#596)

### Fixes & tech 🚒

* Render-state syncing (from GUI to VM, from VM to GUI) refactoring (#663) 
* After updating document's image, this image is only updated after reopening the document (#642)
* Event subscription lifecycle issues (#675)

## Version 0.0.42

### New features 🚀

* Mention suggests (#574)
* Editable mentions (#573)

### Design & UX 🔳

* New design for scroll-and-move targeting (#636)
* Enhanced scroll & move targeting (#636)

### Fixes & tech 🚒

* Turn off custom context menu (#594)
* Should not crash the app when opening action menu for currently focused block (#635)
* DI/Dagger optimizations (#626)
* Refactored `ControlPanelMachine` (#634)
* Updated Dagger to `2.28.3` (#627)
* Safely setting emoji icon on home dashboard and in editor if image not found in our data set when searching by unicode (#638)
* Show error if we failed to start account while sign-in (#633)

### Middleware ⚙

* Updated middleware protocol to `0.13.0` (#454)

## Version 0.0.41

### New features 🚀

* Setting random emoji icon when creating a new page (#603)

### Design & UX 🔳

* Enhanced scroll & move targeting (#610)
* Redesigned block toolbar (#590)
* Redesigned keychain dialog screen (#614)
* Checked and fixed line-spacing values for text blocks in editor (#614)

### Fixes & tech 🚒

* Turn-into in multi-select mode should not break selected/unselected-state-related logic (#621)
* App should not crash when user presses change-style button or open-action-menu button on block-toolbar when document's title is focused (#620)
* Drag-and-drop area issues on home dashboard (#570)
* Should not navigate to congratulation screen (designed for sign-up flow) after sign-in (#606)
* Setup analytics module (#618)

### Middleware ⚙

* Updated middleware protocol to `0.12.2` (#454)

## Version 0.0.40

### New features 🚀

* Test flight for scroll-and-move feature (#567)

### Fixes & tech 🚒

* Added new custom span for rendering mentions in text blocks (#563)
* Added new text watcher for intercepting mention-related events (#574)

## Version 0.0.39

### New features 🚀

* Search-document-engine screen integrated on home dashboard and pages (#555)
* Picking/removing avatar image for profile document (#568)
* Setting links via block-styling toolbar (#559)

### Design & UX 🔳

* New animation for action menu (#464)
* Redesigned avatar and greeting text sizes and relative positioning on home dashboard (#571)
* Redesigned add-block/turn-into bottom sheet (new palette, updated categories) (#572, #569)

### Fixes & tech 🚒

* Should not crash app when opening the archive from the workspace-navigation-structure screen (#582)
* Should not crash app when opening _link to_ tab on workspace-navigation-structure screen (#576)
* When creating a new nested page (B) inside some other page (A), the link block for the page B should be present on the page A when user navigates back to the page A (#561)

## Version 0.0.38

### New features 🚀

* New workspace-navigation screen integrated for home dashboard and pages (#552, #553, #556)

## Version 0.0.37

### New features 🚀

* Editable profile document (#504)
* New emoji search engine in document-icon picker (#549) 

### Design & UX 🔳

* Using Apple emojis as document icons (#542)
* Redesigned profile screen (#504)

## Version 0.0.36

### New features 🚀

* User can set image icon for document by choosing an image from device (#535)
* Styling toolbar shows currently applied style in markup-styling mode (#525)
* Wired document's icon with action menu (#529)
* User can upload files from device's cloud (#537)

### Design & UX 🔳

* Redesigned page emoji icon picker (#531)
* Empty state (zero blocks selected) for multi-select mode (#527)
* Uploading state for 

### Fixes & tech 🚒

* Fixed file permission issues on Android 10 and 11 (#334)

## Version 0.0.35

### New features 🚀

* Custom markup context menu enabled by default. Test flight (#483)
* Styling toolbar shows currently applied style in block-styling mode (#503)

### Design & UX 🔳

* Second iteration for custom markup context menu: y-positioning (relative to text), button states (#483)
* Inter (regular, medium, bold) is now the main font in the editor (#522)
* Redesigned selected states for tabs in styling toolbar (#506)
* Redesigned selected states for markup and alignment in styling toolbar (#506)
* Redesigned selected states for background and text color tabs in styling toolbar (#506)

### Fixes & tech 🚒

* Support suggestions for custom keyboards (#466)
* Should ignore split-line `enter` press in document's title (#513)
* Setting cursor when pasting from anytype clipboard (#484)
* Should focus document's title when first paragraph (as the first block in the document) is deleted (#498)

## Version 0.0.34

### New features 🚀

* Enabled markup for headers and highlight blocks (#480)

### Design & UX 🔳

* New screen for debug settings (#492)
* Custom context menu. First iteration available only in debug mode (#430)

### Fixes & tech 🚒

* Enabled markup links (#200)
* Added UI and integrations tests for basic CRUD, split and merge operations in editor (#497)
* Better control over cursor position while CRUD, split and merge operations in editor (#491)
* Fix incorrect cursor positioning while deleting an empty block (#493)
* Fix Inconsistent behavior when merging two highlight blocks (#478)
* Should preserve text style while splitting (#479)
* Should focus and open keyboard when creating headers or highlight block (#485)

## Version 0.0.33

### New features 🚀

* Select text and copy-paste inside Anytype. First iteration (#467)
* Copy and paste multiple blocks in multi-select mode. First iteration (#467)

### Design & UX 🔳

* Undo/redo migrated to document's context menu (#461)

### Fixes & tech 🚒

* Resolve race conditions on split and merge (#463, #448)
* Turn-into code block in edit-mode and multi-select mode does not work (#468)

## Version 0.0.32

### New features 🚀

* User can paste from web to Anytype. First iteration (#447)
* Turn-into in multi-select mode for text blocks (#458)
* All media blocks can be selected in multi-select mode (#427, #428)

### Fixes & tech 🚒

* New and more stable enter-press detection (#449)
* Refactored media block click handling (#427, #428)
* Load profile picture from local http-server instead of loading image blob (#431)
* Should persist link markup while editing text (#455)
* Regression | Should convert an empty list block to a paragraph on enter-pressed event (#457)
* Inconsistent backspace detection when user presses backspace on non-empty text where selection > 0 (#450)

### Middleware ⚙

* Updated middleware to `0.11.0` (#454)

## Version 0.0.31

### New features 🚀

* User can add code block (#409)

### Design & UX 🔳

* New bookmark block design (#422)
* Render bookmark in multi-select mode (#422)
* Updated subtitles for add-block or turn-into bottom sheet items (#429)
* Text background should have the same height as the OS text-selection highlight (#392)
* Text background should have z-axis priority lower as the one of the OS text-selection highlight (#426)

### Fixes & tech 🚒

* Migrate from short name emojis to unicode when parsing document icons (#408)
* Consuming event payload from middleware callaback responses (#408)
* Hard-coded alpha invite code for internal use (#408)
* `PageViewModel` refactoring (#408)
* Better logging for middleware requests and responses (#421)
* Should persist home dashboard document order (#425)
* New way to render background mark: using `Annotation` span instead of `BackgroundColorSpan` (#436)

### Middleware ⚙

* Updated middleware to `0.9.0` (#339)

## Version 0.0.30

### New features 🚀

* Multi-select mode: user can enter/exit this mode, select and delete blocks (#404).
* Multi-select mode: turn-into (not stable) (#375)
* Enable action toolbar for media blocks (#405)

### Design & UX 🔳

* Multi-select toolbar (top and bottom) ($404)
* Basic animations on entering/exiting multi-select mode (#404)
* Added background state selector for editor blocks (#404)
* Padding and margin fixes for editor blocks ($404)

### Fixes & tech 🚒

* Should render multi-line text in action toolbar block preview (#405)
* Action toolbar supports text color and background color (#405)
* Action toolbar has its own layouts (for a better separation of concerns) (#405)
* Do not crash app wheh failing to parse bookmark uri (#414)
* Migrate from Fabric to FirebaseCrashlytics (#414)
* Read/edit mode switcher for editor (#404)
* Refactored top navigation bar in document (switched to custom widget implementation) (#406)

## Version 0.0.29

### New features 🚀

* Navigation from bookmark block to device browser (#390)
* New block-action toolbar enabled for all text blocks (#382)

### Design & UX 🔳

* Text block previews in the new block-action toolbar now have the same style as in the editor (#382)
* Add-block toolbar should have its title hidden while scrolling (#374)
* Block-styling toolbar in block styling mode (applying text color, background to the whole block) (#379)
* Enabled style page features in block-styling toolbar (#379)
* Fixed collapsing toolbar animation on home-dashboard screen (#384)
* New `turn-into` toolbar (#386)
* Skip collapsed state for bottom sheet dialogs (`add-block`, `turn-into`) (#391)
* Ellipsize and reduce bookmark's description to two lines, bookmark's title to one line (#390)

### Fixes & tech 🚒

* Should open keyboard and focus the target when a new block is added to the document (#388)
* Should close keyboard after document archiving (#395)
* Should hide archived documents from home dashboard screen (#387)
* Fix carriage positioning for `split` / `merge` operations (#353)
* Main layout optimization (switched to `FragmentContainerView`) (#385)
* Refactored custom context menu for text blocks (#393)
* Title and emoji for inner document link's title and emoji icon taken from details (#389)

## Version 0.0.28

### Design & UX 🔳

* New block-styling toolbar with swiping pages (enabled only for markup/selected text editing) (#366)
* New block-action toolbar (enabled only on paragraph blocks) (#366)
* New main toolbar with options: `add-block`, `multi-select` (disabled), `remove-focus` (#370)
* New behavior for create-new-page (+) button on editor screen: (+) button is hidden while scrolling (#377)
* Skipping `collapsed` state while closing page bottom sheet (#377)

### Fixes & tech 🚒

* Updated Kotlin to `1.3.72` (#378)
* Switched from hex color codes to named colors (#377)
* Refactored markup-related spans to implement custom interface (better control over removing spans from text while updates) (#377)

## Version 0.0.27

### Design 🔳

* Rendering bookmark in error / failed-to-load state (#351)
* New markup menu (instead of bottom toolbar) (#348)
* Emoji picker issues (alignment, empty spaces, etc) (#324)

### Fixes & tech 🚒

* Get title and icon from document details (#356)
* Wire bookmark menu with action toolbar (#351)
* Add-block bottom sheet has incorrect fonts in title and subtitle (#361)
* Do not show main toolbar when no block is focused on a page (#103)
* Create the block when user taps under all types of non-empty blocks (#350)
* Duplicate action should transfer the carriage to a new block (#352)
* Duplicated-platform-classes issue caused by `emoji-java` library breaks Github Actions CI (#357)

### Middleware ⚙

* Updated middleware to `0.5.0` (#339)
* Added `blockSetDetails` command (#339)

## Version 0.0.26

### New features 🚀

* Navigation to desktop from any page on bottom-swipe gesture (#316)
* Hot keys for patterns (`bullet`, `numbered`, `h1`, `h2`, `h3`, `quote`, `toggle`, `checkbox`, `divider`)  (#340)

### Design 🔳

* Redesigned add-block panel (using new bottom sheet design) (#329)
* Design fixes for list-item blocks (alignment, padding, etc.) (#328)
* Update checkbox's text color in checked-state (#328)
* Image block scaling-related fixes (#326)

### Fixes & tech 🚒

* Should focus title block after page creation (#323)
* Should close keyboard when exiting page via toolbar's back button (#338)

## Version 0.0.25

### New features 🚀

* Undo/redo changes in document (unstable) (#284)
* User can archive documents (#293)

### Design 🔳

* Added navigation bar with title and icon for pages (#293)

### Fixes & tech 🚒

* Should open new page after its creation on some other page (#283)
* Should update link block titles when corresponding page titles have been updated (#283)
* Should set "Untitled" as link's title if it's not set or blank (#283)

### Middleware ⚙

* Added `blockUndo` command (#284)
* Added `blockRedo` command (#284)
* Added `blockSetPageIsArchived` command (#293)

## Version 0.0.24

### New features 🚀

* User can add bookmark placeholder and create bookmark from url (#140)
* User can add image blocks (#139)
* User can add file blocks (#295)
* User can add toggle blocks and change expanded/collapsed state (#313)
* Added support for nested blocks rendering (#313)

### Fixes & tech 🚒

* Toolbars should not prevent user from scrolling page to its end (#310)
* Should create a new block after the target block when user adds a new block via add-block toolbar (#305)
* Refactored block creation in `Middleware` and reduced code duplication (introduced factory to create a block from a block prototype) (#140)
* New mappers (from middleware layer entity to data layer entity) (#140)
* Introduced new rendering converter (from business tree-like data structures to flattened view data structures) (#313)

### Middleware ⚙

* Added `blockBookmarkFetch` command (#140)
* Added `blockUpload` command (#295)

## Version 0.0.23

### New features 🚀

* Bookmark block rendering (#290)
* User can add video blocks (#142)
* User can watch video from video blocks (#142)

### Fixes & tech 🚒

* Refactored create-block requests (#142)

## Version 0.0.22

### New features 🚀

* User can download files on phone (#256)
* User can set an emoji as page icon (#280)

### Fixes & tech 🚒

* Update Kotlin to 1.3.70 (#278)

### Design 🔳

* Fix home dashboard list item spacing (#258)
* Different icons for different mime types for file blocks (#288)
* Page icon picker (#280)

### Middleware ⚙

* Added `blockSetIconName` command (#280)

## Version 0.0.21

### New features 🚀

* Render file and picture blocks (#255)

### Design 🔳

* Added page-icon-picker widgets: layout, adapters, etc. (#243)

### Fixes & tech 🚒

* Should hide keyboard when closing a page (#263)
* Fixed emoji transparency issue (#261)
* New models for files in `domain` and `data` modules + mappers (#269)
* Provide config object for the whole app (#272)
* Added `UrlBuilder` for building urls for file and pictures (#272)
* Updated middleware config model to include gateway url (#270)

## Version 0.0.20

### New features 🚀

* Allow users to split blocks (not stable yet) (#229)
* Allow users to set background color to block layouts (#244)
* Allow users to create new pages on home dashboard by pressing (+) button on page screen (#191)
* Allow users to add divider blocks (#234)
* Enable sub-page navigation (naive implementation) (#235)
* Implemented new back navigation: closing pages on swipe-down gesture (#231)

### Design 🔳

* One-line (ellipsized) page titles on home dashboard screen (#233)

### Fixes & tech 🚒

* Turn-into panel is still visible when system opens virtual keyboard on focus (#169)
* Missing diff-util implementation for headers results in app crash (#227)
* Option toolbars (`add-block`, `turn-into`, `color`, `action`) are still visible when system opens virtual keyboard on focus (#102)
* Default text color and default background color from app ressources aren't converted correctly to hex color code (#204)
* Added scenarios for UI-testing (#241)

### Middleware ⚙

* Added `blockSplit` command (#229)
* Added `blockSetTextBackgroundColor` command (#244)

## Version 0.0.19

### New features 🚀

* Allow users to create numbered lists (nested lists are not supported) (#156)
* Allow users to create a sub-page (navigation is not supported) (#214)

### Fixes & tech 🚒

* Fix: Text watcher is not always removed when the corresponding block is deleted (#221)
* Testing: added basic unit testing for BlockAdapter (#220)
* Testing: added first UI tests for editor/page (#208)

## Version 0.0.18

### New features 🚀

* Merge the target block with the previous block if the carriage of the target block is positioned at the beginning of the text on backspace-pressed event (#159)
* Turn a list item into a paragraph on empty-block-enter-pressed event (#207)
* Enable keyboard/code (not stable yet) (#80)

### Fixes & tech 🚒

* Improved `BlockViewDiffUtil` implementation (better change payload procession) (#164, #195)
* Page titles on the home dashboard are not always updated when user returns back from a page to the home dashboard (#199)
* Inconsistent behaviour while editing page's title on page screen (#182)
* Event channel refactoring (decreased code duplication) (#194)

### Middleware ⚙

* Added `blockMerge` command (#159)

## Version 0.0.17

### New features 🚀

* User can now use the color toolbar to change the text color of the whole text block (#153)
* User can now use the markup-color toolbar to change the background color of the selected text (#111)
* Create a checkbox-list item on enter-pressed-event (instead of a simple paragraph) (#155)
* Create a bulleted-list item on enter-pressed-event (instead of a simple paragraph) (#154)
* `Block.Content.Text` model now has optional `color` property (#153).
* Added documentation engine (`DOKKA`) for `domain` module: documentation is generated automatically from KDoc (#168).
* Added new content model: `Block.Content.Link` (#173)

### Design 🔳

* Updated app fonts (#183)
* Removed shadows from cards (#177)

### Fixes 🚒

* User cannot undo markup formatting if there are already several markups of the same type in the text (#151)
* Markup is broken when user splits the range (#122)
* Page title changes are not saved after user pressed backspace on empty page title block (#185). 

### Middleware ⚙

* Updated middleware library and protocol to 0.2.4 (#173, #181)
* Added `blockCreatePage` command (#173)
* Added `blockSetTextColor` command (#153). 
* Added `accountStop` command (#180)

## Version 0.0.16

### New features 🚀

* Added turn-into toolbar: allow user to change block text style (#144)
* Clearing focus when user hides keyboard (#133)
* Added `PLUS` button on page screen (#133)
* Better UX: increased cursor/focusing speed (cursor is now moved to the next block with a greater speed) (#135)

### Fixes 🚒

* Fixed main toolbar visibility illegal states: no longer showing the main toolbar when no block is focused (#103)
* Wire the control panel with the focused block: `ControlPanelToolbar` holds the id of the focused block (#133)
* The main toolbar is hidden when no block is focused on a page (#103)
* Fixed a regressed issue: new paragraph is not focused when created after on-enter-press event is triggered (#138)
* Fixed Github Actions CI issue: using token from repository secrets (#148)
* Fixed issues related to incorrectly calculated adapter position resulting in app crash (#147)

### Middleware ⚙

* Refactored event handler (list of events is now processed at once, not one event after another as before) (#134)
* Added `blockSetTextStyle` command. 

## Version 0.0.15

### New features 🚀

* Enabled user to add `checkbox` and `bullet` blocks. (#106)
* Allow user to `delete` (unlink) or `duplicate` blocks inside a page (#107).
* Added block-action (delete, duplicate) toolbar (#107)
* Create a new paragraph on enter-press event at the end of the line (#129)
* Create an empty paragraph block when user clicks on empty space (#126)
* Delete target block when user presses backspace inside an empty block (#113)

### Fixes 🚒

* Every page is now opened in expanded state (#121)
* Should not show colour toolbar and add-block toolbar at the same time (#119)

### Middleware ⚙

* Added support for `duplicate` and `unlink` operations (#107)
* Middleware-client refactoring (#118)
