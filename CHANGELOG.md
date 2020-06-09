# Change log for Android @Anytype app.

## Version 0.0.35 (WIP)

### New features 🚀

*

### Design & UX 🔳

*

### Fixes & tech 🚒

* Setting cursor when pasting from anytype clipboard (#484)
* Should focus document's title when first paragraph (as the first block in the document) is deleted (#498)

### Middleware ⚙

*

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
