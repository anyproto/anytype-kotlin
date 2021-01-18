package com.anytypeio.anytype.domain.cover

interface CoverCollectionProvider {
    fun provide(): List<CoverImage>
}