package com.example.travelsharingapp.ui.screens.account_management

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.ui.screens.authentication.AuthViewModel
import com.example.travelsharingapp.ui.screens.authentication.PasswordChangeState
import com.example.travelsharingapp.ui.screens.authentication.PasswordStrengthMeter
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.utils.PasswordRequirementsTexts
import com.example.travelsharingapp.utils.PasswordStrength
import com.example.travelsharingapp.utils.calculatePasswordStrength
import com.example.travelsharingapp.utils.checkPasswordRequirements
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    topBarViewModel: TopBarViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Change Password",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* nothing for now */}
        )
    }

    val context = LocalContext.current
    val passwordChangeState by authViewModel.passwordChangeState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }

    var currentPasswordVisibility by remember { mutableStateOf(false) }
    var newPasswordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) } // Independent visibility

    var newPasswordRequirementChecks by remember { mutableStateOf(checkPasswordRequirements("")) }
    var newPasswordFieldInteracted by remember { mutableStateOf(false) }
    var passwordStrength by remember { mutableStateOf(PasswordStrength.NONE) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val isEmailPasswordUser = currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true

    LaunchedEffect(passwordChangeState) {
        when (val state = passwordChangeState) {
            is PasswordChangeState.Success -> {
                Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordChangeState()
                onBack()
            }
            is PasswordChangeState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordChangeState()
            }
            else -> { /* Idle or Loading */ }
        }
    }

    if (!isEmailPasswordUser && currentUser != null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Password change is not available for your sign-in method (e.g., Google Sign-In).",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onBack) {
                Text("Go Back")
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Update Your Password", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (currentPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (currentPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { currentPasswordVisibility = !currentPasswordVisibility }) {
                        Icon(imageVector = icon, contentDescription = "Toggle current password visibility")
                    }
                },
                modifier = Modifier
                    .semantics { contentType = ContentType.Password}
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            PasswordStrengthMeter(strength = passwordStrength)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    if (!newPasswordFieldInteracted) newPasswordFieldInteracted = true
                    newPasswordRequirementChecks = checkPasswordRequirements(it)
                    passwordStrength = calculatePasswordStrength(it)
                },
                label = { Text("New Password") },
                singleLine = true,
                trailingIcon = {
                    val icon = if (newPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { newPasswordVisibility = !newPasswordVisibility }) {
                        Icon(imageVector = icon, contentDescription = "Toggle new password visibility")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                visualTransformation = if (newPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .semantics { contentType = ContentType.NewPassword}
                    .fillMaxWidth(),
                isError = newPasswordFieldInteracted && newPassword.isNotEmpty() && newPasswordRequirementChecks.any { !it.isMet },
                supportingText = {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        newPasswordRequirementChecks.forEach { check ->
                            val (iconChar, color) = when {
                                check.isMet -> "✓" to MaterialTheme.colorScheme.primary
                                newPasswordFieldInteracted && newPassword.isNotEmpty() -> "✗" to MaterialTheme.colorScheme.error
                                newPasswordFieldInteracted && newPassword.isEmpty() && check.description == PasswordRequirementsTexts.LENGTH -> "✗" to MaterialTheme.colorScheme.error
                                else -> "•" to MaterialTheme.colorScheme.onSurfaceVariant
                            }
                            Text(
                                text = "$iconChar ${check.description}",
                                color = color,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = { confirmNewPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                trailingIcon = {
                    val icon = if (confirmPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(imageVector = icon, contentDescription = "Toggle confirm password visibility")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier
                    .semantics { contentType = ContentType.NewPassword}
                    .fillMaxWidth(),
                isError = confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword,
                supportingText = {
                    if (confirmNewPassword.isEmpty() && newPassword.isNotEmpty()){
                        Text("Please confirm your new password.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    } else if (confirmNewPassword.isNotEmpty() && newPassword == confirmNewPassword) {
                        Text("✓ Passwords match.", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                    } else if (confirmNewPassword.isNotEmpty() && newPassword != confirmNewPassword) {
                        Text("✗ Passwords do not match.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            val allPasswordReqsMet = newPassword.isNotEmpty() && newPasswordRequirementChecks.all { it.isMet }
            val passwordsMatch = newPassword == confirmNewPassword
            val currentPasswordEntered = currentPassword.isNotBlank()

            val canAttemptChange = currentPasswordEntered &&
                    allPasswordReqsMet &&
                    passwordsMatch &&
                    passwordChangeState !is PasswordChangeState.Loading

            Button(
                onClick = {
                    if (!newPasswordFieldInteracted) newPasswordFieldInteracted = true
                    newPasswordRequirementChecks = checkPasswordRequirements(newPassword) // Re-check on attempt

                    if (!currentPasswordEntered) {
                        Toast.makeText(context, "Please enter your current password.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (!allPasswordReqsMet) {
                        Toast.makeText(context, "Please ensure your new password meets all requirements.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (!passwordsMatch) {
                        Toast.makeText(context, "New passwords do not match.", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    authViewModel.changePassword(currentPassword, newPassword)
                },
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                enabled = canAttemptChange
            ) {
                Text("Change Password")
            }
        }
    }

    if (passwordChangeState is PasswordChangeState.Loading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Updating password...")
        }
    }
}