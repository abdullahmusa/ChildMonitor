package hu.aut.bme.childmonitor

import hu.aut.bme.childmonitor.domain.auth.AuthService
import hu.aut.bme.childmonitor.domain.auth.FirebaseAuthService
import hu.aut.bme.childmonitor.domain.auth.MockAuthService
import hu.aut.bme.childmonitor.domain.webrtc.ParentRepository
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun authServiceModule(): Module = module {
    single<AuthService> { FirebaseAuthService() }
}

internal actual fun parentScreenModelModule(): Module = module {
    single { ParentRepository() }
}

internal actual fun childScreenModelModule(): Module = module {}