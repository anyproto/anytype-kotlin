# Change log for Android @Anytype app.

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
* Allow users to create new pages on home dashboard by pressing plus-button on page screen (#191)
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

### Middleware ⚙️

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

### Middleware ⚙️

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

### Middleware ⚙️

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
