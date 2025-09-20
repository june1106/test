# MDP Remote Controller

A production-ready Android app for the Mobile Data Processing (MDP) project that implements the full Android Remote Controller Module (ARCM) UI and Bluetooth workflows.

## Features

### Bluetooth Connectivity
- **Device Discovery**: Scan for and discover Bluetooth devices (HC-05, HC-06 modules common)
- **Connection Management**: Connect/disconnect to selected devices with robust error handling
- **Classic SPP over RFCOMM**: Implements Serial Port Profile for reliable communication
- **Auto-reconnect**: Option to automatically reconnect to the last known device

### Robot Control
- **Drive Pad**: Interactive 2×3 grid of movement controls
  - Forward, Left, Right, Reverse buttons with haptic feedback
  - Debounced input to prevent command flooding
- **Real-time Commands**: Send motion commands to robot via Bluetooth
- **Command Protocol**: Implements exact MDP command strings (FORWARD, LEFT, RIGHT, REVERSE)

### Arena Management
- **13×13 Grid Display**: Interactive 2D canvas showing the arena layout
- **Robot Pose Indicator**: Triangle/arrow showing robot position and facing direction
- **Obstacle Management**: 
  - Drag-and-drop placement of obstacles
  - Visual representation with obstacle IDs (B1, B2, etc.)
  - Target face indicators and ID badges
- **Undo/Redo**: Full history management for map changes

### Message System
- **Real-time Logging**: Scrollable message box showing robot/system communications
- **Message Parsing**: Automatic parsing of incoming messages (MSG, TARGET commands)
- **Timestamp Display**: All messages include timestamps for debugging

### Data Models
- **Robot Pose**: Position (x,y) and facing direction (N/E/S/W)
- **Obstacles**: Position, ID, target face, and target ID
- **Bluetooth State**: Comprehensive state management (Idle, Scanning, Connected, etc.)

## Architecture

### MVVM + Unidirectional Data Flow
- **MainViewModel**: Single source of truth for app state
- **Repository Pattern**: Clean separation of data sources
- **Kotlin Coroutines & Flows**: Reactive programming for real-time updates
- **Hilt Dependency Injection**: Clean architecture with dependency injection

### Module Structure
```
app/                    # Main UI and navigation
├── ui/
│   ├── connect/       # Bluetooth connection screen
│   ├── control/       # Robot control screen
│   ├── map/          # Arena map screen
│   └── components/   # Reusable UI components
├── nav/              # Navigation setup
└── MainActivity.kt   # App entry point

core/
├── bluetooth/        # Bluetooth communication
├── model/           # Data models and commands
└── map/             # Arena state management
```

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+ (API level 26)
- Target SDK 36 (Android 14)
- Kotlin 2.0.21
- Gradle 8.12.1

### Installation
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build the project

### Permissions
The app requires the following permissions:
- `BLUETOOTH` - Basic Bluetooth functionality
- `BLUETOOTH_ADMIN` - Bluetooth administration
- `BLUETOOTH_CONNECT` - Connect to Bluetooth devices (Android 12+)
- `BLUETOOTH_SCAN` - Scan for Bluetooth devices (Android 12+)
- `ACCESS_FINE_LOCATION` - Required for Bluetooth scanning
- `ACCESS_COARSE_LOCATION` - Fallback location permission

### Bluetooth Pairing
1. Enable Bluetooth on your Android device
2. Pair with your HC-05/HC-06 module in Android settings
3. Launch the MDP Remote Controller app
4. Tap the scan button to discover devices
5. Select your paired device and tap "Connect"

## Usage

### Connecting to Robot
1. **Scan for Devices**: Tap the floating action button to start scanning
2. **Select Device**: Choose your HC-05/HC-06 module from the list
3. **Connect**: Tap "Connect" to establish the Bluetooth connection
4. **Navigate**: Upon successful connection, you'll be taken to the control screen

### Controlling the Robot
1. **Drive Tab**: Use the directional buttons to control robot movement
   - Forward: Move robot forward
   - Left/Right: Turn robot left or right
   - Reverse: Move robot backward
2. **Message Log**: Monitor robot responses and system messages in real-time

### Managing the Arena
1. **Map Tab**: Switch to the map view to manage the arena
2. **Place Obstacles**: Drag and drop obstacles onto the grid
3. **Set Target Faces**: Tap obstacles to set which face has the target image
4. **View Target IDs**: Target IDs received from the robot are displayed as badges
5. **Undo/Redo**: Use the toolbar buttons to undo/redo map changes

## Command Protocol

### Outbound Commands (to Robot)
```
FORWARD          # Move robot forward
LEFT             # Turn robot left
RIGHT            # Turn robot right
REVERSE          # Move robot backward
ADD,B1,(10,6)    # Add obstacle B1 at position (10,6)
SUB,B1           # Remove obstacle B1
FACE,B2,N        # Set target face of obstacle B2 to North
```

### Inbound Commands (from Robot)
```
MSG,[Moving]                    # Robot status message
TARGET,B2,11                    # Target ID 11 detected on obstacle B2
TARGET,B2,11,N                  # Target ID 11 detected on North face of B2
```

## Development

### Building for Production
```bash
./gradlew assembleRelease
```

### Running Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

### Fake Bluetooth Mode
For development and testing without physical hardware:
- The app includes a `FakeBluetoothRepository` that simulates Bluetooth operations
- Provides realistic device discovery and message simulation
- Useful for UI development and testing

### Key Dependencies
- **Jetpack Compose**: Modern UI toolkit
- **Navigation Compose**: Navigation between screens
- **ViewModel**: State management
- **Hilt**: Dependency injection
- **Coroutines**: Asynchronous programming
- **DataStore**: Persistent storage

## Troubleshooting

### Connection Issues
- Ensure Bluetooth is enabled on your device
- Verify the HC-05/HC-06 module is paired in Android settings
- Check that the module is in discoverable mode
- Try restarting the app if connection fails

### Permission Issues
- Grant all required permissions when prompted
- For Android 12+, ensure location permissions are granted for Bluetooth scanning
- Check app settings if permissions are denied

### Build Issues
- Ensure all dependencies are properly synced
- Clean and rebuild the project if needed
- Check that the correct SDK versions are installed

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is part of the MDP (Mobile Data Processing) project. Please refer to the project documentation for licensing information.
