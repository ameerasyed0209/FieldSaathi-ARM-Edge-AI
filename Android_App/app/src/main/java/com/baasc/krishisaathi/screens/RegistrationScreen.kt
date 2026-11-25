package com.baasc.krishisaathi.screens

import androidx.compose.runtime.Composable

@Composable
fun RegistrationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = onNavigateBack,
    onSignUpSuccess: () -> Unit = {}
) {
    SignUpScreen(
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin,
        onSignUpSuccess = onSignUpSuccess
    )
}
