package com.eleonorez.cunny.ui.compose.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.UserCircle
import com.adamglin.phosphoricons.regular.BookmarkSimple
import com.adamglin.phosphoricons.regular.Bell
import com.adamglin.phosphoricons.regular.SpeakerHigh
import com.adamglin.phosphoricons.regular.MusicNotes
import com.adamglin.phosphoricons.regular.PaintBrush
import com.adamglin.phosphoricons.regular.Info
import com.adamglin.phosphoricons.regular.Question
import com.adamglin.phosphoricons.regular.Flag
import com.adamglin.phosphoricons.regular.SignOut
import com.adamglin.phosphoricons.regular.CaretRight
import com.eleonorez.cunny.helper.SettingsManager
import com.eleonorez.cunny.ui.compose.components.*
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager(context) }

    val narrationOn by settingsManager.narrationFlow.collectAsState(initial = true)
    val soundOn by settingsManager.soundFlow.collectAsState(initial = false)
    val currentTheme by settingsManager.themeFlow.collectAsState(initial = "light")

    val stubClick: () -> Unit = { CunnyToast.show("Coming in v2!", CunnyToastType.INFO) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    AmbientBackground(isHome = false, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
                .verticalScroll(rememberScrollState())
        ) {
        // Safe inset top padding
        Spacer(modifier = Modifier.height(12.dp))

        // Flat settings rows
        SettingsRow(
            label = "Account",
            onClick = onProfile,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.UserCircle,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    tint = CunnyColors.textSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SettingsRow(
            label = "Notifications",
            onClick = stubClick,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Bell,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    tint = CunnyColors.textSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SettingsDivider()

        SettingsRow(
            label = "Cunny narration",
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.SpeakerHigh,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                CunnyToggle(
                    checked = narrationOn,
                    onCheckedChange = { newVal ->
                        scope.launch { settingsManager.saveNarration(newVal) }
                    }
                )
            }
        )

        SettingsRow(
            label = "Sound effects",
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.MusicNotes,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                CunnyToggle(
                    checked = soundOn,
                    onCheckedChange = { newVal ->
                        scope.launch { settingsManager.saveSound(newVal) }
                    }
                )
            }
        )

        // Custom row for Theme selection with selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.PaintBrush,
                contentDescription = null,
                tint = CunnyColors.textDark,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Theme",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = CunnyColors.textDark
                )
                Text(
                    text = currentTheme.replaceFirstChar { it.uppercase() },
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle
                )
            }
            ThemeSelector(
                selectedTheme = currentTheme,
                onThemeSelected = { newVal ->
                    scope.launch { settingsManager.saveTheme(newVal) }
                }
            )
        }

        SettingsDivider()

        SettingsRow(
            label = "About",
            onClick = stubClick,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Info,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    tint = CunnyColors.textSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SettingsRow(
            label = "Help",
            onClick = stubClick,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Question,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    tint = CunnyColors.textSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SettingsRow(
            label = "Report a problem",
            onClick = stubClick,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.Flag,
                    contentDescription = null,
                    tint = CunnyColors.textDark,
                    modifier = Modifier.size(24.dp)
                )
            },
            trailing = {
                Icon(
                    imageVector = PhosphorIcons.Regular.CaretRight,
                    contentDescription = null,
                    tint = CunnyColors.textSubtle,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        SettingsDivider()

        SettingsRow(
            label = "Log out",
            onClick = onLogout,
            labelColor = CunnyColors.accentRed,
            icon = {
                Icon(
                    imageVector = PhosphorIcons.Regular.SignOut,
                    contentDescription = "Log out",
                    tint = CunnyColors.accentRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        )

        Spacer(Modifier.height(100.dp))
    
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun SettingsPreview() {
    CunnyTheme { SettingsScreen(onProfile = {}, onLogout = {}) }
}
