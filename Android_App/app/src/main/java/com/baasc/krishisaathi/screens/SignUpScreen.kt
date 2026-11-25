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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.baasc.krishisaathi.ui.theme.CardBackground
import com.baasc.krishisaathi.ui.theme.DividerColor
import com.baasc.krishisaathi.ui.theme.ForestGreen
import com.baasc.krishisaathi.ui.theme.KrishiSaathiTheme
import com.baasc.krishisaathi.ui.theme.TextSecondary

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    // Intercept system Back button / gesture to pop back to Welcome screen
    BackHandler {
        onNavigateBack()
    }

    val focusManager = LocalFocusManager.current

    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var mobileNumber by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var fullNameError by rememberSaveable { mutableStateOf<String?>(null) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }
    var mobileError by rememberSaveable { mutableStateOf<String?>(null) }
    var passwordError by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmPasswordError by rememberSaveable { mutableStateOf<String?>(null) }

    val errorFullNameRequired = stringResource(id = R.string.error_full_name_required)
    val errorEmailRequired = stringResource(id = R.string.error_email_required)
    val errorMobileRequired = stringResource(id = R.string.error_mobile_required)
    val errorPasswordRequired = stringResource(id = R.string.error_password_required)
    val errorConfirmPasswordRequired = stringResource(id = R.string.error_confirm_password_required)
    val errorPasswordsMismatch = stringResource(id = R.string.error_passwords_mismatch)

    fun validateAndSignUp() {
        var isValid = true

        if (fullName.trim().isEmpty()) {
            fullNameError = errorFullNameRequired
            isValid = false
        } else {
            fullNameError = null
        }

        if (email.trim().isEmpty()) {
            emailError = errorEmailRequired
            isValid = false
        } else {
            emailError = null
        }

        if (mobileNumber.trim().isEmpty()) {
            mobileError = errorMobileRequired
            isValid = false
        } else {
            mobileError = null
        }

        if (password.trim().isEmpty()) {
            passwordError = errorPasswordRequired
            isValid = false
        } else {
            passwordError = null
        }

        if (confirmPassword.trim().isEmpty()) {
            confirmPasswordError = errorConfirmPasswordRequired
            isValid = false
        } else if (password != confirmPassword) {
            confirmPasswordError = errorPasswordsMismatch
            isValid = false
        } else {
            confirmPasswordError = null
        }

        if (isValid) {
            onSignUpSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Top background leaf header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
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

        // White card containing the complete Sign Up form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 130.dp)
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
                        .padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Small drag / accent indicator bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(
                                color = DividerColor,
                                shape = RoundedCornerShape(2.dp)
                            )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title with leaf decoration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.create_account_title),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_plant_leaf),
                            contentDescription = "Leaf icon",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Subtitle
                    Text(
                        text = stringResource(id = R.string.create_account_subtitle),
                        fontSize = 14.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 1. Full Name
                    KrishiTextField(
                        value = fullName,
                        onValueChange = {
                            fullName = it
                            if (fullNameError != null) fullNameError = null
                        },
                        placeholder = stringResource(id = R.string.enter_full_name),
                        leadingIcon = Icons.Default.Person,
                        isError = fullNameError != null,
                        errorMessage = fullNameError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Email
                    KrishiTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            if (emailError != null) emailError = null
                        },
                        placeholder = stringResource(id = R.string.enter_email),
                        leadingIcon = Icons.Default.Email,
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Mobile Number
                    KrishiTextField(
                        value = mobileNumber,
                        onValueChange = {
                            mobileNumber = it
                            if (mobileError != null) mobileError = null
                        },
                        placeholder = stringResource(id = R.string.enter_mobile_number),
                        leadingIcon = Icons.Default.Phone,
                        isError = mobileError != null,
                        errorMessage = mobileError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Password
                    KrishiTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            if (passwordError != null) passwordError = null
                        },
                        placeholder = stringResource(id = R.string.create_password),
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
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Confirm Password
                    KrishiTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            if (confirmPasswordError != null) confirmPasswordError = null
                        },
                        placeholder = stringResource(id = R.string.confirm_password),
                        leadingIcon = Icons.Default.Lock,
                        trailingIcon = if (isConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        onTrailingIconClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
                        trailingIconDescription = if (isConfirmPasswordVisible) {
                            stringResource(id = R.string.hide_password)
                        } else {
                            stringResource(id = R.string.show_password)
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        errorMessage = confirmPasswordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                validateAndSignUp()
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Create Account Button
                    KrishiPrimaryButton(
                        text = stringResource(id = R.string.create_account_btn),
                        onClick = {
                            focusManager.clearFocus()
                            validateAndSignUp()
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottom "Already have an account? Sign In"
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.already_have_account),
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = stringResource(id = R.string.sign_in),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            modifier = Modifier
                                .clickable(onClick = onNavigateToLogin)
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
fun SignUpScreenPreview() {
    KrishiSaathiTheme {
        SignUpScreen(
            onNavigateBack = {},
            onNavigateToLogin = {},
            onSignUpSuccess = {}
        )
    }
}
