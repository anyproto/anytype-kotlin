package com.anytypeio.anytype.data.auth.repo

class AuthDataStoreFactory(
    val cache: AuthCacheDataStore,
    val remote: AuthRemoteDataStore
)