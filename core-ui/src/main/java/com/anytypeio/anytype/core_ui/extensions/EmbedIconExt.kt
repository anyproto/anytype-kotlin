package com.anytypeio.anytype.core_ui.extensions

import androidx.annotation.DrawableRes
import com.anytypeio.anytype.core_ui.R

/**
 * Determines if an embed can be opened in an external app or browser.
 */
fun String.canOpenEmbedExternally(): Boolean {
    return when (this) {
        // Video platforms
        "YouTube", "Vimeo", "Bilibili" -> true
        // Social media
        "Twitter", "Facebook", "Instagram", "Reddit", "Telegram" -> true
        // Audio platforms
        "SoundCloud", "Spotify" -> true
        // Maps
        "Google Maps", "OpenStreetMap" -> true
        // Design/collaboration tools
        "Miro", "Figma", "Sketchfab" -> true
        // Diagram/code tools that don't open externally on mobile
        "Mermaid", "Chart", "Excalidraw", "Kroki", "Graphviz",
        "CodePen", "GitHub Gist", "Draw.io" -> false
        // Images
        "Image" -> false
        else -> false
    }
}

/**
 * Maps embed processor display name to its corresponding icon drawable resource.
 * Returns the generic embed icon as a fallback if no specific icon is found.
 */
@DrawableRes
fun String.toEmbedIconResource(): Int {
    return when (this) {
        "Mermaid" -> R.drawable.ic_embed_mermaid
        "Chart" -> R.drawable.ic_embed_chart
        "YouTube" -> R.drawable.ic_embed_youtube
        "Vimeo" -> R.drawable.ic_embed_vimeo
        "SoundCloud" -> R.drawable.ic_embed_soundcloud
        "Google Maps" -> R.drawable.ic_embed_google_maps
        "Miro" -> R.drawable.ic_embed_miro
        "Figma" -> R.drawable.ic_embed_figma
        "Twitter" -> R.drawable.ic_embed_twitter
        "OpenStreetMap" -> R.drawable.ic_embed_open_street_map
        "Reddit" -> R.drawable.ic_embed_reddit
        "Facebook" -> R.drawable.ic_embed_facebook
        "Instagram" -> R.drawable.ic_embed_generic_embed_icon // Instagram icon not available (PDF format)
        "Telegram" -> R.drawable.ic_embed_telegram
        "GitHub Gist" -> R.drawable.ic_embed_github_gist
        "CodePen" -> R.drawable.ic_embed_codepen
        "Bilibili" -> R.drawable.ic_embed_bilibili
        "Excalidraw" -> R.drawable.ic_embed_excalidraw
        "Kroki" -> R.drawable.ic_embed_kroki
        "Graphviz" -> R.drawable.ic_embed_graphviz
        "Sketchfab" -> R.drawable.ic_embed_sketchfab
        "Image" -> R.drawable.ic_embed_external_image
        "Draw.io" -> R.drawable.ic_embed_drawio
        "Spotify" -> R.drawable.ic_embed_spotify
        else -> R.drawable.ic_embed_generic_embed_icon
    }
}
