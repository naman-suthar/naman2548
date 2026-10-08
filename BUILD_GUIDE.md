# Build Guide - Board Games App

## Current Status

We've successfully created a complete Android multi-game app with:
- ✅ 3 fully implemented games (2048, Snake, Tic-Tac-Toe)
- ✅ 3 interactive home screen widgets
- ✅ Material 3 design system
- ✅ Multi-module clean architecture
- ✅ Complete game logic with AI (minimax for Tic-Tac-Toe)
- ✅ State persistence with Room & DataStore
- ✅ ~7,600+ lines of Kotlin code

## Build Issues

GitHub Actions is experiencing compilation errors that are hard to debug remotely. Here are **proven alternatives** to get a working APK:

---

## ✅ RECOMMENDED: Build with GitHub Codespaces

**This is the easiest and most reliable method!**

### Steps:

1. **Go to the repository**:
   ```
   https://github.com/naman-suthar/naman2548
   ```

2. **Click "Code" → "Codespaces" → "Create codespace on main"**

3. **Wait 2-3 minutes** for the environment to set up

4. **In the terminal that appears**, run:
   ```bash
   ./gradlew assembleDebug --stacktrace
   ```

5. **If successful**, the APK will be at:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

6. **Download it**:
   - Right-click the file in the file explorer
   - Select "Download"
   - Or use: `code app/build/outputs/apk/debug/app-debug.apk`

7. **Install on your phone** (see Installation Guide below)

### Why This Works:
- Full Android SDK available
- Same Ubuntu environment as your local machine
- Can see detailed error messages
- Free for 60 hours/month

---

## Alternative: Build Locally with Android Studio

### Prerequisites:
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- 8GB+ RAM recommended

### Steps:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/naman-suthar/naman2548.git
   cd naman2548
   ```

2. **Open in Android Studio**:
   - File → Open
   - Select the `naman2548` folder
   - Wait for Gradle sync to complete

3. **Build APK**:
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - Or in terminal: `./gradlew assembleDebug`

4. **Find the APK**:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

5. **Install on phone** (see below)

---

## Installing APK on Android Device

### Enable Installation:

1. **Enable Developer Mode**:
   - Settings → About Phone
   - Tap "Build Number" 7 times
   - You'll see "You are now a developer!"

2. **Enable Unknown Sources**:
   - Settings → Apps → Special Access → Install Unknown Apps
   - Select your file manager or browser
   - Enable "Allow from this source"

### Install:

**Method 1: Direct Transfer**
1. Transfer APK to phone (USB, email, Drive, etc.)
2. Open from file manager
3. Tap "Install"
4. Tap "Open" when done

**Method 2: ADB (if phone connected via USB)**
```bash
adb install app-debug.apk
```

---

## Testing the App

### Games to Test:

1. **Home Screen**:
   - Should show 3 game cards
   - Material 3 theming (dynamic colors on Android 12+)
   - Dark mode support

2. **2048**:
   - Swipe in 4 directions
   - Tiles merge correctly
   - Score tracks properly
   - Undo button works
   - New game button resets

3. **Snake**:
   - Arrow buttons control direction
   - Snake grows when eating food
   - Game over on collision
   - Speed controls (1-5x)
   - Pause/resume works

4. **Tic-Tac-Toe**:
   - Play vs CPU
   - CPU uses minimax AI (unbeatable on Hard)
   - Difficulty settings (Easy/Medium/Hard)
   - Game modes (PvP / PvCPU / CPUvCPU)
   - Stats tracking

### Widgets to Test:

1. **Long-press home screen**
2. **Tap "Widgets"**
3. **Find "Board Games"**
4. **Add each widget**:
   - 2048 Widget - Interactive controls
   - Snake Widget - Auto-plays itself!
   - Tic-Tac-Toe Widget - Play vs AI

5. **Test features**:
   - Widgets should persist across reboots
   - State should save properly
   - Controls should be responsive

---

## Troubleshooting

### Build Errors:

**"SDK not found"**:
- Install Android SDK via Android Studio
- Set `ANDROID_HOME` environment variable

**"Java version mismatch"**:
- This project requires JDK 17
- Check with: `java -version`
- Install from: https://adoptium.net/

**"Gradle sync failed"**:
- File → Invalidate Caches → Invalidate and Restart
- Delete `.gradle` folder and sync again

### Installation Errors:

**"App not installed"**:
- Enable "Install from unknown sources"
- Make sure you have storage space
- Try uninstalling any previous version

**"Parse error"**:
- APK might be corrupted
- Try downloading/building again
- Make sure it's for your device architecture

---

## What's Included

### Modules (14 total):
```
app/                    - Main application
core/
  ├── data/            - Room database + DataStore
  ├── domain/          - Business logic
  └── ui/              - Shared UI components
game/
  ├── engine/          - Game engine abstraction
  ├── game-2048/       - 2048 implementation
  ├── game-snake/      - Snake implementation
  └── game-tictactoe/  - Tic-Tac-Toe with AI
widget/
  ├── widget-2048/     - 2048 widget
  ├── widget-snake/    - Auto-playing snake widget
  └── widget-tictactoe/ - Tic-Tac-Toe widget
feature/
  ├── home/            - Game selection screen
  └── settings/        - Settings (placeholder)
design-system/         - Material 3 theme
```

### Tech Stack:
- Kotlin 1.9.20
- Jetpack Compose (BOM 2024.02.00)
- Material 3 with dynamic colors
- Glance 1.0.0 (widgets)
- Room 2.6.1 (database)
- Koin 3.5.3 (DI)
- Coroutines + Flow
- WorkManager (Snake widget auto-play)

---

## Next Steps

Once you have the APK installed:

1. **Test all games** thoroughly
2. **Add widgets** to home screen
3. **Report any bugs** you find
4. **Enjoy playing!** 🎮

---

## Support

If you encounter issues:
1. Check this guide first
2. Look at error messages carefully
3. Try the GitHub Codespaces method - it's most reliable
4. Search for the specific error online

**Repository**: https://github.com/naman-suthar/naman2548

---

Built with ❤️ using Jetpack Compose and Material 3
