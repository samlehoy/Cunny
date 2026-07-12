package com.eleonorez.cunny.ui.compose.screens.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.R
import com.eleonorez.cunny.di.Injection
import com.eleonorez.cunny.ui.compose.components.*
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import com.eleonorez.cunny.data.retrofit.ApiConfig
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import kotlinx.coroutines.tasks.await
import com.eleonorez.cunny.data.billing.BillingManager

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progressRepository = remember { Injection.provideProgressRepository(context) }
    val badgesState by progressRepository.unlockedBadges.collectAsState(initial = emptyList())

    val user = Firebase.auth.currentUser
    var name by remember { mutableStateOf(user?.displayName ?: "User") }
    var email by remember { mutableStateOf(user?.email ?: "user@example.com") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .cunnyStatusBarPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Custom Header Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CunnyBackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Profile",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CunnyColors.textDark
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar circle with gradient + edit button overlay
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(CunnyColors.gradPlum)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = name.take(1).uppercase()
                    Text(
                        text = initial,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                        color = Color.White
                    )
                }

                // Edit button overlay (bottom-right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { /* edit photo */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = "Edit photo",
                        tint = CunnyColors.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "Change Photo",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CunnyColors.primary,
                modifier = Modifier.clickable { /* change photo */ }
            )

            Spacer(Modifier.height(24.dp))

            // Form fields
            CunnyFormField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            CunnyFormField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )
            CunnyFormField(
                label = "Old Password",
                value = oldPassword,
                onValueChange = { oldPassword = it },
                isPassword = true,
                placeholder = "Enter old password",
                modifier = Modifier.padding(bottom = 16.dp)
            )
            CunnyFormField(
                label = "New Password",
                value = newPassword,
                onValueChange = { newPassword = it },
                isPassword = true,
                placeholder = "Enter new password",
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(Modifier.height(8.dp))
            CunnyPrimaryButton(text = "Save Changes", onClick = {
                CunnyToast.show("Saved!", CunnyToastType.SUCCESS)
            })

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Badges",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(10.dp))

            if (badgesState.isEmpty()) {
                Text(
                    text = "Complete lessons and scans to earn badges.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    modifier = Modifier.align(Alignment.Start)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    badgesState.forEach { badge ->
                        val label = when (badge.badgeId) {
                            "lesson-complete" -> "🏆 Lesson Complete"
                            "first-scan" -> "📸 First Scan"
                            else -> badge.badgeId
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(CunnyColors.primaryPale)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CunnyColors.primary
                            )
                        }
                    }
                }
            }

            var showDeleteDialog by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val db = remember { BookmarkRoomDatabase.getDatabase(context) }
            val billingManager = remember { BillingManager.getInstance(context) }
            val isPremium by billingManager.isPremium.collectAsState()

            // Restore Purchase section
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Subscription",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))
            if (isPremium) {
                Text(
                    text = "✅ Cunny Premium aktif",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CunnyColors.accentGreen,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(Modifier.height(8.dp))
            }
            CunnyPrimaryButton(
                text = "Pulihkan Pembelian",
                onClick = {
                    scope.launch {
                        billingManager.queryExistingPurchases()
                        // Small delay to let the StateFlow update
                        kotlinx.coroutines.delay(500)
                        val restored = billingManager.isPremium.value
                        val msg = if (restored) {
                            "Langganan berhasil dipulihkan!"
                        } else {
                            "Tidak ada langganan aktif ditemukan"
                        }
                        withContext(Dispatchers.Main) {
                            CunnyToast.show(msg, if (restored) CunnyToastType.SUCCESS else CunnyToastType.INFO)
                        }
                    }
                }
            )
            
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Danger Zone",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFD32F2F),
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(Modifier.height(8.dp))
            CunnyPrimaryButton(
                text = "Delete Account",
                onClick = { showDeleteDialog = true },
                containerColor = Color(0xFFD32F2F)
            )

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Account?", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold) },
                    text = { Text("This will permanently delete your account, learning progress, achievements, and all related data. This action cannot be undone.", fontFamily = DmSansFontFamily) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                scope.launch {
                                    try {
                                        // 1. Delete from PostgreSQL backend
                                        val apiService = ApiConfig.getApiService()
                                        apiService.deleteAccount()
                                        
                                        // 2. Clear local Room database
                                        withContext(Dispatchers.IO) {
                                            db.gamificationDao().clearAllGamificationData()
                                        }
                                        
                                        // 3. Delete user account from Firebase Auth
                                        val firebaseUser = Firebase.auth.currentUser
                                        if (firebaseUser != null) {
                                            firebaseUser.delete().await()
                                        }
                                        Firebase.auth.signOut()
                                        
                                        // 4. Navigate to Login screen (clear entire back stack)
                                        CunnyToast.show("Account deleted successfully", CunnyToastType.SUCCESS)
                                        onAccountDeleted()
                                    } catch (e: Exception) {
                                        CunnyToast.show("Failed to delete account: ${e.message}", CunnyToastType.ERROR)
                                    }
                                }
                            }
                        ) {
                            Text("Delete Permanently", color = Color(0xFFD32F2F), fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", fontFamily = SoraFontFamily)
                        }
                    }
                )
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun ProfilePreview() {
    CunnyTheme { ProfileScreen(onBack = {}, onAccountDeleted = {}) }
}
