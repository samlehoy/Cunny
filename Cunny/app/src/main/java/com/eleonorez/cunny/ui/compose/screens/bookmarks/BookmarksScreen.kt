package com.eleonorez.cunny.ui.compose.screens.bookmarks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import androidx.lifecycle.viewmodel.compose.viewModel
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Robot
import com.adamglin.phosphoricons.regular.Code
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Camera
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import com.eleonorez.cunny.data.repository.BookmarkRepository
import com.eleonorez.cunny.ui.bookmark.BookmarkViewModel
import com.eleonorez.cunny.ui.bookmark.BookmarkViewModelFactory
import com.eleonorez.cunny.ui.compose.components.AmbientBackground
import com.eleonorez.cunny.ui.compose.components.BookmarkListRow
import com.eleonorez.cunny.ui.compose.components.CunnySearchField
import com.eleonorez.cunny.ui.compose.components.SettingsDivider
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onBookmarkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { BookmarkRoomDatabase.getDatabase(context) }
    val viewModel: BookmarkViewModel = viewModel(
        factory = BookmarkViewModelFactory(BookmarkRepository(db.bookmarkDao()))
    )
    val bookmarks by viewModel.filteredBookmarks.observeAsState(emptyList())
    var query by remember { mutableStateOf("") }

    AmbientBackground(isHome = false, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
                .verticalScroll(rememberScrollState())
        ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CunnyBackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Saved",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CunnyColors.textDark
            )
        }

        CunnySearchField(
            value = query,
            onValueChange = {
                query = it
                viewModel.setQuery(it)
            },
            placeholder = "Search bookmarks...",
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        val icons = listOf(
            PhosphorIcons.Regular.Robot,
            PhosphorIcons.Regular.Code,
            PhosphorIcons.Regular.ChartBar,
            PhosphorIcons.Regular.Camera
        )

        bookmarks.filter { it.id != -1 }.forEachIndexed { index, bookmark ->
            BookmarkListRow(
                title = bookmark.title,
                subtitle = bookmark.description,
                icon = icons[index % icons.size],
                onClick = { onBookmarkClick(bookmark.title) }
            )
            SettingsDivider()
        }

        Spacer(Modifier.height(100.dp))
    
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun BookmarksPreview() {
    CunnyTheme { BookmarksScreen(onBack = {}, onBookmarkClick = {}) }
}
