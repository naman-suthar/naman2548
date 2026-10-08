# Board Games - Android Multi-Game App

[![Android CI](https://github.com/naman-suthar/naman2548/actions/workflows/android-ci.yml/badge.svg)](https://github.com/naman-suthar/naman2548/actions/workflows/android-ci.yml)

A modern Android app featuring classic board games built with Jetpack Compose, Material 3, and home screen widgets. Play your favorite games directly from your home screen without opening the app!

## 🎮 Games

### 2048
- **App**: Full-featured 2048 game with smooth animations and gesture controls
- **Widget**: Play 2048 directly from your home screen with swipe controls
- **Features**: Score tracking, undo moves, win detection, game over handling

### Snake
- **App**: Classic snake game with auto-play mode and adjustable speed
- **Widget**: Self-playing snake that runs automatically on your home screen
- **Features**: Speed controls (1-5x), pause/resume, collision detection, food spawning

### Tic-Tac-Toe
- **App**: Strategic tic-tac-toe with multiple game modes
- **Widget**: Play against AI directly from your home screen
- **Features**: 
  - **Game Modes**: Player vs Player, Player vs CPU, CPU vs CPU
  - **AI Difficulty**: Easy (random), Medium (mixed), Hard (minimax with alpha-beta pruning)
  - **Stats Tracking**: X wins, O wins, draws
  - **Undo Support**: Take back moves

## 🏗️ Architecture

Built with clean architecture principles and modern Android development practices:

### Multi-Module Structure
```
naman2548/
├── app/                          # Main application module
├── core/
│   ├── common/                   # Shared utilities
│   ├── data/                     # Data layer with Room & DataStore
│   ├── domain/                   # Domain models and business logic
│   └── ui/                       # Shared UI components
├── game/
│   ├── engine/                   # Generic game engine abstraction
│   ├── game-2048/               # 2048 game logic and UI
│   ├── game-snake/              # Snake game logic and UI
│   └── game-tictactoe/          # Tic-Tac-Toe game logic and UI
├── widget/
│   ├── framework/               # Widget utilities
│   ├── widget-2048/            # 2048 home screen widget
│   ├── widget-snake/           # Snake home screen widget
│   └── widget-tictactoe/       # Tic-Tac-Toe home screen widget
├── feature/
│   ├── home/                    # Game selection screen
│   └── settings/                # App settings
└── design-system/               # Material 3 theme and components
```

### Tech Stack

**UI Framework**
- Jetpack Compose - Modern declarative UI
- Material 3 - Latest Material Design with dynamic colors
- Glance - Compose for app widgets

**Architecture**
- MVVM - ViewModel + StateFlow for reactive UI
- Repository Pattern - Clean data access layer
- Dependency Injection - Koin for DI

**Data Persistence**
- Room - SQLite database for game states
- DataStore - Preferences and widget state
- Kotlinx Serialization - State serialization

**Background Work**
- WorkManager - Snake widget auto-play scheduler
- Coroutines - Async operations and flows

**Testing**
- JUnit 5 - Unit testing framework
- Compose Testing - UI component testing
- Google Truth - Fluent assertions
- Turbine - Flow testing

## 🎨 Design

### Material 3 Integration
- Dynamic color theming (Material You) on Android 12+
- Adaptive layouts for different screen sizes
- Dark mode support
- Accessibility-friendly components

### Widget Design
- Responsive sizing (3 sizes per widget)
- Minimal battery impact
- State persistence across reboots
- Interactive controls with immediate feedback

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- JDK 17
- Android SDK 26+ (minimum), 34 (target)

### Build

```bash
# Clone the repository
git clone https://github.com/naman-suthar/naman2548.git
cd naman2548

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Run lint
./gradlew lintDebug
```

### Adding Widgets

1. Long-press on your home screen
2. Tap "Widgets"
3. Find "Board Games" widgets
4. Drag your favorite game to the home screen
5. Resize and enjoy!

## 🧪 Testing

### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run specific module tests
./gradlew :game:game-2048:test
./gradlew :game:game-snake:test
./gradlew :game:game-tictactoe:test
```

### UI Tests
```bash
# Run Compose UI tests
./gradlew connectedAndroidTest
```

### Code Coverage
```bash
# Generate coverage report
./gradlew jacocoTestReport

# Reports available at:
# */build/reports/jacoco/test/html/index.html
```

## 📦 CI/CD

### GitHub Actions Workflows

**Android CI** (`.github/workflows/android-ci.yml`)
- Runs on every push and PR
- Jobs: Build, Test, Lint, Code Quality
- Uploads build artifacts and test results
- Codecov integration for coverage tracking

**Release** (`.github/workflows/release.yml`)
- Triggered on version tags (v*.*.*)
- Builds signed release APK and AAB
- Creates GitHub releases
- Optional: Uploads to Play Store

### Setting Up Secrets

For release builds, configure these GitHub secrets:
- `KEYSTORE_FILE`: Base64-encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password
- `PLAY_STORE_JSON_KEY`: Play Store service account JSON (optional)
- `CODECOV_TOKEN`: Codecov upload token (optional)

## 🎯 Game Engine

All games implement a common `GameEngine` interface:

```kotlin
interface GameEngine<S : GameState, A : GameAction> {
    fun initialState(): S
    fun processAction(state: S, action: A): GameResult<S>
    fun isValidAction(state: S, action: A): Boolean
    fun isGameOver(state: S): Boolean
    fun getScore(state: S): Int
    fun serialize(state: S): String
    fun deserialize(data: String): S
}
```

This abstraction allows:
- Consistent game behavior across app and widget
- Easy state persistence and restoration
- Deterministic game logic for testing
- Clean separation of concerns

## 🤖 AI Implementation

### Tic-Tac-Toe Minimax
The Tic-Tac-Toe CPU opponent uses the minimax algorithm with alpha-beta pruning:
- **Hard difficulty**: Always plays optimally (unbeatable)
- **Medium difficulty**: 50% optimal, 50% random
- **Easy difficulty**: Completely random moves
- **Performance**: Alpha-beta pruning reduces search space by ~50%

## 📊 Project Stats

- **Modules**: 14
- **Lines of Code**: ~7,600+
- **Files**: 95+
- **Languages**: Kotlin (100%)
- **Commits**: 10+
- **Development Time**: ~21 days (61% of planned 35-day timeline)

## 🛠️ Development Roadmap

### ✅ Completed (Phase 1-4)
- [x] Multi-module project structure
- [x] Game engine abstraction
- [x] Material 3 design system
- [x] Data persistence layer
- [x] 2048 game (app + widget)
- [x] Snake game (app + widget)
- [x] Tic-Tac-Toe game (app + widget)

### 🚧 In Progress (Phase 5)
- [ ] Accessibility improvements
- [ ] Localization (i18n)
- [ ] Performance optimization
- [ ] Analytics integration
- [ ] Crash reporting
- [ ] Comprehensive documentation

### 📋 Future Enhancements
- [ ] More games (Chess, Sudoku, Minesweeper)
- [ ] Online multiplayer
- [ ] Achievements system
- [ ] Leaderboards
- [ ] Custom themes
- [ ] Sound effects and haptics

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👏 Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Widgets powered by [Glance](https://developer.android.com/jetpack/compose/glance)
- Material Design 3 by [Google](https://m3.material.io/)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m ‘Add some AmazingFeature’`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📧 Contact

Naman Suthar - namansuthar12345@gmail.com

Project Link: [https://github.com/naman-suthar/naman2548](https://github.com/naman-suthar/naman2548)

---

Made with ❤️ and Jetpack Compose
