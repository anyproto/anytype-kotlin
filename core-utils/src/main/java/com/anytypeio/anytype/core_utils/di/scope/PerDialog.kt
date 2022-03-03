package com.anytypeio.anytype.core_utils.di.scope

import javax.inject.Scope

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class PerDialog

@Scope
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class CreateFromScratch