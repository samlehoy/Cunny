package com.eleonorez.cunny

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.eleonorez.cunny.helper.SettingsManager
import com.eleonorez.cunny.ui.compose.navigation.CunnyRootApp
import com.eleonorez.cunny.ui.compose.navigation.CunnyRoutes
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val settingsManager = remember { SettingsManager(applicationContext) }
            val currentTheme by settingsManager.themeFlow.collectAsState(initial = "light")
            val useDarkTheme = when (currentTheme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            CunnyTheme(darkTheme = useDarkTheme) {
                val startDestination = remember {
                    val onboardingFinished = onBoardingFinished()
                    val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                    when {
                        !onboardingFinished -> CunnyRoutes.ONBOARDING_STEP_1
                        !isLoggedIn -> CunnyRoutes.AUTH_LOGIN
                        else -> CunnyRoutes.HOME
                    }
                }

                CunnyRootApp(
                    startDestination = startDestination,
                    onOnboardingComplete = {
                        getSharedPreferences("onBoarding", MODE_PRIVATE)
                            .edit()
                            .putBoolean("Finished", true)
                            .apply()
                    },
                    onRoleSave = { role ->
                        getSharedPreferences("onBoarding", MODE_PRIVATE)
                            .edit()
                            .putString("user_role", role)
                            .apply()
                    }
                )
            }
        }
    }

    private fun onBoardingFinished(): Boolean {
        val sharedPref = getSharedPreferences("onBoarding", MODE_PRIVATE)
        return sharedPref.getBoolean("Finished", false)
    }
}
