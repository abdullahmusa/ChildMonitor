package hu.aut.bme.childmonitor

import android.Manifest
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import hu.aut.bme.childmonitor.domain.auth.AuthService
import hu.aut.bme.childmonitor.domain.auth.FirebaseAuthService
import hu.aut.bme.childmonitor.domain.webrtc.ChildScreenModel
import hu.aut.bme.childmonitor.domain.webrtc.ParentScreenModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startKoin {
            androidContext(this@AppActivity)
        }

        val systemBarColor = Color.TRANSPARENT
        requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

        setContent {
            val view = LocalView.current
            var isLightStatusBars by remember { mutableStateOf(false) }
            if (!view.isInEditMode) {
                LaunchedEffect(isLightStatusBars) {
                    val window = (view.context as Activity).window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    window.statusBarColor = systemBarColor
                    window.navigationBarColor = systemBarColor
                    WindowCompat.getInsetsController(window, window.decorView).apply {
                        isAppearanceLightStatusBars = isLightStatusBars
                        isAppearanceLightNavigationBars = isLightStatusBars
                    }
                }
            }
            App(systemAppearance = { isLight -> isLightStatusBars = isLight })
        }
    }
}

actual fun authServiceModule(): Module = module {
    single<AuthService> { FirebaseAuthService(Firebase.auth) }
}

internal actual fun parentScreenModelModule(): Module = module {
    single { ParentScreenModel(context = androidContext()) }
}

internal actual fun childScreenModelModule(): Module = module {
    single { ChildScreenModel(context = androidContext()) }
}