package com.baasc.krishisaathi.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.baasc.krishisaathi.R
import com.baasc.krishisaathi.components.KrishiPrimaryButton
import com.baasc.krishisaathi.components.KrishiTextField
import com.baasc.krishisaathi.ui.theme.ButtonGreen
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.DividerColor
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.InputBackground
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.ui.theme.TextPrimary
import com.baasc.krishisaathi.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Intercept system Back button / gesture to pop back to Welcome screen
    BackHandler {
        onNavigateBack()
    }

    val focusManager = LocalFocusManager.current

    var usernameOrEmail by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }

    var usernameError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }

    val errorEmptyNameEmail = stringResource(id = R.string.error_empty_name_email)
    val errorEmptyPassword = stringResource(id = R.string.error_empty_password)

    fun validateAndLogin() {
        var isValid = true

        if (usernameOrEmail.trim().isEmpty()) {
            usernameError = errorEmptyNameEmail
            isValid = false
        } else {
            usernameError = null
        }

        if (password.trim().isEmpty()) {
            passwordError = errorEmptyPassword
            isValid = false
        } else {
            passwordError = null
        }

        if (isValid) {
            onLoginSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top background leaf header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg_tropical_leaves),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Circular white Back button
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 16.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onNavigateBack),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back),
                        tint = ForestGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // White card containing the login form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 180.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(12.dp, shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)),
                shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                color = CardBackground
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Drag indicator bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = DividerColor,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Title with leaf decoration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.welcome_back),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_plant_leaf),
                            contentDescription = "Leaf icon",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text = stringResource(id = R.string.login_subtitle),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Full Name / Email Input
                    KrishiTextField(
                        value = usernameOrEmail,
                        onValueChange = {
                            usernameOrEmail = it
                            if (usernameError != null) usernameError = null
                        },
                        placeholder = stringResource(id = R.string.full_name_or_email),
                        leadingIcon = Icons.Default.Person,
                        isError = usernameError != null,
                        errorMessage = usernameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    KrishiTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        placeholder = stringResource(id = R.string.password),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        onTrailingIconClick = { isPasswordVisible = !isPasswordVisible },
                        trailingIconDescription = if (isPasswordVisible) {
                            stringResource(id = R.string.hide_password)
                        } else {
                            stringResource(id = R.string.show_password)
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        errorMessage = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndLogin()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Remember Me & Forgot Password Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Custom rounded Remember Me checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = if (rememberMe) ButtonGreen else InputBackground,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.remember_me),
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }

                        // Forgot Password clickable
                        Text(
                            text = stringResource(id = R.string.forgot_password),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ForestGreen,
                            modifier = Modifier
                                .clickable { /* Password recovery placeholder */ }
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    // Login Button
                    KrishiPrimaryButton(
                        text = stringResource(id = R.string.login),
                        onClick = {
                            focusManager.clearFocus()
                            validateAndLogin()
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Bottom "Don't have an account? Sign up"
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dont_have_account),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = stringResource(id = R.string.sign_up),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToRegister)
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    KrishiSaathiTheme {
        LoginScreen(
            onNavigateBack = {},
            onNavigateToRegister = {},
            onLoginSuccess = {}
        )
    }
}
