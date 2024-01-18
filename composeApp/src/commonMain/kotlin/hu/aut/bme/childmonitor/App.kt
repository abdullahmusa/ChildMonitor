package hu.aut.bme.childmonitor

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import hu.aut.bme.childmonitor.presentation.common.login.LoginScreen
import hu.aut.bme.childmonitor.theme.AppTheme
import org.koin.core.context.GlobalContext
import org.koin.core.context.KoinContext
import org.koin.core.context.KoinContextHandler
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module

@Composable
internal fun App(
    systemAppearance: (isLight: Boolean) -> Unit = {},
) = AppTheme(systemAppearance) {
    if (GlobalContext.getOrNull() == null) {
        startKoin {}
    }
    loadKoinModules(
        listOf(
            authServiceModule(),
            parentScreenModelModule(),
            childScreenModelModule()
        )
    )

    Navigator(screen = LoginScreen())
}

internal expect fun authServiceModule(): Module
internal expect fun parentScreenModelModule(): Module
internal expect fun childScreenModelModule(): Module