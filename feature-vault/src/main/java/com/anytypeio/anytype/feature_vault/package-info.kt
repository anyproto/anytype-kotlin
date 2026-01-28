/**
 * Feature module for the Vault screen.
 *
 * This module contains:
 * - VaultScreen and related Compose UI components (migrated from :app)
 * - VaultViewModel and related presentation logic (migrated from :presentation)
 * - Vault-specific DI setup for Hilt (to be added)
 *
 * ## Migration Status
 *
 * ### UI Files (Completed)
 * Migrated from :app/ui/vault to :feature-vault/ui/:
 * - VaultScreen.kt
 * - VaultScreenToolbar.kt
 * - VaultChatCard.kt
 * - VaultSpaceCard.kt
 * - VaultDataSpaceChatCard.kt
 * - VaultOneToOneSpaceCard.kt
 * - VaultScreenAlertModals.kt
 * - VaultEmptyScreen.kt
 * - VaultSpaceActions.kt
 * - ChooseSpaceTypeScreen.kt
 * - SpacesIntroductionScreen.kt
 *
 * ### Presentation Files (Completed)
 * Migrated from :presentation/vault/ to :feature-vault/presentation/:
 * - VaultViewModel.kt
 * - VaultViewModelFactory.kt
 * - Models.kt (VaultSpaceView, VaultUiState, VaultCommand, VaultNavigation, VaultErrors)
 * - ExitToVaultDelegate.kt
 *
 * ### Key Changes
 * - R class imports updated to use module-specific R classes:
 *   - Colors/Drawables: com.anytypeio.anytype.core_ui.R
 *   - Strings: com.anytypeio.anytype.localization.R
 * - BuildConfig.SHOW_CHATS replaced with showChats parameter
 * - BuildConfig.DEBUG usage removed (commented-out code in VaultViewModel)
 * - All vault model imports use feature_vault.presentation package
 *
 * ## Dependencies
 * - :feature-chats (for chat-specific icons)
 * - :core-ui, :core-models, :localization, :domain, :analytics
 *
 * ## Tests (Completed)
 * Migrated from :presentation/vault/ to :feature-vault/presentation/:
 * - VaultViewModelTest.kt (20 tests)
 * - VaultChannelsSortingTest.kt (9 tests)
 * - VaultViewModelFabric.kt (test factory)
 * - DefaultCoroutineTestRule.kt (test utility)
 *
 * ## Integration Status (Completed 2026-01-18)
 * - :app depends on :feature-vault and uses its VaultViewModel
 * - VaultFragment.kt imports presentation and UI from feature-vault
 * - Duplicate vault UI files removed from :app/ui/vault/
 * - Utility functions (getChatTextColor, UnreadIndicatorsRow, buildChatContentWithInlineIcons) made public
 *
 * ## Next Steps
 * - Create Hilt DI module for vault dependencies
 */
package com.anytypeio.anytype.feature_vault
