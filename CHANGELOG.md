# Change log for Android @Anytype app.

## Version 0.0.16 (WIP)

### New features

* Clearing focus when user hides keyboard (#133)
* Added `PLUS` button on page screen (#133)

### Fixes

* `ControlPanelToolbar` holds now the id of the focused block (#133)
* The main toolbar is hidden when no block is focused on a page (#103)
* Regress: new paragraph is not focused when created after on-enter-press event is triggered (#138)
* Fixed Github Actions CI issue related with token.

### Middleware

* Refactored event handler (list of events is now processed at once, not one event after another as before) (#134)


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
