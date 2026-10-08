package com.xalid.meditsinaproroka.nativeapp

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class MedicineLaunchState(
    val store: AppStore,
    val bookResult: Result<BookData>,
)

class MainActivity : ComponentActivity() {
    // A platform splash covers the initial book parse, never a text interstitial.
    @Volatile private var initialScreenReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        splash.setKeepOnScreenCondition { !initialScreenReady }
        super.onCreate(savedInstanceState)
        configureActivityTransitions()

        val appContext = applicationContext
        val launchTheme = if (
            getSharedPreferences("alfatiha_native", MODE_PRIVATE).getBoolean("dark", false)
        ) "dark" else "light"
        // The system splash may restore bright status-bar icons. Set the
        // initial contrast before Compose, then reapply after splash dismissal.
        applySystemBars(launchTheme)
        // Apply the contrast after the Android splash window is actually removed.
        // Compose's first visible frame may precede the platform's exit callback.
        splash.setOnExitAnimationListener { splashView ->
            splashView.remove()
            applySystemBars(launchTheme)
        }

        MedicineRuntimeWarmup.preload(appContext)

        setContent {
            val warmedState = remember {
                val warmedStore = MedicineRuntimeWarmup.peekStore()
                val warmedBook = MedicineBookCache.peekOrNull()
                if (warmedStore != null && warmedBook != null) {
                    warmedStore.syncSharedTheme()
                    MedicineLaunchState(
                        store = warmedStore,
                        bookResult = Result.success(warmedBook),
                    )
                } else {
                    null
                }
            }
            var launchState by remember { mutableStateOf(warmedState) }

            LaunchedEffect(Unit) {
                if (launchState == null) {
                    launchState = withContext(Dispatchers.IO) {
                        val store = MedicineRuntimeWarmup.getOrCreateStore(appContext)
                        store.syncSharedTheme()
                        val bookResult = MedicineBookCache.getOrLoad(appContext)
                        bookResult.exceptionOrNull()?.let { error ->
                            Log.e(TAG, "Failed to load bundled book.json", error)
                        }
                        MedicineLaunchState(
                            store = store,
                            bookResult = bookResult,
                        )
                    }
                }
            }

            val state = launchState
            LaunchedEffect(state) {
                if (state != null) {
                    // Allow Compose one frame to commit the actual home/error screen
                    // before releasing Android's splash on the next pre-draw.
                    withFrameNanos { }
                    initialScreenReady = true
                    // The splash exit can override WindowInsetsController flags.
                    // Apply the active reader theme again on the first visible frame.
                    withFrameNanos { }
                    applySystemBars(state.store.settings.theme)
                }
            }
            MedicinaTheme(state?.store?.settings?.theme ?: launchTheme) {
                if (state == null) {
                    BookLoadingScreen()
                } else {
                    state.bookResult.fold(
                        onSuccess = { book ->
                            MedicinaApp(book = book, store = state.store)
                        },
                        onFailure = { BookLoadErrorScreen() },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MedicineRuntimeWarmup.peekStore()?.syncSharedTheme()
    }

    private fun applySystemBars(mode: String) {
        val dark = mode == "dark" ||
            (mode != "light" && (resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.isAppearanceLightStatusBars = !dark
        controller.isAppearanceLightNavigationBars = !dark
    }

    private fun configureActivityTransitions() {
        if (Build.VERSION.SDK_INT >= 34) {
            // Opening is already driven by ActivityOptions.makeCustomAnimation()
            // in the host activity. Do not stack a second OPEN transition here.
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.section_return_enter,
                R.anim.section_return_exit,
            )
        }
    }

    private companion object {
        const val TAG = "MedicineProphet"
    }
}

@Composable
private fun BookLoadingScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Safety fallback for older OSes: show the same branded icon as
            // the Android splash, never a second "opening book" text screen.
            Image(
                painter = painterResource(R.drawable.medicine_launcher),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun BookLoadErrorScreen() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Card {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "Не удалось открыть книгу",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Не удалось прочитать данные книги. Закройте раздел и попробуйте открыть его снова.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
