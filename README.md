# MoviePicker

MoviePicker is a small Android app for people who can't decide what to watch. It shows a wheel of movies. Spin it, and whichever movie it stops on is tonight's pick.

## How it works

- **Spin button**: tap **Spin** in the middle of the screen to send the wheel spinning at a random speed.
- **Fling the wheel**: you can also drag the wheel to turn it, or flick it to spin it yourself. The spin follows the direction of your finger.
- **Result**: the wheel slows down and stops, and the movie it lands on appears at the bottom of the screen. While it's moving, the text reads "Rotating..".

The wheel has 12 slices of 30° each:

| Movies | | |
| --- | --- | --- |
| Lord of the Rings | Donnie Darko | Filth |
| Monty Python | Pulp Fiction | Star Wars |
| Jurassic Park | A Clockwork Orange | Shaun of the Dead |
| Superbad | American Beauty | Scott Pilgrim vs. the World |

## Project structure

| Path | Description |
| --- | --- |
| `app/src/main/java/.../MainActivity.java` | All app logic: touch handling, fling detection, the spin animation and mapping the final angle to a movie |
| `app/src/main/res/layout/activity_main.xml` | Layout: the background, the wheel image, the round Spin button and the result text |
| `app/src/main/res/drawable/` | Images for the wheel, the backgrounds and the button shape |

How the spin works: the wheel image is rotated with a `Matrix`. A `GestureDetector` turns flicks into rotation speed, and a `Runnable` reposts itself on each frame, rotating the wheel and reducing the speed until it drops below a threshold. The final angle is then normalized to 0–360° and matched to a movie.

The rotary dial code is based on the tutorial by [Ralf Wondratschek](http://www.vrallev.net).

## Requirements

- Android Studio (recent version)
- JDK 17 (Android Studio's bundled JDK works)
- Android SDK Platform 35
- A device or emulator running Android 5.0 (API 21) or newer

## Build and run

1. Clone the repository:
   ```sh
   git clone https://github.com/LorenzoFeldens/MoviePicker.git
   ```
2. Open the folder in Android Studio and let Gradle sync. This creates `local.properties` with your SDK path.
3. Run the `app` configuration on a device or emulator.

To build from the command line instead:

```sh
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack

- Java
- Gradle 9.3, Android Gradle Plugin 8.13.2
- AndroidX AppCompat
- compileSdk / targetSdk 35, minSdk 21
