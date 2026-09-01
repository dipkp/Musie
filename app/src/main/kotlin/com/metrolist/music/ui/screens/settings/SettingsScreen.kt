/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.metrolist.music.LocalPlayerAwareWindowInsets
import com.metrolist.music.R
import com.metrolist.music.ui.component.IconButton
import com.metrolist.music.ui.component.Material3SettingsGroup
import com.metrolist.music.ui.component.Material3SettingsItem
import com.metrolist.music.ui.utils.backToMain

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun SettingsScreen(
    navController: NavController,
    latestVersionName: String,
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val matchesSearch: (Int) -> Boolean = { titleRes ->
        searchQuery.isBlank() || context.getString(titleRes).contains(searchQuery.trim(), ignoreCase = true)
    }
    val isAndroid12OrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasAndroidAuto =
        remember {
            runCatching {
                context.packageManager.getPackageInfo("com.google.android.projection.gearhead", 0)
            }.isSuccess
        }

    val settingsItems =
        buildList {
            if (matchesSearch(R.string.account_settings)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.account),
                    title = { Text(stringResource(R.string.account_settings)) },
                    onClick = { navController.navigate("settings/account") },
                ),
            )
            if (matchesSearch(R.string.appearance)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.palette),
                    title = { Text(stringResource(R.string.appearance)) },
                    onClick = { navController.navigate("settings/appearance") },
                ),
            )
            if (matchesSearch(R.string.player_and_audio)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.play),
                    title = { Text(stringResource(R.string.player_and_audio)) },
                    onClick = { navController.navigate("settings/player") },
                ),
            )
            if (matchesSearch(R.string.listen_together)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.group_outlined),
                    title = { Text(stringResource(R.string.listen_together)) },
                    onClick = { navController.navigate("settings/integrations/listen_together") },
                ),
            )
            if (matchesSearch(R.string.content)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.language),
                    title = { Text(stringResource(R.string.content)) },
                    onClick = { navController.navigate("settings/content") },
                ),
            )
            if (matchesSearch(R.string.ai_lyrics_translation)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.translate),
                    title = { Text(stringResource(R.string.ai_lyrics_translation)) },
                    onClick = { navController.navigate("settings/ai") },
                ),
            )
            if (matchesSearch(R.string.privacy)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.security),
                    title = { Text(stringResource(R.string.privacy)) },
                    onClick = { navController.navigate("settings/privacy") },
                ),
            )
            if (matchesSearch(R.string.storage)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.storage),
                    title = { Text(stringResource(R.string.storage)) },
                    onClick = { navController.navigate("settings/storage") },
                ),
            )
            if (matchesSearch(R.string.backup_restore)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.restore),
                    title = { Text(stringResource(R.string.backup_restore)) },
                    onClick = { navController.navigate("settings/backup_restore") },
                ),
            )
            if (matchesSearch(R.string.integrations)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.integration),
                    title = { Text(stringResource(R.string.integrations)) },
                    onClick = { navController.navigate("settings/integrations") },
                ),
            )
            if (isAndroid12OrLater && matchesSearch(R.string.default_links)) {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.link),
                        title = { Text(stringResource(R.string.default_links)) },
                        onClick = {
                            try {
                                val intent =
                                    Intent(
                                        Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS,
                                        "package:${context.packageName}".toUri(),
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(intent)
                            } catch (_: ActivityNotFoundException) {
                                Toast.makeText(context, R.string.open_app_settings_error, Toast.LENGTH_LONG).show()
                            } catch (_: SecurityException) {
                                Toast.makeText(context, R.string.open_app_settings_error, Toast.LENGTH_LONG).show()
                            }
                        },
                    ),
                )
            }
            if (hasAndroidAuto && matchesSearch(R.string.android_auto)) {
                add(
                    Material3SettingsItem(
                        icon = painterResource(R.drawable.ic_android_auto),
                        title = { Text(stringResource(R.string.android_auto)) },
                        onClick = { navController.navigate("settings/android_auto") },
                    ),
                )
            }
            if (matchesSearch(R.string.changelog)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.history),
                    title = { Text(stringResource(R.string.changelog)) },
                    onClick = { navController.navigate("settings/changelog") },
                ),
            )
            if (matchesSearch(R.string.updater)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.update),
                    title = { Text(stringResource(R.string.updater)) },
                    onClick = { navController.navigate("settings/updater") },
                ),
            )
            if (matchesSearch(R.string.musie_about)) add(
                Material3SettingsItem(
                    icon = painterResource(R.drawable.info),
                    title = { Text(stringResource(R.string.musie_about)) },
                    onClick = { navController.navigate("settings/about") },
                ),
            )
        }

    Column(
        Modifier
            .windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(
            Modifier.windowInsetsPadding(
                LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Top),
            ),
        )
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_settings)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                )
            },
            trailingIcon =
                if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = { searchQuery = "" },
                            onLongClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = stringResource(R.string.clear_search),
                            )
                        }
                    }
                } else {
                    null
                },
            singleLine = true,
            shape = RoundedCornerShape(32.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
        )
        if (settingsItems.isEmpty()) {
            Text(
                text = stringResource(R.string.no_settings_found),
                modifier = Modifier.padding(24.dp),
            )
        }
        Material3SettingsGroup(
            items = settingsItems,
            useLowContrast = true,
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    TopAppBar(
        title = { Text(stringResource(R.string.settings)) },
        navigationIcon = {
            IconButton(
                onClick = navController::navigateUp,
                onLongClick = navController::backToMain,
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = null,
                )
            }
        },
    )
}
