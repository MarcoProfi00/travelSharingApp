package com.example.travelsharingapp.ui.screens.settings

import android.app.Activity
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.travelsharingapp.BuildConfig
import com.example.travelsharingapp.R
import com.example.travelsharingapp.data.model.NotificationType
import com.example.travelsharingapp.ui.screens.authentication.AuthViewModel
import com.example.travelsharingapp.ui.screens.main.TopBarViewModel
import com.example.travelsharingapp.ui.theme.ThemeSetting
import com.example.travelsharingapp.utils.shouldUseTabletLayout
import com.google.firebase.auth.GoogleAuthProvider

enum class SettingCategory(val title: String) {
    APPEARANCE("Appearance"),
    NOTIFICATIONS("Notifications"),
    ACCOUNT_MANAGEMENT("Account Management"),
    CONNECTED_ACCOUNTS("Connected Accounts"),
    ABOUT("About");
}

data class SettingsCommonStates(
    val currentTheme: ThemeSetting,
    val masterNotificationsEnabled: Boolean,
    val notificationTypeEnabledStates: Map<NotificationType, Boolean>,
    val appVersionName: String,
    val linkedProviders: List<com.google.firebase.auth.UserInfo>,
    val authViewModel: AuthViewModel,
    val activity: Activity,
    val notificationSettingsViewModel: NotificationSettingsViewModel,
    val onShowThemeDialog: () -> Unit,
    val onNavigateToChangePassword: () -> Unit,
    val onNavigateToEditAccount: () -> Unit,
    val onNavigateToDeleteAccount: () -> Unit,
    val onLogout: () -> Unit
)

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    topBarViewModel: TopBarViewModel,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    notificationSettingsViewModel: NotificationSettingsViewModel = viewModel(),
    onNavigateToChangePassword: () -> Unit,
    onNavigateToEditAccount: () -> Unit,
    onNavigateToDeleteAccount: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit,
) {

    LaunchedEffect(Unit) {
        topBarViewModel.setConfig(
            title = "Settings",
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = { /* nothing for now */}
        )
    }

    val currentTheme by themeViewModel.themeSetting.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }

    val masterNotificationsEnabled by notificationSettingsViewModel.masterNotificationsEnabled.collectAsState()
    val notificationTypeEnabledStates by notificationSettingsViewModel.notificationTypeEnabledStates.collectAsState()

    val appVersionName = remember { BuildConfig.VERSION_NAME }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentTheme,
            onThemeSelected = { newTheme ->
                themeViewModel.updateThemeSetting(newTheme)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    val context = LocalContext.current
    val activity = context as Activity

    val linkedProviders by authViewModel.linkedProviders.collectAsState()
    val accountLinkStatus by authViewModel.accountLinkOperationStatus.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.refreshLinkedProviders()
    }

    LaunchedEffect(accountLinkStatus) {
        accountLinkStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearAccountLinkOperationStatus()
        }
    }

    val commonStates = remember(
        currentTheme, masterNotificationsEnabled, notificationTypeEnabledStates,
        appVersionName, linkedProviders, authViewModel, activity, notificationSettingsViewModel
    ) {
        SettingsCommonStates(
            currentTheme = currentTheme,
            masterNotificationsEnabled = masterNotificationsEnabled,
            notificationTypeEnabledStates = notificationTypeEnabledStates,
            appVersionName = appVersionName,
            linkedProviders = linkedProviders,
            authViewModel = authViewModel,
            activity = activity,
            notificationSettingsViewModel = notificationSettingsViewModel,
            onShowThemeDialog = { showThemeDialog = true },
            onNavigateToChangePassword = onNavigateToChangePassword,
            onNavigateToEditAccount = onNavigateToEditAccount,
            onNavigateToDeleteAccount = onNavigateToDeleteAccount,
            onLogout = onLogout
        )
    }

    val isTablet = shouldUseTabletLayout()
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isTablet && isLandscape) {
        TabletLandscapeSettingsLayout(
            modifier = modifier,
            commonStates = commonStates
        )
    } else {
        DefaultSettingsLayout(
            modifier = modifier,
            commonStates = commonStates
        )
    }
}

@Composable
fun TabletLandscapeSettingsLayout(
    modifier: Modifier = Modifier,
    commonStates: SettingsCommonStates
) {
    var selectedCategory by remember { mutableStateOf(SettingCategory.APPEARANCE) }

    Row(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            items(
                count = SettingCategory.entries.size,
                key = { index -> SettingCategory.entries[index].name },
                itemContent = { index ->
                    val category = SettingCategory.entries[index]
                    val isSelected = selectedCategory == category

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            )
                            .clickable(
                                onClick = { selectedCategory = category },
                                role = Role.Tab
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (index < SettingCategory.entries.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                when (selectedCategory) {
                    SettingCategory.APPEARANCE -> item { AppearanceSettingsItems(commonStates) }
                    SettingCategory.NOTIFICATIONS -> item { NotificationSettingsItems(commonStates) }
                    SettingCategory.ACCOUNT_MANAGEMENT -> item { AccountManagementSettingsItems(commonStates) }
                    SettingCategory.CONNECTED_ACCOUNTS -> item { ConnectedAccountsSettingsItems(commonStates) }
                    SettingCategory.ABOUT -> item { AboutSettingsItems(commonStates) }
                }
            }
        }
    }
}

@Composable
fun DefaultSettingsLayout(
    modifier: Modifier = Modifier,
    commonStates: SettingsCommonStates
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        item { SettingsCategoryHeader("Appearance") }
        item { AppearanceSettingsItems(commonStates) }
        item { SettingsDivider() }

        item { SettingsCategoryHeader("Notifications") }
        item { NotificationSettingsItems(commonStates) }
        item { SettingsDivider() }

        item { SettingsCategoryHeader("Account Management") }
        item { AccountManagementSettingsItems(commonStates) }
        item { SettingsDivider() }

        item { SettingsCategoryHeader("Connected Accounts") }
        item { ConnectedAccountsSettingsItems(commonStates) }
        item { SettingsDivider() }

        item { SettingsCategoryHeader("About") }
        item { AboutSettingsItems(commonStates) }
    }
}

