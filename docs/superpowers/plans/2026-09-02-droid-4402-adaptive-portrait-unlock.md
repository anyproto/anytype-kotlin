# DROID-4402 — Adaptive layout: remove the portrait lock

Date: 2026-09-02
Branch: `droid-4402-landscape-tablet-support`

## Problem

Three activities declare `android:screenOrientation="portrait"`:

- `MainActivity` — hosts the whole app (`NavHostFragment` with 120 fragments plus a Compose overlay).
- `SettingsActivity` — a title plus `PreferenceFragment`.
- `QrScannerActivity` — a CameraX preview.

Android 16 (API 36) ignores these restrictions on windows whose smallest width is
600dp or more. The app opts out through the `<application>` property
`android.window.PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY`. Android 17 (API 37)
removes that opt-out. Google Play requires API 37 in August 2027.

## Goal

Remove the portrait lock. Keep the existing single-column layouts readable on a wide
window. Do not build a two-pane tablet UI in this step.

## Approach

Cap the content column at 600dp and center it. One layout change covers all 120
fragments. The qualifier is `w600dp`, not `sw600dp`, because `w` follows the current
window width. A phone in landscape has `sw360dp` but `w780dp`, and that case matters
most after the unlock.

## Changes

### 1. Manifest

Remove `android:screenOrientation="portrait"` from the three activities. Remove the
now dead `PROPERTY_COMPAT_ALLOW_RESTRICTED_RESIZABILITY` property.

### 2. One width resource

- `core-utils/res/values/dimens.xml` — `max_content_width` = `10000dp` (no cap).
- `core-utils/res/values-w600dp/dimens.xml` — `max_content_width` = `600dp`.

The same value drives the content column and the bottom sheet width. The dimension lives in
core-utils because the bottom sheet base classes do, and core-ui depends on core-utils.

`core-ui/extensions/ContentWidthExtension.kt` reads it through two helpers: `View.contentWidth()`
for the View code, and `contentWidthDp()` for the composables.

### 3. `activity_main.xml`

Replace the root `FrameLayout` with a `ConstraintLayout`. Give the
`FragmentContainerView` and the `composeOverlay` a width of `0dp` plus
`app:layout_constraintWidth_max="@dimen/max_content_width"`, and constrain both to the
parent edges. Paint the root with `@color/background_primary` as the backdrop.

### 4. View bottom sheets

Material Components 1.12.0 declares **no** `behavior_maxWidth` style attribute. The first build
failed on it: `AAPT: error: style attribute 'attr/behavior_maxWidth' not found`. The value goes
through `BottomSheetBehavior.setMaxWidth` instead.

`core-utils/ext/BottomSheetContentWidth.kt` holds `applyContentWidthCap()`. Three places call it:
`BaseBottomSheetFragment`, `BaseBottomSheetComposeFragment` and `WidgetOverlayFragment`.

Compose sheets need no change: Material3 1.3.2 caps `ModalBottomSheet` at
`BottomSheetDefaults.SheetMaxWidth` (640dp) by default.

### 5. Code that reads the screen width

The column breaks code that sizes itself from `displayMetrics.widthPixels`. Each site
must read its own container instead.

| File | Effect | Severity |
|---|---|---|
| `ViewerGridAdapter.kt:49` | Grid row header becomes wider than the column | Breaks |
| `RelationValueListWidget.kt:51` | Text ellipsizes too late | Visible |
| `BaseActionWidgetItemDecoration.kt:68` | Wrong item spacing | Visible |
| `Title.kt:667` | Sets a hard `width = screenWidth`, so the image overflows the column | Breaks |

Seven composables under `core-ui/features/fields/` carry the same defect through
`LocalConfiguration.current.screenWidthDp`. Their `halfScreenWidth` reaches 608dp inside a 600dp
column, so a field title never ellipsizes. They now call `contentWidthDp()`.

### 6. `activity_settings.xml`

