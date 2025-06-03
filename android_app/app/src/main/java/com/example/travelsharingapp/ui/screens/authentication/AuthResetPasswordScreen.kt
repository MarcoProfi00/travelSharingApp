package com.example.travelsharingapp.ui.screens.authentication

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.utils.shouldUseTabletLayout

@Composable
fun AuthResetPasswordScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    var email by remember { mutableStateOf("") }
    val passwordResetState by authViewModel.passwordResetEmailState.collectAsState()

    val configuration = LocalConfiguration.current
    val isTablet = shouldUseTabletLayout()
    val widthUi = if (isTablet && configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) 0.45f else 0.85f

    LaunchedEffect(passwordResetState) {
        when (val state = passwordResetState) {
            is PasswordResetEmailState.Success -> {
                Toast.makeText(context, "Password reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordResetEmailState()
                onNavigateToLogin()
            }
            is PasswordResetEmailState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetPasswordResetEmailState()
            }
            else -> { /* Idle or Loading */ }
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
                Text("Reset Password", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (email.isNotBlank()) {
                                authViewModel.sendPasswordResetEmail(email)
                            }
                        }
                    ),
                    modifier = Modifier
                        .semantics { contentType = ContentType.Username + ContentType.EmailAddress}
                        .fillMaxWidth()
                        .height(68.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        authViewModel.sendPasswordResetEmail(email)
                    },
                    modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                    enabled = passwordResetState !is PasswordResetEmailState.Loading
                ) {
                    Text("Send Reset Link")
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = {
                    authViewModel.resetPasswordResetEmailState()
                    onNavigateToLogin()
                }) {
                    Text("Back to Login")
                }

                if (passwordResetState is PasswordResetEmailState.Loading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator()
                }
            }
        }
    }
}