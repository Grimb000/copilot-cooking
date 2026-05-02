# pmvs10b-lab2-Grimb000

# Overview

Report on LabRabota 2.  
Android Studio laboratory work: creating and improving simple applications, number guessing game, and Calculator11 with data type analysis.

# Projects

This repository contains three main Android projects:

1. **GuessTheNumber-Java** – number guessing game implemented in Java (Task 2, 3.1).  
2. **GuessTheNumber-Kotlin** – the same game reimplemented in Kotlin with additional checks (Task 3.2).  
3. **Calculator11-Kotlin** – Calculator11 that analyses primitive data types and selects the smallest suitable type for a given number (Task 3.3, Variant 11).

Each project uses a single Activity with XML layouts based on `ConstraintLayout` and additional layout containers (TableLayout/LinearLayout).

## How to build

// The steps below are the same for all projects, only the application module name differs.

1. Open the project in **Android Studio** (Giraffe/Flamingo or newer).  
2. Make sure that Android SDK and an emulator or a physical device are configured.  
3. Select the `app` configuration and press **Run** (green triangle) to build and install the application.  
4. To run a specific module (for example, Calculator11), open that module in Android Studio or select the corresponding run configuration.

### GuessTheNumber-Java

- Language: **Java**.  
- Single `MainActivity` with XML layout and `TableLayout` for positioning controls.  
- Game logic:
  - Random integer from 1 to 100 is generated.
  - User tries to guess the number using an `EditText` and a button.
  - The application shows hints: *too low*, *too high*, *hit*.
  - A boolean flag stores whether the game is finished, the button starts a new game after a successful guess.
- Improvements (Task 3.1):
  - Handling empty input and non-numeric values (error message instead of crash).
  - Check that the number is inside the valid range (1–200) and show a separate message otherwise.
  - Limitation on the number of attempts (e.g., 10 tries) with a final message showing the hidden number.
  - Additional button to finish the Activity (Exit).
  - Custom colors for background, text, and buttons.
  - Localization for Russian and English using `values/strings.xml` and `values-en/strings.xml`.

### GuessTheNumber-Kotlin

- Language: **Kotlin**.  
- One Activity (`MainActivity`) rewritten from the Java version using idiomatic Kotlin:
  - `lateinit` properties for UI references (`TextView`, `EditText`, `Button`).
  - Use of `toInt()` / `toDouble()` and `try/catch` for safe parsing.
  - Common `onClick(View)` handler connected via `android:onClick` attribute in XML.
- Functionality:
  - The same rules as in the Java version: random number from 1 to 100, hints, and game restart.
  - Handling of empty input, invalid numbers, and out-of-range values.
  - Limitation on the number of attempts (constant `MAX_TRIES`).
  - UI and strings are reused from the Java project, localization preserved.

### Calculator11-Kotlin

- Language: **Kotlin**.  
- Application **Calculator11** consists of one `MainActivity` and two XML layouts:
  - `res/layout/activity_main.xml` – portrait orientation.
  - `res/layout-land/activity_main.xml` – landscape orientation (adapted arrangement of input field, table, and result).
- Main features:
  - `EditText` for entering a number with `numberSigned|numberDecimal` input type (supports negative and fractional values).  
  - Button "Check type" that triggers analysis of the entered value.  
  - `TableLayout` showing primitive Java/Kotlin types (`byte`, `short`, `int`, `long`, `float`, `double`, `char`) with their sizes in bytes and bits.  
  - `TextView` displaying the smallest suitable type for the given number:
    - For integral values the code compares against `Byte.MIN_VALUE`, `Short.MIN_VALUE`, `Int.MIN_VALUE`, and `Long` range.
    - For values with a fractional part the program chooses between `float` and `double` depending on precision.
  - Error messages for:
    - empty input,
    - non-numeric input,
    - numbers that do not fit into supported ranges.
- Layout:
  - Root `ConstraintLayout` with nested `LinearLayout` and `TableLayout`.
  - Additional `styles.xml` defines common styles for table cells (`CellHeader`, `Cell`) for equal width columns.
- Localization:
  - Base language (Russian) in `values/strings.xml`.
  - English translation in `values-en/strings.xml`.
  - Belarusian translation in `values-be/strings.xml`.
- Horizontal orientation:
  - Separate layout in `layout-land` to satisfy the requirement for landscape representation of the calculator UI.

# Author

Ilya Kislov, group 10.

# Additional Notes

- All projects are intended for educational purposes within LabRabota 2 (Android Studio).  
- Source code is structured by tasks: Java and Kotlin versions of the number guessing game and Calculator11 are separated into individual modules/projects.  
- The applications were tested on an Android Emulator with API level close to 30–34; minor UI differences are possible on other devices.
- The full laboratory report is available here:  
[LabRabota 2 report](https://docs.google.com/document/d/13EAgGiu-UlN-YA2pHvhIw8cLKWPMTWkLfVwQFu0-ilo/edit?usp=sharing)
