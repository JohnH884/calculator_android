Introduction

PocketCalculator is a calculator for the Android platform developed using Kotlin and Jetpack Compose to fulfill the calculator project requirements. This calculator has capabilities such as working with multiple digits, decimals, addition, subtraction, multiplication, division, clearing, and equals operations.

Improvements

- Percentage: The input number is transformed into a percentage. Example: 200 × 10% = 20.

- Backspace: Deletes the previous number in the display safely. If all numbers are deleted, then the display becomes zero.

- Sign Change: Clicking the ± button toggles from positive to negative values.

- Calculation history: Displays the most recent 20 calculations made by the user, along with a clear button.

- Dark Mode: Has an in-app option for switching between dark and light modes.

- Button Animation: Animates on press with size changing.

- Accessibility: Meaningful button description, large buttons, readable font sizes, scrollable content, and layouts for narrow and wide screens.

## Setup and Run

Requirements:
- Android Studio
- JDK 17
- Android SDK Platform 35
- Android SDK Build-Tools 34.0.0
- An emulator or Android device running API 24 or later

Steps:
1. Download or clone this repository.
2. Open the folder containing `settings.gradle.kts` in Android Studio.
3. Set the Gradle JDK to JDK 17.
4. Allow Gradle to finish syncing.
5. Start an emulator or connect an Android device with USB debugging enabled.
6. Select the `app` run configuration and your device.
7. Click Run to install and open the calculator.

To build the APK, run unit tests, and check lint, open PowerShell in the project folder with `JAVA_HOME` set to your JDK 17 installation:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

The debug APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```
The calculation logic is separated from the user interface:

- `MainActivity.kt` starts the app.
- `CalculatorEngine.kt` handles arithmetic, number entry, percentage, deletion, sign changes, and errors.
- `CalculatorState.kt` defines the calculator state and user actions.
- `CalculatorScreen.kt` contains reusable composables for the display, keypad, and history.
- `CalculatorTheme.kt` defines the light and dark themes.
- `CalculatorStateSaver.kt` saves and restores calculator state.

Path to the source code Source code: [Open the Kotlin source files](app/src/main/java/com/example/pocketcalculator/)

## Screenshots

![Light mode](Screenshot%202026-10-27%20011136.png)

![Dark mode](Screenshot%202026-10-27%20011316.png)

![Calculation history](Screenshot%202026-10-27%200114021.png)

##video 
https://www.youtube.com/watch?v=eqMD34Ln2wY
check list
- [x] App compiles and runs without crashes
- [x] Baseline features match the video app
- [x] Two enhancements implemented and visible in the demo
- [x] README includes overview, features, setup, screenshots, and video link
- [x] APK uploaded; repo access granted
