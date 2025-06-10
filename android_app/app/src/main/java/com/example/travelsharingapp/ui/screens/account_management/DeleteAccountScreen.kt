package com.example.travelsharingapp.ui.screens.account_management

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.travelsharingapp.ui.screens.authentication.AccountDeleteState
import com.example.travelsharingapp.ui.screens.authentication.AuthViewModel
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

@Composable
fun DeleteAccountScreen(
    modifier: Modifier = Modifier,
    topBarViewModel: TopBarViewModel,
    authViewModel: AuthViewModel,
    clearAllSessionData: () -> Unit,
    onAccountDeletedSuccessfully: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accountDeleteState by authViewModel.accountDeleteState.collectAsState()

    var currentPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val isEmailPasswordUser = currentUser?.providerData?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true


    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Delete Account",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {}
        )
        authViewModel.resetAccountDeleteState()
    }

    LaunchedEffect(accountDeleteState) {
        when (val state = accountDeleteState) {
            is AccountDeleteState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAccountDeleteState()
                onAccountDeletedSuccessfully() // Navigate to login or welcome screen
            }
            is AccountDeleteState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAccountDeleteState()
            }
            else -> { /* Idle or Loading */ }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Account Deletion",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Text(
                text = "This action is irreversible. All your data associated with this account will be permanently deleted. " +
                        "If you are sure you want to proceed, please enter your current password to confirm.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (isEmailPasswordUser) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val icon = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(imageVector = icon, contentDescription = "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (currentPassword.isBlank()) {
                            Toast.makeText(context, "Please enter your current password.", Toast.LENGTH_SHORT).show()
                        } else {
                            showConfirmationDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = accountDeleteState !is AccountDeleteState.Loading
                ) {
                    Text("Delete My Account Permanently")
                }
            } else if (currentUser != null) {
                Text(
                    text = "Account deletion for your sign-in method (e.g., Google Sign-In) might need to be managed through the provider's settings or may not require a password here. " +
                            "Proceed with caution. If you wish to delete your Firebase data, ensure you understand the implications.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Button(
                    onClick = { showConfirmationDialog = true },
                    modifier = Modifier.fillMaxWidth(0.8f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = accountDeleteState !is AccountDeleteState.Loading
                ) {
                    Text("Request Account Deletion")
                }
            } else {
                Text(
                    text = "You are not signed in.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you absolutely sure you want to delete your account? This cannot be undone.") },
                    confirmButton = {
                        Button(
                            onClick = {
                                showConfirmationDialog = false
                                authViewModel.deleteCurrentUserAccount(currentPassword, context, clearAllSessionData = clearAllSessionData)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Yes, Delete It")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmationDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        if (accountDeleteState is AccountDeleteState.Loading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Deleting account...")
            }
        }
    }
}