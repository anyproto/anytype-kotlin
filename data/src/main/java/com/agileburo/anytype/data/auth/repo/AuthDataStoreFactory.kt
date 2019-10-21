package com.agileburo.anytype.data.auth.repo

class AuthDataStoreFactory(
    val cache: AuthCacheDataStore,
    val remote: AuthRemoteDataStore
)