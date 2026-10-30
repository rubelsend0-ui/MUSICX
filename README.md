# SONIQ X PRO — Phase 1

Native Android music player (Kotlin, Jetpack Compose, Material 3, Media3, Room, Hilt).

> **Honest status:** This source was written without access to the Android SDK or Gradle.
> It has **not been compiled, unit-tested, or run on a device or emulator**.
> Expect to fix some compile errors on the first CI run. Nothing below is claimed as "tested".

## What Phase 1 contains

| Area | Status |
|---|---|
| Gradle project, version pins (AGP 8.5.2, Kotlin 1.9.24, Gradle 8.7, compileSdk 34, minSdk 26, JVM 17) | written, not built |
| Local library via MediaStore (songs/albums/artists/folders, search, sort, permission handling) | written, not built, not tested |
| Playback: ExoPlayer inside `MediaSessionService` + `MediaController` bridge, notification, audio focus, headphone-unplug pause | written, not built, not tested on device |
| Mini-player + full player (seek, shuffle, repeat, speed, queue add/remove/reorder, favorite) | written, not built, not tested |
| Room DB: songs, playlists, playlist tracks, favorites, recently played | written, not built |
| Playlists: create/rename/delete, add/remove, reorder (move up/down) | written, not built, not tested |
| Settings (DataStore): theme, accent, default sort, storage info, clear cache, clear recents, about, privacy | written, not built |
| JVM unit tests: formatting, list reorder, sorting, grouping | written, never run |
| GitHub Actions workflow | YAML parses; never executed |

## NOT in Phase 1 (not started)

Home dashboard, online search and provider architecture, lyrics, download manager (WorkManager),
embedded yt-dlp, Room/instrumented DB tests, ViewModel tests, download-state tests.
No network/INTERNET permission is declared in this phase.

## Architecture

`com.soniqxpro`: `database` (Room entities/DAOs), `repository`, `settings` (DataStore), `playback`
(`PlaybackService`, `PlayerConnection`), `ui` (screens, components, theme, viewmodel), `navigation`, `di`, `util`, `model`.
The player lives in the service so it survives the UI; the UI talks to it through `PlayerConnection`
(a `MediaController`). Commands sent before the controller connects are queued.

## Build

```
# once, if gradle/wrapper/gradle-wrapper.jar is missing (it is not included):
gradle wrapper --gradle-version 8.7
./gradlew testDebugUnitTest assembleDebug
```
APK: `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

`.github/workflows/android-build.yml` runs on push to `main` and manual dispatch: JDK 17, Android SDK,
Gradle 8.7, generates the wrapper jar if missing, runs unit tests, builds the debug APK, uploads it as
artifact **SONIQ-X-PRO-APK**, and uploads reports if the build fails.
After pushing, run once: `git update-index --chmod=+x gradlew` (the workflow also runs `chmod +x`).

## Permissions

`READ_MEDIA_AUDIO` (API 33+) / `READ_EXTERNAL_STORAGE` (API ≤32): read your music.
`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: background playback.
`POST_NOTIFICATIONS` (API 33+): media notification. `WAKE_LOCK`: keep playing with screen off.

## Known limitations

- `gradle-wrapper.jar` (binary) and `gradlew.bat` are not included.
- App icon is a simple vector mark, not final branding. Splash uses the Android 12 splash API.
- Playlist reorder is move up/down, not drag-and-drop.
- Playback speed is shown for all sources; per-source support is not checked.
- Audio-quality and playback-preference settings are not implemented yet.
