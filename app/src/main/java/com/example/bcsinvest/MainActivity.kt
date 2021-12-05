package com.example.bcsinvest

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.ui.graphics.imageFromResource
import androidx.ui.res.vectorResource
import com.example.bcsinvest.navigation.NavigationItem
import com.example.bcsinvest.screen.graph.GraphViewModel
import com.example.bcsinvest.screen.graph.NewGraphView
import com.example.bcsinvest.screen.settings.SettingsView
import com.example.bcsinvest.screen.settings.SettingsViewModel
import com.example.bcsinvest.ui.theme.BcsInvestTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    val settingsViewModel: SettingsViewModel by viewModels()
    val graphViewModel: GraphViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        setContent {

            val navController = rememberNavController()

            val systemUiController = rememberSystemUiController()
            val useDarkIcons = MaterialTheme.colors.isLight
            val context = LocalContext.current



            BcsInvestTheme {
                Scaffold(
                    floatingActionButton = {

                            FloatingActionButton(onClick = {
                                Toast.makeText(context, " Форма Оставить заявку", Toast.LENGTH_SHORT).show()
                            }
                            ) {
                                Icon(ImageVector.vectorResource(id = R.drawable.ic_assignment), contentDescription = "Создать новую")

                            }

                    },
                    isFloatingActionButtonDocked = true,
                    floatingActionButtonPosition = FabPosition.Center,
                    bottomBar = {

                            BottomAppBar(

                                cutoutShape = MaterialTheme.shapes.small.copy(
                                    CornerSize(percent = 50)
                                ),

                                contentPadding = PaddingValues(horizontal = 50.dp)
                            ) {
                                IconButton(onClick = {
                                    navController.navigate(NavigationItem.Settings.route) {
                                        launchSingleTop = true
                                    }
                                }) {
                                    Icon(
                                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_create),
                                        contentDescription = "Localized description",
                                    )
                                }
                                Spacer(Modifier.weight(0.5f, true))
                                IconButton(onClick = {
                                    navController.navigate(NavigationItem.Graphic.route) {
                                        launchSingleTop = true
                                    }
                                }) {
                                    Icon(
                                        ImageVector.vectorResource(id = R.drawable.ic_wallet),
                                        contentDescription = "Localized description",
                                    )
                                }
                            }

                    }

                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavigationItem.Settings.route
                    ) {
                        composable(NavigationItem.Settings.route) {
                            SettingsView(navController, settingsViewModel, graphViewModel)
                        }
                        composable(NavigationItem.Graphic.route) {
                            NewGraphView(navController, graphViewModel)
                        }
                    }
                }
            }

            SideEffect {
                // Update all of the system bar colors to be transparent, and use
                // dark icons if we're in light theme
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BcsInvestTheme {
        Greeting("Android")
    }
}