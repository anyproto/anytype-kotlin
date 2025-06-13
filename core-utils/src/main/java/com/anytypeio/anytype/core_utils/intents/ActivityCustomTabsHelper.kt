package com.anytypeio.anytype.core_utils.intents

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.anytypeio.anytype.core_utils.ext.toast


/**
 * Utility class for opening URLs in Custom Tabs with fallback to external browser
 */
object ActivityCustomTabsHelper {

    /**
     * Open URL in Custom Tabs if available, otherwise fallback to external browser
     *
     * @param activity Android activity
     * @param url URL to open
     * @param toolbarColor Optional toolbar color for Custom Tabs
     */
    fun openUrl(
        activity: Activity,
        url: String,
        toolbarColor: Int? = null
    ) {
        try {
            val uri = Uri.parse(url)
            openInCustomTabs(activity, uri, toolbarColor)
        } catch (e: Exception) {
            // Fallback to external browser if Custom Tabs fails
            try {
                openInExternalBrowser(activity, Uri.parse(url))
            } catch (fallbackException: Exception) {
                activity.toast("Failed to open URL: ${fallbackException.message}")
            }
        }
    }

    /**
     * Open URL in Custom Tabs
     */
    private fun openInCustomTabs(
        activity: Activity,
        uri: Uri, 
        toolbarColor: Int?
    ) {
        val builder = CustomTabsIntent.Builder()
        
        // Set toolbar color if provided
        toolbarColor?.let { color ->
            // Define custom color parameters
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(color)
                .build()

            builder.setDefaultColorSchemeParams(
                colorSchemeParams
            )
        }
        
        // Build and launch Custom Tab
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(activity, uri)
    }

    /**
     * Fallback to external browser
     */
    private fun openInExternalBrowser(activity: Activity, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Add package name to help with browser selection
            putExtra("com.android.browser.application_id", activity.packageName)
        }
        activity.startActivity(intent)
    }
} 