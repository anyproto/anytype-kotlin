# Quick Capture — Android Implementation Spec

**Status:** Implemented behind the `quickCapture` / `quickCaptureTypeSuggestions` experimental flags (2026-08-28); device-verification items in §13 still open · **Date:** 2026-08-28
**Source of product decisions:** `anytype-swift/quick-capture-handoff.md` (iOS v1, IOS-6617). This spec maps that platform-neutral contract onto the anytype-kotlin codebase and resolves the Android-specific questions (editor embedding, space activation, on-device AI runtime).

**Goal:** capture a thought into the right space in 2 taps — tap pencil → keyboard is up in the right space → type → send. Local AI suggests the object type. Capture must never block on AI.

---

## 0. Key Android platform decisions (read first)

These are the three decisions that shape everything else; the rest of the spec assumes them.

### D1. Sheet = `BottomSheetDialogFragment` hosting a real `EditorFragment` subclass

The editor is Views-based (`app/.../ui/editor/EditorFragment.kt:223`, 9600-line `EditorViewModel`), so the sheet cannot be a pure Compose `ModalBottomSheet`. The codebase already embeds the full editor in a bottom sheet **twice**:

- `app/.../ui/editor/EditorModalFragment.kt:15` — `BaseBottomSheetFragment` + child `EditorTemplateFragment` in a `FragmentContainerView`, full-height, own nav graph `nav_editor_modal.xml`.
- `app/.../ui/templates/TemplateSelectFragment.kt:29` — multiple editors in a `ViewPager2` inside a sheet, with sheet-owned chrome calling into the child editor.

Quick Capture copies this shape: a new `QuickCaptureFragment : BaseBottomSheetFragment` hosting a new `EditorQuickCaptureFragment : EditorFragment`, with a Compose header row above the fragment container, wired as a `<dialog>` destination in a new `nav_quick_capture.xml` graph (included from `graph.xml`, like `nav_editor_modal.xml` at `graph.xml:93`).

`EditorTemplateFragment` (`app/.../ui/templates/EditorTemplateFragment.kt`) is the catalog of exactly the overrides we need: `navigationDestinationId` (`:34`), `saveAsLastOpened() = false` (`:55`), `binding.topToolbar.hide()` (`:96`), suppressing FABs via `render(state)` override (`:64`). **One critical difference:** the template modal no-ops `setupWindowInsetAnimation()` (`:59-60`), disabling the IME-follow behavior of the bottom toolbars. Quick Capture *needs* the type bar to ride the keyboard, so the inset animation (`EditorFragment.kt:1094`, `syncTranslationWithImeVisibility` in `core-utils/.../ext/WindowInsetExt.kt:18`) must be adapted to work inside the dialog window rather than disabled. → **Verification item V1.**

### D2. Activate the target space (cheaply) — Android's version of the iOS "don't run full space activation" lesson

iOS learned not to run full space activation because it navigated the app underneath the sheet. On Android, **activation and navigation are decoupled**: `spaceManager.set(space)` (`domain/.../workspace/WorkspaceManager.kt:67`) only calls `workspaceOpen` and caches the `Config`; navigation is a separate, explicit command (`VaultViewModel.handleSpaceSelection`, `feature-vault/.../VaultViewModel.kt:825-845` calls them separately). So the translated rule is:

> Call `spaceManager.set(space)` when the sheet opens / the chip switches, but do **not** emit any navigation command and do **not** call `SaveCurrentSpace`.

We *want* activation because the editor render pipeline depends on app-singleton, active-space-scoped stores: `StoreOfObjectTypes` / `StoreOfRelations` (`di/main/SubscriptionsModule.kt:115-127`) are filled by `ObjectTypesSubscriptionManager` / `RelationsSubscriptionManager`, which `clear()` + re-subscribe off `spaceManager.state()` (`domain/.../search/ObjectTypesSubscriptionManager.kt:28-48`). Opening an editor for a non-active space renders with the wrong space's types/relations — silently. The sync badge has the same coupling (`presentation/.../sync/SpaceSyncAndP2PStatusProviderImpl.kt:21-27`). Activating the target space makes the editor, the type bar, and the sync dot all correct with zero new infrastructure.

Cleanup: the vault normally has no active space (`ExitToVaultDelegate` = `spaceManager.clear()` + `clearLastOpenedSpace`). On sheet dismissal (send or swipe), invoke the same exit-delegate semantics to return the vault to its normal state. The banner's "View" action re-activates via `DeepLinkToObjectDelegate` anyway (see §8).

A proper cross-space types subscription (per-space stores) remains the right *future* foundation — same note as the iOS handoff — but is out of scope for v1.

Store repopulation after `set()` is asynchronous; the sheet should await the first store emission for the target space before showing the type bar content (the editor itself tolerates late types by re-rendering). → **Verification item V2** (no visible flash of wrong-space types).

