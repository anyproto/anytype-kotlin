# Change log for Android @Anytype app.

## Version 0.0.17 (WIP)

### New features

* User can now use color toolbar to change the text color of the whole text block (#153)
* `Block.Content.Text` model now has optional `color` property (#153).
* User can add or remove links from text and open these links in a web browser (#112).
* Added documentation engine (`DOKKA`) for `domain` module: documentation is generated automatically from KDoc (#168).

### Fixes

* Fixed issue: user cannot undo markup formatting if there are already several markup of the same type in the text (#151)

### Middleware

* Added `blockSetTextColor` command (#153). 

## Version 0.0.16

### New features

* Added turn-into toolbar: allow user to change block text style (#144)
* Clearing focus when user hides keyboard (#133)
* Added `PLUS` button on page screen (#133)
* Better UX: increased cursor/focusing speed (cursor is now moved to the next block with a greater speed) (#135)

### Fixes

* Fixed main toolbar visibility illegal states: no longer showing the main toolbar when no block is focused (#103)
* Wire the control panel with the focused block: `ControlPanelToolbar` holds the id of the focused block (#133)
* The main toolbar is hidden when no block is focused on a page (#103)
* Fixed a regressed issue: new paragraph is not focused when created after on-enter-press event is triggered (#138)
* Fixed Github Actions CI issue: using token from repository secrets (#148)
* Fixed issues related to incorrectly calculated adapter position resulting in app crash (#147)

### Middleware

* Refactored event handler (list of events is now processed at once, not one event after another as before) (#134)
* Added `blockSetTextStyle` command. 

## Version 0.0.15

### New features

* Enabled user to add `checkbox` and `bullet` blocks. (#106)
* Allow user to `delete` (unlink) or `duplicate` blocks inside a page (#107).
* Added block-action (delete, duplicate) toolbar (#107)
* Create a new paragraph on enter-press event at the end of the line (#129)
* Create an empty paragraph block when user clicks on empty space (#126)
* Delete target block when user presses backspace inside an empty block (#113)

### Fixes

* Every page is now opened in expanded state (#121)
* Should not show colour toolbar and add-block toolbar at the same time (#119)

### Middleware

* Added support for `duplicate` and `unlink` operations (#107)
* Middleware-client refactoring (#118)
