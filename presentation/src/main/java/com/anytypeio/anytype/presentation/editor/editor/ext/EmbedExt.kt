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