### D3. On-device AI = ML Kit GenAI Prompt API (Gemini Nano via AICore), prompt-side constraint + strict app-side validation

Researched against mid-2026 state (details in §9): `com.google.mlkit:genai-prompt:1.0.0-beta4` is the production path Google is steering to (no allowlist, model lives in AICore → zero APK cost, first-class availability/download/warmup APIs). Its Structured Output API only takes **compile-time** schemas (`@Generable`/`@Guide(enumValues=[...])` — annotation constants), so it cannot express our runtime list of space type names. Therefore: enumerate the type names in the prompt, decode with `temperature=0 / topK=1 / maxOutputTokens≈16`, and enforce the "must be from the list" contract by exact-match validation in app code — functionally equivalent to iOS's constrained decoding, with enforcement moved app-side. Device coverage is flagship-2024+ only (low single-digit % of the fleet), which the handoff already anticipates: the non-AI ordering is the primary experience.

---

## 1. Feature flags

Follow the Kanban / compact-mode "experimental feature" pattern (SharedPreferences-backed, observable, user-flippable):

| Flag | Key | Gates |
|---|---|---|
| `quickCapture` | `prefs.device.quick_capture_enabled` | pencil entry point + everything below |
| `quickCaptureTypeSuggestions` | `prefs.device.quick_capture_ai_enabled` | §9 only; independent of the main flag |

Plumbing per flag (5 files, copy `getKanbanEnabled` end-to-end): `persistence/.../repo/DefaultUserSettingsCache.kt` (key at `:971` area, `MutableStateFlow` mirror like `:61-63`) → `data/.../auth/repo/UserSettingsCache.kt` → `data/.../auth/repo/UserSettingsDataRepository.kt` → `domain/.../config/UserSettingsRepository.kt` (`get/set/observe` triple like `:83-85`) → toggle rows in `app/.../ui/settings/ExperimentalFeaturesScreen.kt:32` + `presentation/.../settings/ExperimentalFeaturesViewModel.kt`. (Experimental Features entry is `BuildConfig.DEBUG`-gated today via `ProfileSettingsViewModel.kt:74` — fine for dogfooding; release enablement is just flipping the default.)

## 2. Entry point (vault)

The vault (`feature-vault/.../ui/VaultScreen.kt:42`, Compose, hosted by `app/.../ui/vault/VaultFragment.kt:55`) has **no bottom bar** — its `Scaffold` has only a `topBar` (`VaultScreen.kt:77`), and the create-space "+" is *already* in the top toolbar next to the search/profile area (`VaultScreenToolbar.kt:171-193`). So Android is already at the iOS *end state* for the "+" (top bar); what's missing is the pencil in a primary position:

