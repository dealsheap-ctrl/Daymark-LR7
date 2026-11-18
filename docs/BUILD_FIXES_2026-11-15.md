# Build fixes — 2026-11-15

## Fix 1: Bottom navigation resource names

`app/src/main/res/menu/bottom_nav_menu.xml` referenced `tab_tasks`, `tab_insights`, and `tab_settings`, but the string resources are named `nav_tasks`, `nav_insights`, and `nav_settings` in both the default and Russian resource directories. The menu now uses the existing `nav_*` names.

## Fix 2: Unsupported Material attribute

Removed `app:itemActiveIndicatorColor` from `activity_main.xml`. That attribute was not found by the resource linker in the Material Components dependency currently pinned by this project. The bottom navigation continues to use the configured icon and text tint colors; only the optional active-indicator tint is omitted.

## Retry build

In Android Studio, open the project root (the directory containing `settings.gradle.kts`), run **File → Sync Project with Gradle Files**, then **Build → Make Project**. If a new error appears, capture the first error line and the complete detail pane.

## Fix 3: Debug exception buttons no longer terminate the app

The `NetworkOnMainThreadException` reproduction previously installed a `StrictMode` policy with `penaltyDeath()`. Android documents that penalty as process-fatal, so the emulator closed Daymark as soon as the network violation happened. The three debug scenarios now throw the intended exceptions and are caught at the `SettingsFragment` UI boundary. `AppLog.e` records the full stack trace under the `Daymark` tag and the user gets a small confirmation Toast.

For the network case, the debug helper briefly enables `detectNetwork().penaltyLog()` and performs a loopback-only request; it never enables `penaltyDeath` and does not contact an external host. The exact `NetworkOnMainThreadException` is then raised deterministically for the debugger. In Android Studio, set the exception breakpoints to stop on **Caught** exceptions. The app remains open after resuming execution.
