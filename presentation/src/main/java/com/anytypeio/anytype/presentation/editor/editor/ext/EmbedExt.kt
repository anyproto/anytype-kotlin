package com.anytypeio.anytype.presentation.editor.editor.ext

import com.anytypeio.anytype.core_models.Block

fun Block.Content.Embed.Processor.toDisplayName(): String {
    return when (this) {
        Block.Content.Embed.Processor.MERMAID -> "Mermaid"
        Block.Content.Embed.Processor.CHART -> "Chart"
        Block.Content.Embed.Processor.YOUTUBE -> "YouTube"
        Block.Content.Embed.Processor.VIMEO -> "Vimeo"
        Block.Content.Embed.Processor.SOUNDCLOUD -> "SoundCloud"
        Block.Content.Embed.Processor.GOOGLE_MAPS -> "Google Maps"
        Block.Content.Embed.Processor.MIRO -> "Miro"
        Block.Content.Embed.Processor.FIGMA -> "Figma"
        Block.Content.Embed.Processor.TWITTER -> "Twitter"
        Block.Content.Embed.Processor.OPEN_STREET_MAP -> "OpenStreetMap"
        Block.Content.Embed.Processor.REDDIT -> "Reddit"
        Block.Content.Embed.Processor.FACEBOOK -> "Facebook"
        Block.Content.Embed.Processor.INSTAGRAM -> "Instagram"
        Block.Content.Embed.Processor.TELEGRAM -> "Telegram"
        Block.Content.Embed.Processor.GITHUB_GIST -> "GitHub Gist"
        Block.Content.Embed.Processor.CODEPEN -> "CodePen"
        Block.Content.Embed.Processor.BILIBILI -> "Bilibili"
        Block.Content.Embed.Processor.EXCALIDRAW -> "Excalidraw"
        Block.Content.Embed.Processor.KROKI -> "Kroki"
        Block.Content.Embed.Processor.GRAPHVIZ -> "Graphviz"
        Block.Content.Embed.Processor.SKETCHFAB -> "Sketchfab"
        Block.Content.Embed.Processor.IMAGE -> "Image"
        Block.Content.Embed.Processor.DRAWIO -> "Draw.io"
        Block.Content.Embed.Processor.SPOTIFY -> "Spotify"
    }
}

/**
 * Whether the embed's full text content is needed on mobile.
 * Processors that can open externally need the text (it's a URL).
 * Others (Excalidraw, Mermaid, etc.) store large data blobs that are
 * never displayed or used on mobile, so we can drop them to avoid
 * expensive main-thread string operations (DiffUtil, equals, trim).
 */
fun Block.Content.Embed.Processor.isTextNeededOnMobile(): Boolean {
    return when (this) {
        // Video platforms
        Block.Content.Embed.Processor.YOUTUBE,
        Block.Content.Embed.Processor.VIMEO,
        Block.Content.Embed.Processor.BILIBILI,
        // Social media
        Block.Content.Embed.Processor.TWITTER,
        Block.Content.Embed.Processor.FACEBOOK,
        Block.Content.Embed.Processor.INSTAGRAM,
        Block.Content.Embed.Processor.REDDIT,
        Block.Content.Embed.Processor.TELEGRAM,
        // Audio platforms
        Block.Content.Embed.Processor.SOUNDCLOUD,
        Block.Content.Embed.Processor.SPOTIFY,
        // Maps
        Block.Content.Embed.Processor.GOOGLE_MAPS,
        Block.Content.Embed.Processor.OPEN_STREET_MAP,
        // Design/collaboration tools
        Block.Content.Embed.Processor.MIRO,
        Block.Content.Embed.Processor.FIGMA,
        Block.Content.Embed.Processor.SKETCHFAB -> true
        // Diagram/code tools with potentially large data blobs
        Block.Content.Embed.Processor.MERMAID,
        Block.Content.Embed.Processor.CHART,
        Block.Content.Embed.Processor.EXCALIDRAW,
        Block.Content.Embed.Processor.KROKI,
        Block.Content.Embed.Processor.GRAPHVIZ,
        Block.Content.Embed.Processor.CODEPEN,
        Block.Content.Embed.Processor.GITHUB_GIST,
        Block.Content.Embed.Processor.DRAWIO,
        Block.Content.Embed.Processor.IMAGE -> false
    }
}