- Add a **FAB** to the vault `Scaffold` (`floatingActionButton` slot), pencil/compose glyph (new asset, e.g. `ic_vault_quick_capture.xml` — no pencil icon exists in `core-ui` today), inset above `navigationBarsPadding`. This is the Android-platform equivalent of "primary spot in the bottom bar". *(Flag for design review: FAB position center vs. end; the handoff's bottom-bar layout doesn't map 1:1.)*
- Visible only when `quickCapture` flag is on **and** at least one editable space exists (see §4 — the handoff's "if no editable space, the sheet closes" becomes "the pencil doesn't show").
- Tap → `VaultViewModel.onQuickCaptureClicked()` → `VaultCommand.OpenQuickCapture` → `VaultFragment` navigates to the `quickCaptureScreen` dialog destination.
- The existing "+" and its dropdown (`CreateChannelDropdownMenu`) are untouched.

## 3. Capture sheet composition

New screen package `app/.../ui/quickcapture/` + `presentation/.../quickcapture/QuickCaptureViewModel` (host VM) + DI component via `ComponentManager` (copy `VaultDI.kt` shape, `@PerScreen`).

Layout `fragment_quick_capture.xml`: drag indicator, `ComposeView` (header row), `FragmentContainerView` (editor child). Sheet behavior: `skipCollapsed()` + `setFullHeightSheet()` + `expand()` (as `EditorModalFragment` does); swipe-down dismisses with **no confirmation** (draft is preserved, §5).

### Header row (Compose, sheet-owned; editor is headerless)

```
[drag indicator]
[ 🅆 SpaceName ▾ ]        [sync ●] [⋯] [🗑] [ ↑ ]
```

- **Space chip**: `SpaceIconView` (`core-ui`) + name + chevron → opens the space picker (§4).
- **Sync dot**: reuse `StatusBadgeWidget` semantics in Compose or embed the existing widget; tap → `SpaceSyncStatusScreen` (`core-ui/.../syncstatus/SpaceSyncStatusScreen.kt:52`, already a Compose `ModalBottomSheet`). Correct space guaranteed by D2. The host VM collects `SpaceSyncAndP2PStatusProvider` exactly as `EditorViewModel.proceedWithCollectingSyncStatus()` (`EditorViewModel.kt:9317`) does.
- **⋯**: calls into the child editor's menu path with the quick-capture restriction (§7) — precedent for sheet-chrome→child-editor calls: `TemplateSelectFragment.kt:52-58`.
- **🗑 Clear draft**: visible only when the draft is non-empty (§5 emptiness signal); tap → destructive confirmation → `DeleteObjects` + clear pointer + fresh draft.
- **↑ Send**: accent-filled circle, disabled while empty (§5); action in §8.

The child `EditorQuickCaptureFragment` hides the editor's own top chrome (`binding.topToolbar.hide()` + recycler top inset, per `EditorTemplateFragment.kt:94-96`) and suppresses the bottom nav FABs via the `render(state)` override. Content scrolls under the sheet header; inset the recycler below it.

### Keyboard

On open with an empty draft, focus the title block and show the IME immediately (the "2 taps → typing" promise). Restored non-empty drafts: focus end of the document. → **Verification item V3** (IME appears reliably inside the dialog window on open).

### In-editor navigation (mentions / links)

Don't push inside the sheet. `EditorQuickCaptureFragment` overrides the open-object navigation paths to: dismiss the sheet (draft persists) → route through the activity-level deep-link path (`MainViewModel.proceedWithOsWidgetObjectOpen`-style, `presentation/.../main/MainViewModel.kt:797`, backed by `DeepLinkToObjectDelegate`).

## 4. Space selection

### Candidate list & ordering (host VM)

Copy the sharing extension's writable-spaces recipe (`presentation/.../sharing/SharingViewModel.kt:114-135`): `combine(spaceViewSubscriptionContainer.observe(), userPermissionProvider.all())`, keep spaces where permissions `isOwnerOrEditor()` (`core-models/.../Multiplayer.kt:53`). Then:

1. Non-1:1 before 1:1 (`ObjectWrapper.SpaceView.isOneToOneSpace`, `ObjectWrapper.kt:440-446`); 1:1 spaces are never auto-selected, always sort last, remain manually selectable.
2. **Pinned before unpinned, then the user's manual pin order** (`spaceOrder` lexid asc) — deliberately matching the vault (`VaultViewModel.kt:524-543`) rather than the handoff's recency-first ordering, so the picker never contradicts the space list the user already knows.
3. Among unpinned (all tied above): device-local capture recency desc (new store, below) — the vault uses chat-message recency in this slot.
4. Final tiebreak: join date desc, then created date desc.

⚠️ Found during research: `Relations.SPACE_JOIN_DATE` is **not** in the space-view subscription keys (`domain/.../multiplayer/SpaceViewSubscriptionContainer.kt:140-166`), so `spaceJoinDate` is always null today (the vault comparator silently falls back to created date). Add the key — it fixes a latent vault-ordering bug too.

### Device-local recency store

Middleware has no per-space last-opened signal, so the client keeps `[spaceId → lastInteractionDate]`:

- Storage: `SpacePreference` proto (`persistence/src/main/proto/preferences.proto`) — add `optional int64 lastInteractionTimestamp = 15;` (next free tag is 14, taken by the draft pointer in §5; tag 4 is reserved).
- Write sites: (a) space activation from the vault — `VaultViewModel.handleSpaceSelection` (`VaultViewModel.kt:825`); (b) chat opens from vault; (c) every quick-capture send. New use case `SetSpaceLastInteraction` + bulk read `GetSpaceQuickCaptureState` (read the whole `preferences` map in one pass, idiom at `DefaultUserSettingsCache.kt:227-243`).
- Auto-selection, in priority order (`QuickCaptureViewModel.resolveInitialSpace`):
  1. **The last space captured into, when its draft is still pending** — a second device-local pointer (`prefs.device.quick_capture_last_space`, written on every chip selection) plus a live-draft check via `FetchObject` (`Object.Search` by id only, so the hidden draft is visible). Reopening quick capture returns the user to their unfinished thought, wherever it lives — 1:1 spaces included, since that was the user's own explicit choice rather than an auto-selection. The validated draft id is passed straight through, so the space activation path does not re-query it.
  2. Otherwise the first editable **non-1:1** space by the ordering above.
- No editable space at all ⇒ pencil hidden (§2).

### Space picker sheet

Compose `ModalBottomSheet` launched from the chip (half-height initial via `rememberModalBottomSheetState(skipPartiallyExpanded = false)`, expandable): rows of 48dp `SpaceIconView` + name, checkmark on the current space, plain (not auto-focused) text filter on names, ordering as above. Use the **elevated** surface (`background_secondary`), not `background_primary` — sheet-over-dimmed-black contrast (iOS hit this; same palette risk here). `SelectSpaceScreen.kt:101` (sharing) is the closest existing composable, but it's a 3-column grid — QC needs the list form; build a small new composable in `core-ui`.

### Switching space with typed content — "text follows the chip"

Retarget the current thought, don't swap drafts:

- Empty current draft → just open/create the target space's draft (per §5), tear down and recreate the child editor fragment with the new `ctx`/`space` args (the editor DI component is keyed by ctx — `ComponentManager.kt:260` — so distinct drafts get distinct components).
- Non-empty → new domain use case `MoveQuickCaptureDraft(from, to)`:
  1. `repo.copy(Command.Copy(context = oldDraft, range = null, blocks = allBlocks))` — use the repository directly, **not** the `Copy` use case (`domain/.../clipboard/Copy.kt:9`), which also writes the user's system clipboard via `AnytypeClipboardStorage`; clobbering the OS clipboard as a side effect of switching a chip is unacceptable. `Command.Copy`/`Command.Paste` carry no space id (`Command.kt:427/:410`) and the middleware resolves by `contextId`, so cross-space copy→paste is a legal sequence.
  2. Create the target draft (§5 create path; same type where it exists in the target space — resolve by `uniqueKey` with a space-scoped search; else target-space default type). Replace (delete) the target space's previously saved draft, if any.
  3. Add a first empty text block (precedent for block-creation without opening the object: `CreatePrefilledNote.kt:17`), then `repo.paste(...)` with the copy response's block slot into that block.
  4. Delete the old draft, update both space pointers.
- ⚠️ File/image blocks are space-scoped; whether `Block.Paste` re-resolves them cross-space is middleware behavior → **Verification item V4** (same open caveat as iOS).

## 5. Draft mechanics (the core contract)

One draft per space, device-local pointer, real middleware object.

- **Registry**: `SpacePreference` proto — add `optional string quickCaptureDraftObjectId = 14;`. Use cases `SetQuickCaptureDraft` / `ClearQuickCaptureDraft` / `GetQuickCaptureDraft` (write idiom: `DefaultUserSettingsCache.setLastOpenedObject`, `:366-414`).
- **Create**: `CreateObject.Param` (`domain/.../page/CreateObject.kt:20`) with:
  - `space = target`, `type = null` — `CreateObject` internally resolves the per-space default type **and its default template** via `GetDefaultObjectType` (`domain/.../launch/GetDefaultObjectType.kt:23`, space-parameterized, ultimate fallback Page). This matches the handoff for free.
  - `internalFlags = [ShouldEmptyDelete, ShouldSelectType]` and **not** `ShouldSelectTemplate` (no template picker inside the sheet). Note: don't route through `getCreateObjectParams(...)` (`presentation/.../objects/ObjectTypeExtensions.kt:107`) — it adds `ShouldSelectTemplate` unconditionally.
  - `prefilled = mapOf(Relations.IS_HIDDEN to true)` — hide the draft **atomically at create** rather than iOS's create-then-set (avoids any search/recents flash). → **Verification item V5** that `Object.Create` accepts `isHidden` in details; fallback = immediate `SetObjectDetails` after create, matching iOS.
- **Hidden until published**: `isHidden` keeps the draft out of search/recents/widgets. Interim until heart ships a real unsynced-draft state (tracked GO-side). Note: no client code writes `isHidden` today (all existing usages are search filters) — this is the first write path.
- **Restore** on sheet open: pointer → validate via **id-only** fetch — `FetchObject` (`domain/.../object/FetchObject.kt:15`) is safe (pure `ID EQUAL` filter, no default filters) but pass explicit `keys` incl. `INTERNAL_FLAGS`, `IS_DELETED`, `IS_ARCHIVED`. ⚠️ **Do not** use `ObjectSearchConstants.filterObjectsByIds` (`ObjectSearchConstants.kt:1290`) — despite its name it appends `IS_HIDDEN != true` (+archived/deleted/hiddenDiscovery) and will *never find the draft*. This exact trap is why the handoff warns about default search filters. Valid & alive → reuse; else clear pointer + create fresh.
- **Close/swipe keeps the draft** — no confirmation. Abandoned *empty* drafts self-delete heart-side on object close (`ShouldEmptyDelete`); the stale pointer is handled by restore validation.
- **Emptiness signal** (Send enable / trash visibility): the host VM opens an id-subscription on the draft — `StorelessSubscriptionContainer.subscribe(StoreSearchByIdsParams(space, "quick-capture-draft", keys = [ID, INTERNAL_FLAGS, NAME, TYPE, LAYOUT, IS_DELETED], targets = [draftId]))` (`domain/.../library/StorelessSubscriptionContainer.kt:35`, `StoreSearchParams.kt:21`). Ids-subscriptions apply **no filters**, so the hidden draft is returned. Empty ⇔ `internalFlags` still contains `ShouldEmptyDelete`; heart clears it on first real content. This deliberately bypasses the editor's own pipeline, where `Flags.skipRefreshKeys` includes `INTERNAL_FLAGS` (`presentation/.../editor/DocumentExternalEventReducer.kt:168-177`) and would delay reacting to flag-only changes. Unsubscribe on sheet close.
- **Clear draft** (🗑): confirmation → `DeleteObjects.Params([draftId])` → clear pointer → create fresh draft in place.

## 6. Type selector bar (quick-capture mode)

The existing widget: `ChooseTypeHorizontalWidget` (`core-ui/.../widgets/toolbar/ObjectTypesComposeWidget.kt:89`), driven by `EditorViewModel.typesWidgetState` (`EditorViewModel.kt:8014`), shown when `ShouldSelectType` is set and permitted (`:9263-9285`), list built in `proceedWithGettingObjectTypesForTypesWidget()` (`:8041`) from `storeOfObjectTypes` (correct space thanks to D2).

Introduce an editor mode — extend `EditorViewModel.Params(ctx, space)` (`EditorViewModel.kt:9596`) with `usecase: EditorUsecase = DEFAULT | QUICK_CAPTURE`, plumbed from fragment args through `DefaultComponentParam` (`di/feature/ObjectSetDI.kt:807`). Behavior deltas in QUICK_CAPTURE:

| Behavior | Today | Quick Capture |
|---|---|---|
| Initial state | collapsed behind "Show types" (`setTypesWidgetVisibility` forces `expanded = false`, `:8018`) | **expanded by default** (keep the Hide/Show toggle) |
| "Done" button | present (`TypesWidgetItem.Done`) | **removed** (sheet has its own send/close) |
| Sets/collections | **included** (`SupportedLayouts.editorCreateObjectLayouts` contains SET & COLLECTION; `includeListTypes = true`) | **excluded** — containers aren't capture targets |
| Sorting | `sortByTypePriority` (orderId → hardcoded priority → name, `ObjectTypeSortingExtensions.kt:54`) | **`lastUsedDate` desc**, ties → existing priority order |
| Search chip / paste chip | works (already excludes SET/COLLECTION/CHAT, `:8030`) | unchanged |
| AI suggestion | — | suggested chip pinned to front with ✨ mark (§9) |

Tap-to-apply is unchanged: `SetObjectType` + apply the type's default template (`proceedWithObjectTypeChangeAndApplyTemplate`, `EditorViewModel.kt:8143`).

`lastUsedDate` notes: heart bumps it on `Object.Create`; the client never writes it (verified — only reads/sorts, e.g. `defaultObjectTypeSearchSorts()` at `ObjectSearchConstants.kt:1066`). Two checks: **V6a** — `LAST_USED_DATE` must be in the type-subscription keys feeding `StoreOfObjectTypes` (it's in `defaultKeysObjectType`, but verify the `ObjectTypesSubscriptionContainer` param set); **V6b** — whether heart also bumps it on `Object.SetObjectType` (the chip-tap path). If not, sorting still converges via creates, but file a heart follow-up rather than client-writing a system relation.

## 7. Trimmed object menu (⋯)

Menu infra: `ObjectMenuBaseFragment` (`app/.../ui/editor/sheets/ObjectMenuBaseFragment.kt:49`) with two item groups — vertical options via `ObjectMenuOptionsProvider.Options` (`presentation/.../menu/ObjectMenuOptionsProvider.kt:8`, computed purely from layout/lock state — **no per-caller hook exists today**) and the horizontal `ObjectAction` row built in `ObjectMenuViewModel.buildActions` (`presentation/.../objects/menu/ObjectMenuViewModel.kt:140-243`).

Add an `isQuickCapture` argument to `ObjectMenuFragment.args(...)`, flowing into the VM:

- **Keep**: `optionRelations` (Properties), `optionIcon`, `optionCover`, `optionDescription` (Show/Hide description); horizontal actions = `UNDO_REDO` only.
- **Hide**: `publishToWeb`, favorite, `COPY_LINK`, `DUPLICATE`, `LINK_TO`, `LOCK`, `USE_AS_TEMPLATE`, `optionHistory`, `MOVE_TO_BIN`, diagnostics, the rest.

Rationale (from the handoff): the menu must not offer actions that publish/expose an unpublished draft; full options return once published. Mechanically: an `Options` preset (e.g. `Options.QUICK_CAPTURE`) + a `buildActions` branch — same idiom as the existing `ObjectSetMenuViewModel` divergence. The QC nav graph must re-declare `objectMenuScreen`, `objectIconPickerScreen`, `objectCoverScreen`, `objectRelationListScreen` (exactly what `nav_editor_modal.xml` already does) so the editor's `safeNavigate` calls resolve inside the sheet.

Undo/Redo rides the editor's existing `undoRedoToolbar` (`fragment_editor.xml:314`, VM entry `EditorViewModel.kt:8318`). → **Verification item V7**: the `BottomSheetBehavior`-based editor toolbars behave inside a `BottomSheetDialog` (the template modal precedent suggests yes; smoke-test undo/redo + style toolbars).

## 8. Send ("publish") flow

On ↑ (enabled only when non-empty):

1. `SetObjectDetails(ctx = draftId, details = mapOf(Relations.IS_HIDDEN to false))` — this *is* publishing. Failure → error toast, draft untouched.
2. Clear the draft pointer for that space.
3. Write space recency (§4); send analytics (§10).
4. Success haptic — `ViewCompat.performHapticFeedback(view, HapticFeedbackConstantsCompat.CONFIRM)`. (The existing helper `core-ui/.../common/HapticExt.kt` is reorder-specific; add a small sibling, don't overload `ReorderHapticFeedbackType`.)
5. Dismiss the sheet (then vault-state cleanup per D2); user lands on the vault.
6. **Vault banner**, Linear-style: wide card floating above the bottom edge (small margin, above the FAB), leading green check, text **"{TypeName} created in {SpaceName}"**, trailing **"View"**; whole card tappable; auto-hide ~6s, animated in/out. Implementation: new state in `VaultViewModel` + a new Compose banner composable in `feature-vault` (there is no shared Compose banner component; the closest precedent is the Activity-level `Command.SnackbarWithOpenType` used for share-extension results, `MainViewModel.kt:1139` — but the handoff's card design warrants a proper composable, which can graduate to `core-ui` later). Type/space names come from the §5 draft subscription + space-view store.
7. Tap → open the created object cross-space: `DeepLinkToObjectDelegate.onDeepLinkToObject(obj, space, switchSpaceIfObjectFound = true)` (`domain/.../workspace/DeepLinkToObjectDelegate.kt:16`, already injected into `VaultViewModel`) + the vault's existing back-stack seeding pattern (`VaultFragment.proceed`, `VaultFragment.kt:383-514`: navigate to space home first unless direct, then `navigation().openDocument/openObjectSet` by layout).
8. Next pencil tap starts a fresh draft (pointer cleared).

## 9. On-device AI type suggestion

### Runtime choice (researched 2026-08)

**Primary: ML Kit GenAI Prompt API** — `com.google.mlkit:genai-prompt:1.0.0-beta4` (beta since 2026-01, structured output + system instructions since beta3, Gemini Nano 4 fixes in beta4). Model = Gemini Nano in the AICore system service: **zero APK size cost**, shared across apps, fully offline inference, min API 26 but gated by device support. No allowlist; ML Kit GenAI Additional ToS apply. The old `com.google.ai.edge.aicore` experimental SDK is superseded — don't build on it.

Constraints that shape the design:

- **Structured Output can't take a runtime enum** (compile-time `@Generable` schemas only) → prompt-side enumeration + strict app-side validation (D3).
- **Safety filters are non-adjustable** and applied to input *and* output; benign-text refusals must be treated as "no suggestion" (the handoff flags the same on iOS).
- **Foreground-only** inference (`BACKGROUND_USE_BLOCKED`) and per-app quotas (`BUSY`) — both are just more silent-fallback cases.
- **Language support beyond English is undocumented** — expect degraded quality on non-English notes; silence is the failure mode either way.
- Device coverage: Gemini Nano ships on 2024+ flagships (Pixel 8/9/10/11, Galaxy S24+/S25/S26/Z, and a set of Honor/Motorola/OnePlus/OPPO/realme/vivo/Xiaomi flagships). Realistically low single-digit % of the Android fleet → **the lastUsed-sorted bar is the primary experience; AI is a progressive enhancement.**

**Fallback design (only if Nano's instruction-following proves too weak in evals):** LiteRT-LM with a downloaded Gemma 3 1B (~529 MB, post-install delivery) has true token-level constrained decoding (LLGuidance: regex/JSON-schema/grammar). Real cost in size/RAM — not v1; recorded here so the interface below stays runtime-agnostic.

### Architecture

- New Gradle module **`core-ai`** (Android library) — isolates the ML Kit dependency from every other module. Contains `MlKitTypeSuggestionEngine`.
- Interface in `domain`: 

```kotlin
interface TypeSuggestionEngine {
    suspend fun isAvailable(): Boolean          // flag && runtime check
    suspend fun prewarm()                        // no-op if unavailable
    suspend fun suggest(text: String, typeNames: List<String>): String?  // null = silence
}
```

- Bound in app DI; a no-op implementation when the `quickCaptureTypeSuggestions` flag is off, so `presentation` never touches ML Kit types.

### Behavior (the platform-neutral contract, mapped)

- **Input**: draft title + body plain text, capped at 500 chars, min 3 chars after trimming (low on purpose — "Buy milk" must classify); the type names currently shown in the bar (§6 list).
- **Trigger**: in `EditorViewModel` (QC mode), observe document changes, `debounce(1000)`, skip if the extracted text is unchanged since the last classification.
- **Engine call** (inside `core-ai`):

```kotlin
val model = Generation.getClient()

suspend fun isAvailable() = runCatching { model.checkStatus() == FeatureStatus.AVAILABLE }.getOrDefault(false)
// On DOWNLOADABLE: optionally start model.download() on unmetered Wi-Fi so future
// sessions work; report unavailable for this session either way.

suspend fun prewarm() { runCatching { model.warmup() } }   // Nano cold start ≈1-2s; call on sheet open

suspend fun suggest(text: String, typeNames: List<String>): String? =
    withTimeoutOrNull(2_500) {
        runCatching {
            val raw = model.generateContent(
                generateContentRequest(TextPart(
                    """
                    Classify the note into exactly one category.
                    Categories: ${typeNames.joinToString(" | ")}
                    Note: "${text.take(500)}"
                    Reply with only the category name, nothing else.
                    """.trimIndent()
                )) { temperature = 0.0f; topK = 1; candidateCount = 1; maxOutputTokens = 16 }
            ).candidates.firstOrNull()?.text?.trim()
            // strict validation replaces constrained decoding:
            typeNames.firstOrNull { it.equals(raw, ignoreCase = true) }
        }.getOrNull()                            // safety refusal / BUSY / anything → null
    }
```

  *(Exact response-accessor naming to be confirmed against the beta4 reference → **V8**.)*
- **UX — suggest-only**: on a non-null result, pin that type's chip to the front of the bar with a small ✨ mark (`TypesWidgetState` gains `suggestedTypeId`; `ObjectTypesComposeWidget` renders the badge). The user taps to apply. **Send never auto-applies.**
- **Failure = silence**, always: unavailable device, model downloading, safety refusal, unsupported language, timeout, quota. The user never sees an AI error; the bar just stays lastUsed-sorted. **Log every fallback reason at debug level** (Timber) — "why no suggestion?" is otherwise undebuggable on device.
- **Warm-up**: call `prewarm()` when the sheet opens (fire-and-forget). Since our prompt preamble + type list is stable per space, AICore's prefix caching helps repeat classifications within a session.
- **Gating**: `quickCaptureTypeSuggestions` flag AND `isAvailable()`. Nothing about the non-AI experience may depend on the AI path.

## 10. Analytics, localization, misc

- **Analytics**: add `EventsDictionary.Routes.quickCapture = "QuickCapture"` (`analytics/.../EventsDictionary.kt:443`). Fire the existing `objectCreate` event via `sendAnalyticsObjectCreateEvent` (`presentation/.../extension/AnalyticsExt.kt:1010`, the `ObjectWrapper.Type` overload) **at send/commit, not at draft creation** (drafts would inflate numbers). Suggestion-shown/accepted events: follow-up, naming needs product (same as iOS).
- **Localization** (`localization` module): `quick_capture_created_in` = "%1$s created in %2$s"; reuse existing "View"; `quick_capture_clear_draft` = "Clear draft" (+ confirmation strings).
- **Cross-device draft sync**: out of scope; arrives free when heart's unsynced-draft state lands (client flow unchanged).
- **Also out of scope for v1** (per handoff §11): AI property-fill/restructuring (blocked on heart's anyblockjson surface), speech input, replacing the in-space "+", OS-level entry points (note: an app-shortcut/OS-widget entry would be cheap on Android via the existing `DeepLinkToCreateObject` plumbing in `Deeplinks.kt:173` — explicitly deferred, not forgotten).

## 11. New/changed surface summary

| Area | Change |
|---|---|
| `persistence` | proto: `SpacePreference.quickCaptureDraftObjectId = 14`, `lastInteractionTimestamp = 15`; `quick_capture_last_space` pref; cache methods; 2 feature-flag prefs |
| `domain` | use cases: draft registry get/set/clear, recency set/bulk-get, `MoveQuickCaptureDraft`; `TypeSuggestionEngine` interface |
| `data` | `UserSettingsCache`/`UserSettingsDataRepository` pass-throughs |
| `core-ai` (new) | `MlKitTypeSuggestionEngine` (ML Kit GenAI Prompt beta4) |
| `presentation` | `QuickCaptureViewModel`; `EditorViewModel` QC mode (type bar deltas, AI trigger, `Params.usecase`); `ObjectMenu*` QC restriction; analytics route |
| `feature-vault` | pencil FAB, banner composable + state, recency write on space open |
| `core-ui` | pencil icon asset, space-picker list composable, ✨ badge on type chip, header-row components |
| `app` | `QuickCaptureFragment`, `EditorQuickCaptureFragment`, `nav_quick_capture.xml`, DI components, `ObjectMenuFragment` arg |
| `analytics` | `Routes.quickCapture` |
| `localization` | 3 strings |

## 12. Phasing (each ships behind the flag)

1. **Foundations (S)** — proto + settings plumbing, both flags, recency writes on space activation, `SPACE_JOIN_DATE` subscription-key fix.
2. **Sheet + draft mechanics (L)** — pencil FAB, sheet with embedded headerless editor, D2 activation dance, create/hidden/restore/clear/emptiness, send + banner, trimmed menu, analytics. Single preselected space (chip static).
3. **Space chip + picker + cross-space move (M)** — §4 complete, `MoveQuickCaptureDraft`.
4. **Type bar QC mode (S/M)** — §6 deltas.
5. **AI suggestions (M)** — `core-ai`, engine, ✨ UX, debug logging. Independent flag.

## 13. Verification items (spike before/while building)

| # | Question | Why it matters |
|---|---|---|
| V1 | ~~IME-follow inside the sheet~~ **Resolved (2026-08-29, on device)**: the inset-translation sync does not position the bar inside a dialog window; the sheet uses `SOFT_INPUT_ADJUST_RESIZE` instead and disables the translation sync. Also: quick capture keeps the type bar visible for the whole draft session (the base editor hides it once the title has text / heart clears `ShouldSelectType`) | type bar must ride the keyboard; core UX |
| V2 | Store swap latency after `spaceManager.set()` — no wrong-space type flash in the bar | D2 correctness |
| V3 | IME reliably appears on sheet open with title focus | the "2 taps" promise |
| V4 | `Block.Paste` cross-space behavior for file/image blocks | §4 move caveat (open on iOS too) |
| V5 | `Object.Create` accepts `isHidden` in create-time details | atomic hide vs. create-then-set fallback |
| V6 | `LAST_USED_DATE` in type-subscription keys (a); heart bumps it on `SetObjectType` (b) | §6 sorting |
| V7 | Editor's `BottomSheetBehavior` toolbars (undo/redo, style) inside a `BottomSheetDialog` | §7 |
| V8 | ML Kit GenAI beta4 response accessor naming + real-device eval of Nano's pick-one-of-N accuracy on short notes | §9; beta API surface may shift |

## 14. Known gaps echoed from the iOS handoff (still true here)

- Hidden-draft is an interim substitute for a real heart-side unsynced-draft state; a queryable `isDraft` relation needs heart work (GO follow-up).
- A proper cross-space types subscription is a future shared foundation (D2 is the pragmatic v1).
- AI min-length guard stays low; log every silent AI fallback; never block capture on AI.

## 15. Post-handoff iOS changes (swept 2026-08-29)

iOS shipped 9 commits after the handoff was written. Six of them **reverse contract points
this spec was built on** — recorded here so the next reader doesn't "fix" our code back to
the old contract:

| Handoff said | iOS now does | Android |
|---|---|---|
| Emptiness = `internalFlags` contains `editorDeleteEmpty` | Content-based: the flag is **one-way** — any details write clears it and it never returns | **Adopted**: title + `snippet` (§5) |
| Create, then set `isHidden` | `isHidden` in the create request (same reason: the follow-up write clears the flag) | Already did this; belt-and-braces fallback kept |
| Target space's existing draft "is replaced (deleted)" | Only after a destructive confirmation, gated on the target draft actually having content | **Adopted** (§4) — `Object.ListDelete` is permanent, not the bin |
| `lastUsedDate` "set it on every create" | Client writes it on the **type object at commit** | **Adopted** — `markTypeAsRecentlyUsed()` on send; best-effort, a rejected write never fails the send |
| Preselect the last space *interacted with* | A separate last-**capture**-space store | Already diverged the same way (§4) |
| Move order: copy → create → paste → delete source | Verify the copy slot, roll back the new draft on failure, restore the replaced pointer, delete source last | Independently equivalent (`MoveQuickCaptureDraft`) |

Also ported: the restored-draft cursor policy (iOS `EditorCursorFocusPolicy.continueWriting`) —
without it a reopened non-empty draft gets no caret, no keyboard and no type bar.

AI classification now uses `mapLatest`, so a superseded suggestion is cancelled rather than
allowed to land late, and `lastClassifiedText` is only set once the engine answers (a cancelled
classification stays retryable).

Still open from the sweep: the `ObjectHeaderExpectedLayout` hint (iOS threads an expected-layout
enum into the editor so the loading placeholder matches the real header height — fixes the
header collapse for *every* editor flow, not just quick capture); skipping the widget-document
lookup in the quick-capture object menu.
