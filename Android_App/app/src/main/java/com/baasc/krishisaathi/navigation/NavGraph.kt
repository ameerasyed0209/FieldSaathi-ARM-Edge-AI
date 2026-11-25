package com.baasc.krishisaathi.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.baasc.krishisaathi.data.CropSubmissionData
import com.baasc.krishisaathi.data.VisionPrediction
import com.baasc.krishisaathi.screens.AddDetailsScreen
import com.baasc.krishisaathi.screens.GetAdviceScreen
import com.baasc.krishisaathi.screens.HomeScreen
import com.baasc.krishisaathi.screens.LoginScreen
import com.baasc.krishisaathi.screens.ScanCaptureScreen
import com.baasc.krishisaathi.screens.SignUpScreen
import com.baasc.krishisaathi.screens.WelcomeScreen
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun KrishiSaathiNavGraph(
    currentRegion: String,
    currentLanguageCode: String,
    onRegionAndLanguageChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    var latestSubmissionData by remember { mutableStateOf<CropSubmissionData?>(null) }
    var latestVisionPrediction by remember { mutableStateOf<VisionPrediction?>(null) }
    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(route = Screen.Welcome.route) {
            WelcomeScreen(
                currentRegion = currentRegion,
                currentLanguageCode = currentLanguageCode,
                onRegionAndLanguageChanged = onRegionAndLanguageChanged,
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.SignUp.route)
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.SignUp.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    if (!navController.popBackStack(Screen.Login.route, inclusive = false)) {
                        navController.navigate(Screen.Login.route)
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route)
                }
            )
        }

        composable(route = Screen.Home.route) {
            HomeScreen(
                selectedRegion = currentRegion,
                selectedLanguageCode = currentLanguageCode,
                onNavigateToScan = {
                    latestVisionPrediction = null
                    navController.navigate(Screen.ScanCapture.route)
                },
                onNavigateToAddDetails = { imageUri ->
                    latestVisionPrediction = null
                    navController.navigate(Screen.AddDetails.createRoute(imageUri))
                },
                onLogout = {
                    navController.popBackStack(Screen.Login.route, inclusive = false)
                }
            )
        }

        composable(route = Screen.ScanCapture.route) {
            ScanCaptureScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { imageUri, prediction ->
                    latestVisionPrediction = prediction
                    navController.navigate(Screen.AddDetails.createRoute(imageUri))
                }
            )
        }

        composable(
            route = Screen.AddDetails.route,
            arguments = listOf(
                navArgument("imageUri") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val rawUri = backStackEntry.arguments?.getString("imageUri")
            val imageUri = if (!rawUri.isNullOrEmpty()) {
                try {
                    URLDecoder.decode(rawUri, StandardCharsets.UTF_8.toString())
                } catch (e: Exception) {
                    rawUri
                }
            } else null

            AddDetailsScreen(
                imageUriString = imageUri,
                initialPrediction = latestVisionPrediction,
                selectedRegion = currentRegion,
                selectedLanguageCode = currentLanguageCode,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToGetAdvice = { submission ->
                    latestSubmissionData = submission
                    navController.navigate(Screen.GetAdvice.route)
                }
            )
        }

        composable(route = Screen.GetAdvice.route) {
            GetAdviceScreen(
                submissionData = latestSubmissionData,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    latestSubmissionData = null
                    latestVisionPrediction = null
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
