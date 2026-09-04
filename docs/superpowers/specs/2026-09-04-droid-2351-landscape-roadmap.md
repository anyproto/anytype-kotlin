# Landscape and tablet support — roadmap for seven tickets

## Context

DROID-4402 removed the portrait lock. Three commits are on branch
`droid-4402-landscape-tablet-support` (PR #3335): the unlock plus a single content column capped
at `@dimen/max_content_width`, review fixes, and a sweep of 26 `ModalBottomSheet` calls that
escaped the cap.

The unlock is not a feature on its own. It exposes defects that the lock had kept unreachable,
and the six remaining tickets are that fallout. This document orders them, states the approach
for each, and names the files.

Decisions taken with the author before writing this:

- **DROID-3304 is redundant.** The lock is removed for every build type and stays that way. The
  consequence: the other six tickets gate the release, not the nightly. Close 3304 with that note.
- **DROID-4402 stays single-column.** The 600dp centred column is the target. A two-pane tablet
  UX is a separate project.
- **State is saved explicitly**, not avoided with `android:configChanges`. The activity keeps
  being recreated, so resource qualifiers keep working.
- **DROID-3403 gets `layout-land` / `values-land`** resources rather than only unpicking fixed
  sizes.

## What is already true on the branch

- `app/src/main/AndroidManifest.xml` — no `screenOrientation` on any activity. Only
  `MediaActivity` declares `configChanges="orientation|screenSize"`.
- `core-utils/src/main/res/values/dimens.xml` + `values-w600dp/dimens.xml` — `max_content_width`
  is `10000dp` by default, `600dp` once the window is wider than 600dp.
- `core-ui/.../extensions/ContentWidthExtension.kt` — `View.contentWidth()`,
  `View.horizontalViewportWidth()`, `contentWidthDp()`.
- `core-utils/.../ext/BottomSheetContentWidth.kt` — `applyContentWidthCap()`.
- `CLAUDE.md:78-103` — the content-column rules, including the `ModalBottomSheet` trap.
- The repo has **no** `values-land`, `layout-land` or `values-sw600dp`. The only qualified width
  bucket is `values-w600dp`, added by this branch.

## Sequencing

```
Block A  DROID-4586 / 3404 / 3406   state restoration   ← do first, only one that loses data
Block B  DROID-3403 / 1713          Data View
Block C  DROID-2351                 QA + roadmap doc    ← needs A and B for real evidence
Block D  DROID-4402                 verify and close
         DROID-3304                 close as redundant
```

---

## Block A — state restoration (DROID-4586, DROID-3404, DROID-3406)

### The real root cause

Three findings, all verified in code:

1. **`Editor.Focus.id(id, isPending)` sets `cursor = null`** —
   `domain/src/main/java/com/anytypeio/anytype/domain/editor/Editor.kt:35`. Every focus change
   through `EditorViewModel.onBlockFocusChanged` (`:1845-1847`) therefore drops the caret offset.
   The offset lives separately in `Editor.Storage.textSelection`
   (`presentation/.../editor/Editor.kt:102`), and nothing joins the two back together.
2. **The re-render wipes focus.** `EditorFragment.onStart()` (`:605`) calls
   `EditorViewModel.onStart()` (`:1382-1468`) on every recreation → `openPage` → full re-render →
   `onStartFocusing` (`:1471-1515`), which sets focus **only when the object is empty**.
   `EditorFragment.onDestroyView` (`:1209-1217`) additionally calls `clearActiveTextSelections()`.
3. **The ViewModel probably survives, the DI component does not.** `by viewModels` retains
   `EditorViewModel` across a configuration change, but `BaseFragment.onDestroy` calls
   `releaseDependencies()` (`core-utils/.../ui/BaseFragment.kt:71-74`), which drops the
   `EditorSubComponent` and its `@PerScreen Editor.Storage`
   (`app/.../di/feature/EditorDI.kt:219-221`). The retained ViewModel keeps a reference to the
   **old** storage and the freshly built factory is never used. **Verify this empirically first.**
   The Bundle is needed in any case, because process death rebuilds the ViewModel too. The check
   decides how the rotation path is wired: if the stores survive, the rotation fix is only a
   re-issue after the re-render, and the Bundle path is exercised by process death alone.

### Reuse, do not invent

`Editor.Restore.Selection(target, range)` already exists
(`presentation/.../editor/Editor.kt:129-134`), is queued through `EditorViewModel.restore`
(`:432`), handed to the adapter in `EditorFragment:371`, and applied in
`BlockAdapter.onBindViewHolder(payloads)` (`core-ui/.../features/editor/BlockAdapter.kt:1471-1494`)
via `holder.content.post { setSelection(start, end) }`. That is the caret-restore mechanism; the
work is to feed it, not to build a new one.

`Editor.Mode.Styling.Single(target, cursor)` (`presentation/.../editor/Editor.kt:65-68`) is the
one place that already stashes and restores a caret offset — copy its shape.

### Approach

1. **Join focus and cursor at the source.** Keep the caret offset when focus changes, either by
   populating `Focus.cursor` in `onBlockFocusChanged` from the current
   `stores.textSelection`, or by reading both stores when composing the restore. Do this first —
   it fixes caret loss on ordinary focus changes, not only on rotation.
2. **Save across recreation.** Copy the only Bundle convention that exists in this repo:
   `EditorFragment.onSaveInstanceState:615-618` / `onViewStateRestored:620-625` with a plain
   `EditorViewModel.onRestoreSavedState(...)` setter (`:5720-5722`). Add a small `@Parcelize`
   holder carrying the focused block id and the selection range. `SavedStateHandle` is used
   **nowhere** in the repo — do not introduce it here.
3. **Re-issue after the re-render, not before.** The restore must be queued after
   `onStartFocusing`, or the re-render will overwrite it.

### DROID-3406 — typed text in relation creation

Two independent paths, both losing text for different reasons:

- **Compose path.** `feature-properties/.../edit/ui/NewPropertyScreen.kt:47` uses plain
  `remember`, not `rememberSaveable`. The repo already uses `rememberSaveable(stateSaver =
  TextFieldValue.Saver)` in 7 files; `core-ui/.../foundation/SearchBar.kt:156-162` is the
  reference, including its reconciliation guard. `EditTypePropertiesViewModel` holds the name in a
  plain `MutableStateFlow` (`:72-73`, updated at `:269-276`).
- **View path.** `RelationCreateFromScratchBaseFragment.kt:97` does `nameInputAdapter.query =
  query` in `onViewCreated`, resetting the visible field back to the launch argument
  (`QUERY_KEY`, `:52`, `:146`) and discarding anything typed since. The text itself lives in a
  plain adapter field, `RelationAddAdapter.RelationNameInputAdapter.query`
  (`core-ui/.../features/relations/RelationAddAdapter.kt:194`), while the ViewModel holds its own
  copy in `RelationCreateFromScratchBaseViewModel.name` (`:50`). On recreation the two diverge.
  Make the fragment read from the ViewModel rather than from the argument.

### Effort

The largest block. Step 1 is small and valuable on its own. Step 3 is where the risk sits: this is
the focus machinery that broke IME composition in DROID-4557, so change it in small commits and
test Hangul or another multi-character IME.

A second risk sits in finding 3 itself. After a rotation the retained ViewModel holds the old
`Editor.Storage`, while any child screen opened afterwards resolves through the **new**
`EditorSubComponent` and a new storage. Two storages for one editor session is a pre-existing
hazard that the portrait lock hid; it may surface as a separate defect while testing this block.

---

## Block B — Data View in landscape (DROID-3403, DROID-1713)

### DROID-3403 — rendering

The failures are fixed sizes. On a phone in landscape the window is about 360dp tall and the
chrome already takes 148dp, leaving roughly 212dp:

| File | Line | Value | Effect |
|---|---|---|---|
| `app/src/main/res/layout/fragment_object_set.xml` | 139 | `dataViewInfo` height **190dp** | Empty-state message (no items, no query, board without group-by; `ObjectSetFragment.kt:686,736,757`). Fits the remaining 212dp with about 20dp to spare, so any extra chrome or a soft keyboard clips it |
| same | 121 / 30 / 90 | toolbar 44dp, header 56dp, paginator 48dp | The 148dp of chrome |
| `core-ui/.../widgets/dv/ViewersWidget.kt` | 96 | `padding(bottom = 168.dp)` | Content pushed off a short sheet |
| `core-ui/.../widgets/dv/ViewerLayoutGroupByWidget.kt` | 85 | `padding(bottom = 250.dp)` | Same |
| `core-ui/.../widgets/dv/ViewerLayoutCoverWidget.kt` | 95 | `padding(bottom = 250.dp)` | Same |
| `core-ui/.../widgets/dv/ViewerEditMoreMenu.kt` | 40 | `.offset(x = offsetX - 220.dp, y = 60.dp)` | Menu placed by a fixed offset |
| `core-ui/.../widgets/dv/ViewerLayoutListMenu.kt` | 40 | `.offset(x = offsetX - 220.dp, y = 246.dp)` | Same |

Approach:

1. Move the four chrome heights and the `dataViewInfo` height into dimensions, then override them
   in `app/src/main/res/values-land/dimens.xml`. That is the smallest change and keeps one layout
   file. Add `layout-land/fragment_object_set.xml` only if the structure itself must differ, not
   for sizes alone. **`dataViewInfo` is declared three times** — `fragment_object_set.xml:139`
   plus `app/src/main/res/xml/fragment_object_set_scene.xml:97-101` and `:226-230`. All three must
   read the same dimension or MotionLayout will fight the layout.
2. The three sheet paddings are Compose, so a resource qualifier does not reach them. Replace the
   fixed bottom padding with `navigationBarsPadding()` plus content-driven height.
3. Anchor the two popup menus to their trigger instead of a fixed dp offset.

### Gallery span count — one value, currently two constants

`GalleryViewWidget.kt:417-418` hardcodes `SMALL_CARDS_COLUMN_COUNT = 2` and
`LARGE_CARDS_COLUMN_COUNT = 1`; the span count never follows the container width. An independent
second copy sits at `core-utils/.../ui/GalleryViewItemDecoration.kt:34` (`SPAN_COUNT = 2`) and
drives the `position % SPAN_COUNT` offset maths (`:18-28`).

Unify the two before making the count adaptive, or the decoration will compute wrong offsets in
silence. Also `core-ui/.../widgets/dv/ListViewItemRelationGroupWidget.kt:40` calls
`setHorizontalGap(15)` with a raw pixel value; it should be a dimension.

### DROID-1713 — horizontal and vertical scroll conflict

Current structure (`core-ui/src/main/res/layout/item_viewer_container.xml`):

```
HorizontalScrollView  @id/horizontalScrollView   ← owns horizontal panning
└─ RelativeLayout (wrap_content)
   └─ LinearLayout (wrap_content, vertical)
      ├─ RecyclerView @id/rvHeader   horizontal LLM, 42dp
      ├─ divider
      └─ RecyclerView @id/rvRows     vertical LLM  ← owns vertical scrolling
```

Each row also holds `@id/rowCellRecycler`, a **horizontal** `RecyclerView`
(`item_viewer_grid_row.xml:41`) nested inside the horizontally scrolling container. It is never
scrolled by the user: the container measures it with `UNSPECIFIED`, so it lays out every cell and
the outer view pans. There is no `requestDisallowInterceptTouchEvent` anywhere in the repo.

Two aggravating factors, both worth confirming on a device before touching code:

- The name column is pinned by writing `translationX` on every visible row on each scroll frame
  (`app/.../ui/sets/ObjectSetFragment.kt:605-616`; applied to incoming rows at
  `ViewerGridAdapter.kt:119-122`).
- The MotionLayout `OnSwipe` uses `gridContainer` as its drag anchor
  (`app/src/main/res/xml/fragment_object_set_scene.xml:10-13`), so a vertical drag on the grid is
  claimed by MotionLayout as well as by `rvRows`.

The ticket predates the current Data View implementation. **Reproduce first.** If it no longer
reproduces, close it with the evidence rather than refactoring.

### Found while mapping, worth a look during 1713

`gridContainer` is never hidden. `ObjectSetFragment.kt:908-1010` switches viewers by visibility,
but for Gallery, List and Board it only clears the grid adapters (`:925-926`, `:942-943`,
`:955-956`) and leaves the grid visible underneath — keeping a `HorizontalScrollView` in the touch
path of every other viewer.

### Effort

Medium. 3403 is mostly resource and padding work with a device check per viewer. 1713 is an
investigation first; its effort is unknown until it either reproduces or is closed.

---

## Block C — QA and roadmap document (DROID-2351)

DROID-2351 asks for two things and gets two documents. The roadmap is **this** file, saved as
`docs/superpowers/specs/2026-09-04-droid-2351-landscape-roadmap.md` now. The QA plan below is
written later, once Blocks A and B have produced evidence to record.

The repo has **no** QA or test-plan document. The closest artefacts are
`app/src/androidTest/resources/Scenario.md` (a flat checkbox list) and the `| Check | Evidence |`
table in `docs/superpowers/plans/2026-09-02-droid-4402-adaptive-portrait-unlock.md:141`. Documents
live in `docs/superpowers/specs/` and `docs/superpowers/plans/` as `YYYY-MM-DD-<ticket>-<slug>.md`.

Write `docs/superpowers/specs/<date>-droid-2351-landscape-qa-plan.md` containing:

- **Device matrix** — phone portrait, phone landscape, tablet portrait, tablet landscape,
  split-screen, desktop windowing. State the `w`/`sw` bucket each lands in; the column binds on
  `w600dp`, which a landscape phone reaches.
- **Screen matrix.** `app/src/main/res/navigation/graph.xml` holds 86 destinations, so an
  exhaustive list is not useful. Group by the areas the graph already uses — settings (12),
  primitives (10), editor (9), relations (8), sets (6), multiplayer (5), types (4), widgets,
  chats, spaces, history, auth — and name one representative screen per area.
- **Rotation checks** this work made reachable: caret and focus survive, typed text survives, the
  account resumes, no sheet stretches.
- **Reviewer rules**, by pointing at `CLAUDE.md:78-103` rather than repeating them.

Effort: small once the evidence exists; it is mostly a write-up of what Blocks A and B measured.

---

## Block D — close DROID-4402 and DROID-3304

DROID-4402 needs verification, not code:

1. Walk the app on a tablet in both orientations and on a phone in landscape.
2. `FieldListScreen` in `app/.../ui/primitives/ObjectFieldsFragment.kt` is the one screen changed
   by the column work that was never seen on a device.
3. Re-check the grid row header with more rows than fit on screen, so holders are created after
   the first layout pass. `core-ui/src/test/java/.../HorizontalViewportWidthTest.kt` covers the
   mechanism; the device check covers the integration.

DROID-3304 closes as redundant, with a note that the lock is gone for every build type.

Effort: small; a device session and two ticket updates.

---

## Verification

- `./gradlew :app:assembleDebug`
- `./gradlew :presentation:testDebugUnitTest :core-ui:testDebugUnitTest :core-utils:testDebugUnitTest :domain:test`
  — `domain` is a JVM module, so its task is `test`, not `testDebugUnitTest`.
- Device: a real tablet at `w1280dp` and a phone in landscape. Measure with
  `adb shell uiautomator dump`, not by eye. The column is 600dp, so a correct sheet or row
  measures 1200px at density 2.0.
- For Block A, the decisive caret test is: type text, rotate, then type one more character and see
  where it lands. Reading the caret from a screenshot is not proof.
- `./gradlew ... | tail` reports the exit code of `tail`. Redirect to a log file and check `$?`, or
  grep the log for `BUILD SUCCESSFUL`.
