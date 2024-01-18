# Child Monitor
A child monitoring app for Android and Desktop, built with *Kotlin Multiplatform*, *Compose Multiplatform*, *Firebase* and *WebRTC*. The application allows devices to be effortlessly set up to stream live video to other devices. With this app, parents and caregivers can conveniently keep an eye on their children by accessing real-time video feeds from anywhere.

## Two roles of the app:
1. **Child:** Streaming live audio/video for *parent devices* (associated with the account)
2. **Parent:** Interacting with *child devices* (associated with the account)

## Used technologies
<p align="left">
    <a href="https://www.jetbrains.com/kotlin-multiplatform/" target="_blank" rel="noreferrer"><img src="images/icons/kotlin.svg" width="36" height="35" alt="Kotlin Multiplatform" /></a>
    <a href="https://developer.android.com/jetpack/compose/" target="_blank" rel="noreferrer"><img src="images/icons/jetpack-compose.svg" width="36" height="36" alt="Jetpack Compose" /></a>
    <a href="https://github.com/JetBrains/compose-multiplatform/" target="_blank" rel="noreferrer"><img src="images/icons/compose-multiplatform.svg" width="36" height="36" alt="Compose Multiplatform" /></a>
    <a href="https://firebase.google.com/" target="_blank" rel="noreferrer"><img src="images/icons/firebase.svg" width="36" height="36" alt="Firebase" /></a>
    <a href="https://webrtc.org/" target="_blank" rel="noreferrer"><img src="images/icons/webrtc.svg" width="36" height="36" alt="WebRTC" /></a>
    <a href="https://insert-koin.io/" target="_blank" rel="noreferrer"><img src="images/icons/koin.svg" width="36" height="36" alt="Koin" /></a>    
    <a href="https://ktor.io/" target="_blank" rel="noreferrer"><img src="images/icons/ktor.svg" width="36" height="36" alt="Ktor" /></a>
    <a href="https://voyager.adriel.cafe/" target="_blank" rel="noreferrer"><img src="images/icons/voyager.svg" width="36" height="36" alt="Voyager" /></a>
</p>

## App in action

### Sign Up/Log In
<p align="center">
    <img src="images/screenshots/desktop-signup.png" width="560"/>
    <img src="images/screenshots/mobile-login.png" width="180" />
</p>

### Role selection
<p align="center">
    <img src="images/screenshots/desktop-role-selector-parent.png" width="560"/>
    <img src="images/screenshots/mobile-role-selector-child.png" width="180" />
</p>

### Child mode (Android only)
<p align="center">
    <img src="images/screenshots/mobile-child-streaming-landscape.png" width="350" />
    <img src="images/screenshots/mobile-child-streaming-portrait.png" width="180" />
</p>

### Parent mode - Device list
<p align="center">
    <img src="images/screenshots/desktop-parent-device-selector.png" width="560"/>
    <img src="images/screenshots/mobile-parent-device-selector.png" width="180" />
</p>

### Parent mode - Video feed
<p align="center">
    <img src="images/screenshots/desktop-parent-streaming.png" width="560"/>
    <img src="images/screenshots/mobile-parent-streaming.png" width="180" />
</p>

## Configuration
 - **Clone** the repository.
 - Use **JDK 19**.
 - Create a **Firebase** project.
 - Enable **Authentication** with **Email/Password**.
 - Enable **Realtime Database**.
 - Add an **Android** app to the project.
 - Place the `google-services.json` file in the `composeApp/` package.
 - Place the Firebase **Project ID** in `desktopMain/.../childmonitor/data/FirebaseClient.kt:28`.
 - Place the Firebase **Web API Key** in `desktopMain/.../childmonitor/domain/auth/FirebaseAuthService.kt:19`.
 - Place the TURN/STUN **server** uri/username/password in `desktopMain/.../childmonitor/domain/webrtc/WebRTCClient.kt:19-21` and `androidMain/.../childmonitor/domain/webrtc/WebRTCClient.kt:12-14`.

 *The app has Todo comments to help with the last steps above.*

*Run the application on both platforms using the Gradle Run Configurations.*