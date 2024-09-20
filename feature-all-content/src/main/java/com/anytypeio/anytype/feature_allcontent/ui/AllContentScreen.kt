package com.anytypeio.anytype.feature_allcontent.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anytypeio.anytype.feature_allcontent.R
import com.anytypeio.anytype.feature_allcontent.ui.AllContentNavigation.ALL_CONTENT_MAIN

@Composable
fun AllContentScreenWrapper() {
    NavHost(
        navController = rememberNavController(),
        startDestination = ALL_CONTENT_MAIN
    ) {
        composable(
            route = ALL_CONTENT_MAIN
        ) {
            AllContentMainScreen()
        }
    }
}

@Composable
private fun AllContentMainScreen() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(id = R.color.background_primary))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item { AllContentTitle() }
        }
    }
}

object AllContentNavigation {
    const val ALL_CONTENT_MAIN = "all_content_main"
}