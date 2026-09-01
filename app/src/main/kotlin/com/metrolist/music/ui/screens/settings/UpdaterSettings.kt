/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.metrolist.music.BuildConfig
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.constants.CheckForUpdatesKey
import com.metrolist.music.constants.UpdateNotificationsEnabledKey
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain
import com.metrolist.music.utils.Updater
import com.metrolist.music.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdaterScreen(navController: NavController) {
    val (automaticChecks, setAutomaticChecks) = rememberPreference(CheckForUpdatesKey, true)
    val (notifications, setNotifications) = rememberPreference(UpdateNotificationsEnabledKey, true)
    var checking by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var resultIsError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun checkNow() {
        if (checking) return
        scope.launch {
            checking = true
            resultText = null
            val result = withContext(Dispatchers.IO) { Updater.checkForUpdate(forceRefresh = true) }
            result.fold(
                onSuccess = { (release, available) ->
                    resultIsError = false
                    resultText =
                        when {
                            release == null -> "No published Musie release was found."
                            available -> "Musie ${release.versionName} is available."
                            else -> "Musie is up to date (${BuildConfig.VERSION_NAME})."
                        }
                },
                onFailure = {
                    resultIsError = true
                    resultText = "Update check failed: ${it.message ?: "unknown error"}"
                },
            )
            checking = false
        }
    }

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                ),
            ).verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top),
            ),
        )
        Material3SettingsGroup(
            title = stringResource(R.string.current_version),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.info),
                        title = { Text("Musie ${BuildConfig.VERSION_NAME}") },
                        description = { Text("${BuildConfig.ARCHITECTURE.uppercase()} • ${if (BuildConfig.DEBUG) "DEBUG" else "RELEASE"}") },
                    ),
                ),
        )
        Spacer(Modifier.height(20.dp))
        Material3SettingsGroup(
            title = stringResource(R.string.update_settings),
            items =
                listOf(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.update),
                        title = { Text(stringResource(R.string.check_for_updates)) },
                        trailingContent = {
                            Switch(checked = automaticChecks, onCheckedChange = setAutomaticChecks)
                        },
                        onClick = { setAutomaticChecks(!automaticChecks) },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.notification),
                        title = { Text(stringResource(R.string.update_notifications)) },
                        trailingContent = {
                            Switch(checked = notifications, onCheckedChange = setNotifications)
                        },
                        onClick = { setNotifications(!notifications) },
                    ),
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.refresh),
                        title = { Text(stringResource(R.string.check_for_updates_button)) },
                        trailingContent = {
                            if (checking) CircularProgressIndicator(strokeWidth = 2.dp)
                        },
                        onClick = ::checkNow,
                    ),
                ),
        )
        resultText?.let {
            Spacer(Modifier.height(16.dp))
            Text(
                text = it,
                color = if (resultIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.updater)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
            }
        },
    )
}
