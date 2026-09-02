/**
 * Metrolist Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.metrolist.music.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.metrolist.music.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThumbnailMenu(
    onEdit: () -> Unit,
    onChooseFromPlaylist: () -> Unit,
    onUseCollage: () -> Unit,
    showRemove: Boolean,
    onRemove: () -> Unit,
    onDismiss: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            start = 8.dp,
            top = 8.dp,
            end = 8.dp,
            bottom = 8.dp + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        ),
    ) {
        item {
            ListItem(
                headlineContent = { 
                    Text(text = stringResource(R.string.choose_from_library)) 
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.insert_photo),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    onEdit()
                    onDismiss()
                }
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.choose_from_playlist))
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    onChooseFromPlaylist()
                    onDismiss()
                },
            )
        }
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(R.string.use_auto_collage))
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.grid_view),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    onUseCollage()
                    onDismiss()
                },
            )
        }
        if (showRemove) item {
            ListItem(
                headlineContent = { 
                    Text(text = stringResource(R.string.remove_custom_image)) 
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                    )
                },
                modifier = Modifier.clickable {
                    onRemove()
                    onDismiss()
                }
            )
        }
    }
}