`FragmentContainerView` uses `layout_height="wrap_content"`. The `RecyclerView` inside
`PreferenceFragment` then measures unbounded and loses its scroll. In landscape the
lower rows become unreachable. Use `0dp` plus `layout_weight="1"`.

The screen also takes the same capped column, so a row does not sit against the left edge of a
tablet.

### 7. `QrScannerActivity`

The camera stays full-bleed; the column does not apply. The close button uses a plain
`padding(16.dp)`. In landscape the cutout and the system bars move to the side and
cover it. Apply `safeDrawingPadding()`.

### 8. `MainViewModel.onRestore`

A rotation recreates `MainActivity`, so `onRestore` now runs while the user is still on the auth
flow. There is no account to resume there. `ResumeAccount` fails with `MnemonicEmptyException`,
and the destructive "Couldn't restore session" dialog appears on every turn. The defect exists
today, but the portrait lock kept it out of reach.

`onRestore` now skips the resume for a user it positively knows to be unauthorized. An error
during the check keeps the previous behaviour.

### 9. The wallpaper

`MainActivity.setWallpaper` painted `container`, which is `R.id.fragment` — now the capped
column. A tablet therefore showed the space wallpaper only inside 600dp, with a bare strip down
each side. The wallpaper moves to the root `rootContainer`, so it reaches the edges and the
column floats on it. `WallpaperResult.None` restores `@color/background_primary` instead of
clearing the background, which would expose the window behind the column.

## Open defect: the editor caret does not survive a rotation

`MainActivity` declares no `configChanges`, so a rotation recreates it and the editor fragment
with it. Measured on the tablet with a real account:

- The block text survives. The focus survives. The keyboard stays open. Nothing crashes.
- **The caret moves.** Typing `RotationPX45ABCDE` with the caret at the end, then rotating, left
  the caret at position 0. A second run left it at position 9. Typing one character after the
  rotation proved the caret really moved; it was not a drawing artifact.

`EditorFragment.onSaveInstanceState` saves only `CURRENT_MEDIA_UPLOAD_KEY`. Neither the focused
block nor the cursor is saved, because the editor never had to survive a recreation while open.

**Decision: accept it for now.** No data is lost, and only the caret position resets. The work is
tracked in DROID-148, which records the two repair routes:

1. **`configChanges` on `MainActivity`.** The activity stops being recreated, so the caret, the
   scroll position and all fragment state survive for free. The repo declares no orientation
   qualified resources apart from `max_content_width`, which makes this far safer here than
   usual. The one cost: `layout_constraintWidth_max` is resolved at inflation, so the column
   width must be re-applied in `onConfigurationChanged`.
2. **Save the focus and the cursor in `EditorFragment`.** Targeted, but it touches the editor
   focus machinery, which is delicate. See DROID-4557.

## Verified on a tablet emulator

The AVD reports 1280dp by 800dp at 320dpi, so the `w600dp` bucket applies in both orientations.

| Check | Evidence |
|---|---|
| `MainActivity` in landscape | Column stops at 600dp, centered |
| Rotation, no account | No logout dialog; log says the user is unauthorized |
| Rotation, real account | `Restored account after activity recreation`; space active |
| Set grid row header | `headerContainer` = 1136px = 568dp, that is 600dp minus 2 x 16dp. The old code gave 2496px |
| View bottom sheet | `design_bottom_sheet` = 1200px = exactly 600dp |
| Action row decoration | Seven items spread inside the column |
| Wallpaper | Fills the window edge to edge, survives a rotation cycle |
| `SettingsActivity` | Landscape, column applied |
| `QrScannerActivity` | Landscape, camera full-bleed, close button clear of the bars |

Not verified: `FieldListScreen` in `ObjectFieldsFragment`, which hosts the seven field
composables. The screen was not reachable from the test account in the time available. The
change there is `contentWidthDp()`, which returns `min(window width, 600dp)` by construction.

## Follow-up

- DROID-148 — the editor caret position is lost when a rotation recreates the activity.

## Out of scope

Two-pane list-detail navigation. Desktop windowing polish. Per-fragment landscape
designs.
