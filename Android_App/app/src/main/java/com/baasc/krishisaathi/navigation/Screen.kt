package com.baasc.krishisaathi.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object SignUp : Screen("signup")
    data object Home : Screen("home")
    data object ScanCapture : Screen("scan_capture")
    data object AddDetails : Screen("add_details?imageUri={imageUri}") {
        fun createRoute(imageUri: String? = null): String {
            return if (!imageUri.isNullOrEmpty()) {
                val encoded = URLEncoder.encode(imageUri, StandardCharsets.UTF_8.toString())
                "add_details?imageUri=$encoded"
            } else {
                "add_details"
            }
        }
    }
    data object GetAdvice : Screen("get_advice")
}
