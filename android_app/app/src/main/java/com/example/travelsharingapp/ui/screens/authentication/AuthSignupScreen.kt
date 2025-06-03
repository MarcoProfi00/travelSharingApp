package com.example.travelsharingapp.ui.screens.authentication

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.utils.PasswordRequirementsTexts
import com.example.travelsharingapp.utils.PasswordStrength
import com.example.travelsharingapp.utils.calculatePasswordStrength
import com.example.travelsharingapp.utils.checkPasswordRequirements
import com.example.travelsharingapp.utils.shouldUseTabletLayout

@Composable
fun AuthSignupScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()
    val widthUi = if (isTablet && configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 0.45f else 1f

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisibility by remember { mutableStateOf(false) }
    val icon = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

    var passwordRequirementChecks by remember { mutableStateOf(checkPasswordRequirements("")) }
    var passwordFieldInteracted by remember { mutableStateOf(false) }
    var passwordStrength by remember { mutableStateOf(PasswordStrength.NONE) }

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> {
                Toast.makeText(context, "Signup Successful: ${state.user.displayName ?: "User"}", Toast.LENGTH_SHORT).show()
                //authViewModel.resetLoginState()
            }
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                //authViewModel.resetLoginState()
            }
            is AuthState.EmailVerificationRequired -> {
                Toast.makeText(context, "Email verification link sent to ${state.user.email}. Check your inbox.", Toast.LENGTH_LONG).show()
                //authViewModel.resetLoginState()
            }
            else -> { /* Unauthenticated or Loading or AccountCollisionDetected  */ }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = widthUi)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create Account", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .semantics { contentType = ContentType.Username + ContentType.EmailAddress}
                        .fillMaxWidth(),
                    supportingText = { Text("") }
                )

                PasswordStrengthMeter(strength = passwordStrength)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (!passwordFieldInteracted) passwordFieldInteracted = true
                        passwordRequirementChecks = checkPasswordRequirements(it)
                        passwordStrength = calculatePasswordStrength(it)
                    },
                    label = { Text(text = "Password") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .semantics { contentType = ContentType.Password}
                        .fillMaxWidth(),
                    isError = passwordFieldInteracted && password.isNotEmpty() && passwordRequirementChecks.any { !it.isMet },
                    supportingText = {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            passwordRequirementChecks.forEach { check ->
                                val icon = if (check.isMet) "✓" else "✗"
                                val color = when {
                                    check.isMet -> MaterialTheme.colorScheme.primary
                                    passwordFieldInteracted && password.isNotEmpty() -> MaterialTheme.colorScheme.error
                                    passwordFieldInteracted && password.isEmpty() && check.description == PasswordRequirementsTexts.LENGTH -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Text(
                                    text = "$icon ${check.description}",
                                    color = color,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(text = "Confirm Password") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .semantics { contentType = ContentType.Password}
                        .fillMaxWidth(),
                    isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                    supportingText = {
                        if (confirmPassword.isEmpty() && password.isNotEmpty()){
                            Text("Please confirm your password.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        } else if (confirmPassword.isNotEmpty() && password == confirmPassword) {
                            Text("✓ Passwords match.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        } else if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                            Text("✗ Passwords do not match.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("")
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
                val allPasswordReqsMet = password.isNotEmpty() && passwordRequirementChecks.all { it.isMet }
                val passwordsMatch = password == confirmPassword

                val canAttemptSignup = isEmailValid &&
                        allPasswordReqsMet &&
                        confirmPassword.isNotEmpty() && passwordsMatch &&
                        authState !is AuthState.Loading

                Button(

                    onClick = {
                        if (!passwordFieldInteracted) passwordFieldInteracted = true
                        passwordRequirementChecks = checkPasswordRequirements(password)


                        if (!isEmailValid) {
                            Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (!allPasswordReqsMet) {
                            Toast.makeText(context, "Please ensure your password meets all requirements.", Toast.LENGTH_LONG).show()
                            return@Button
                        }
                        if (!passwordsMatch) {
                            Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        authViewModel.signupWithEmailAndPassword(email, password)
                    },
                    modifier = Modifier.fillMaxWidth(fraction = 0.6f),
                    enabled = canAttemptSignup
                ) {
                    Text("Sign Up")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    authViewModel.resetLoginState()
                    onNavigateToLogin()
                }) {
                    Text("Already have an account? Log In")
                }
            }

            if (authState is AuthState.Loading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Creating account...")
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthMeter(strength: PasswordStrength) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp) // Add some space above the meter
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password strength: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strength.text,
                style = MaterialTheme.typography.bodySmall,
                color = strength.color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
        progress = { strength.progress },
        modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
        color = strength.color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    }
}