@Composable
fun AppearanceSettingsItems(commonStates: SettingsCommonStates) {
    SettingItem(
        icon = { Icon(Icons.Filled.BrightnessMedium, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary) },
        title = "Theme",
        subtitle = commonStates.currentTheme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        onClick = commonStates.onShowThemeDialog
    )
}

@Composable
fun NotificationSettingsItems(commonStates: SettingsCommonStates) {
    SettingItemWithSwitch(
        icon = { Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.primary) },
        title = "Enable All Notifications",
        subtitle = "Receive all app updates and alerts",
        checked = commonStates.masterNotificationsEnabled,
        onCheckedChange = { enabled ->
            commonStates.notificationSettingsViewModel.setMasterNotificationsEnabled(enabled)
        }
    )
    NotificationType.entries.forEach { notificationType ->
        SettingItemWithSwitch(
            title = notificationType.displayName,
            subtitle = "Allow notifications for ${notificationType.displayName.lowercase()}",
            checked = commonStates.notificationTypeEnabledStates[notificationType] != false,
            enabled = commonStates.masterNotificationsEnabled,
            onCheckedChange = { enabled ->
                commonStates.notificationSettingsViewModel.setNotificationTypeEnabled(notificationType, enabled)
            },
            modifier = Modifier.padding(start = 40.dp)
        )
    }
}

@Composable
fun AccountManagementSettingsItems(commonStates: SettingsCommonStates) {
    SettingItem(
        icon = { Icon(Icons.Filled.AccountCircle, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary) },
        title = "Edit Profile",
        subtitle = "Update your traveler profile information",
        onClick = commonStates.onNavigateToEditAccount
    )
    SettingItem(
        icon = { Icon(Icons.Filled.Lock, contentDescription = "Password", tint = MaterialTheme.colorScheme.primary) },
        title = "Change Password",
        subtitle = "Update your login password",
        onClick = commonStates.onNavigateToChangePassword
    )
    SettingItem(
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.primary) },
        title = "Logout",
        subtitle = "Log out of your account",
        onClick = commonStates.onLogout
    )
    SettingItem(
        icon = { Icon(Icons.Filled.DeleteForever, contentDescription = "Delete Account", tint = MaterialTheme.colorScheme.error) },
        title = "Delete Account",
        subtitle = "Permanently remove your account and data",
        onClick = commonStates.onNavigateToDeleteAccount
    )
}

@Composable
fun ConnectedAccountsSettingsItems(commonStates: SettingsCommonStates) {
    val googleUserInfo = commonStates.linkedProviders.find { it.providerId == GoogleAuthProvider.PROVIDER_ID }
    if (googleUserInfo != null) {
        ConnectedAccountItem(
            iconResId = R.drawable.ic_google_logo,
            providerName = "Google",
            email = googleUserInfo.email,
            onDisconnect = {
                commonStates.authViewModel.unlinkGoogleAccount()
            },
            canDisconnect = !commonStates.authViewModel.isGoogleTheOnlyProvider()
        )
    } else {
        ConnectAccountItem(
            iconResId = R.drawable.ic_google_logo,
            providerName = "Google",
            onConnect = {
                commonStates.authViewModel.linkGoogleAccount(commonStates.activity)
            }
        )
    }
}

@Composable
fun AboutSettingsItems(commonStates: SettingsCommonStates) {
    SettingItem(
        title = "App Version",
        subtitle = commonStates.appVersionName
    )
}

@Composable
fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = {
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Navigate",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
) {
    val itemModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick, role = Role.Button)
    } else {
        modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Box(modifier = Modifier.padding(end = 16.dp)) {
                it()
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailingContent?.let {
            Spacer(modifier = Modifier.width(16.dp))
            it()
        }
    }
}

@Composable
fun SettingItemWithSwitch(
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Box(modifier = modifier) {
        SettingItem(
            icon = icon,
            title = title,
            subtitle = subtitle,
            onClick = { if (enabled) onCheckedChange(!checked) },
            trailingContent = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                )
            }
        )
    }
}


@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeSetting,
    onThemeSelected: (ThemeSetting) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .selectableGroup()
            ) {
                Text(
                    text = "Choose Theme",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ThemeSetting.entries.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null
                        )
                        Text(
                            text = theme.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun ConnectedAccountItem(
    iconResId: Int,
    providerName: String,
    email: String?,
    onDisconnect: () -> Unit,
    canDisconnect: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$providerName logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = providerName, style = MaterialTheme.typography.bodyLarge)
                email?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        TextButton(onClick = onDisconnect, enabled = canDisconnect) {
            Text("Disconnect", color = if (canDisconnect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
        }
    }
}

@Composable
fun ConnectAccountItem(
    iconResId: Int,
    providerName: String,
    onConnect: () -> Unit
) {
    SettingItem(
        icon = {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "$providerName logo",
                modifier = Modifier.size(24.dp)
            )
        },
        title = providerName,
        subtitle = "Connect your $providerName account",
        onClick = onConnect,
        trailingContent = {
            Text("Connect", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
    )
}