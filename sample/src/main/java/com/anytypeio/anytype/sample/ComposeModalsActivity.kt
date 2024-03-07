package com.anytypeio.anytype.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator

class ComposeModalsActivity: ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            androidx.compose.material3.MaterialTheme {
                FirstScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterialNavigationApi::class)
    @Composable
    fun FirstScreen() {
        val bottomSheetNavigator = rememberBottomSheetNavigator()
        val navController = rememberNavController(bottomSheetNavigator)
        ModalBottomSheetLayout(bottomSheetNavigator = bottomSheetNavigator) {
            NavHost(navController = navController, startDestination = "first") {
                composable(route = "first") {
                    MainModal {
                        navController.navigate("second")
                    }
                }
                bottomSheet(route = "second") {
                    SecondModal(
                        secondClicked = { navController.navigate("third") },
                        onDismiss = { navController.popBackStack() }
                    )
                }
                bottomSheet(route = "third") {
                    ThirdModal(
                        secondClicked = { navController.navigate("four") },
                        onDismiss = { navController.popBackStack("first", false) }
                    )
                }
                bottomSheet(route = "four") {
                    FourModal(
                        oneClicked = { navController.popBackStack() },
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainModal(buttonClicked: () -> Unit) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            sheetState = rememberModalBottomSheetState(true),
            onDismissRequest = { /*TODO*/ }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(900.dp)
                    .background(color = Color.Red)
            ) {

                Button(
                    onClick = { buttonClicked() },
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text("Click me")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SecondModal(secondClicked: () -> Unit, onDismiss: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .background(color = Color.Green)
            ) {

                Button(
                    onClick = { secondClicked() },
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text("I'm from 2")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ThirdModal(secondClicked: () -> Unit, onDismiss: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .background(color = Color.Blue)
            ) {

                Button(
                    onClick = { secondClicked() },
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text("I'm from 3")
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun FourModal(oneClicked: () -> Unit, onDismiss: () -> Unit) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { onDismiss() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .background(color = Color.Yellow)
            ) {

                Button(
                    onClick = { oneClicked() },
                    modifier = Modifier.align(Alignment.Center),
                ) {
                    Text("I'm from 4")
                }
            }
        }
    }
}