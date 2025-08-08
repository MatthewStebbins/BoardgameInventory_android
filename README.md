# Board Game Inventory - Android App

An Android application built in Kotlin for managing your board game collection. This app is a mobile conversion of the original Python desktop application, providing all the same functionality with a modern Android interface.

## Features

- **Add Games**: Add games to your inventory using barcode scanning or manual entry
- **Bulk Upload**: Add multiple games at once by scanning location and game barcodes
- **Game Management**: View, edit, loan, return, and delete games from your collection
- **Barcode Scanning**: Integrated barcode scanner using ZXing library
- **API Integration**: Automatic game information lookup using barcode API
- **Import/Export**: Support for CSV and Excel file import/export
- **Location Tracking**: Track game locations with bookcase and shelf information
- **Loan Management**: Keep track of who borrowed games and when
- **Search and Filtering**: Find games quickly with advanced search and filtering options
- **Collection Analytics**: View statistics about your board game collection
- **Offline Support**: Full functionality even without internet connection
- **Dark Mode**: Support for system dark mode preferences

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **UI**: Material Design 3 components
- **Dependency Injection**: Koin
- **Navigation**: Jetpack Navigation Component
- **Networking**: Retrofit + OkHttp
- **Barcode Scanning**: ZXing Android Embedded
- **Image Loading**: Glide
- **Async Operations**: Kotlin Coroutines + Flow
- **File Handling**: OpenCSV + Apache POI
- **Analytics**: Firebase Analytics
- **Testing**: JUnit, Espresso, MockK

## Requirements

- Android 7.0 (API level 24) or higher
- Camera permission for barcode scanning
- Internet permission for API lookups
- Storage permissions for import/export functionality

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Configure your API key in local.properties file:
   ```
   api.key=YOUR_RAPIDAPI_KEY
   ```
5. Build and run the application

## Project Structure

```
app/
├── src/main/java/com/boardgameinventory/
│   ├── api/                    # API services and models
│   ├── data/                   # Room database entities and DAOs
│   │   ├── entities/           # Database entities
│   │   ├── dao/                # Data Access Objects
│   │   ├── converters/         # Type converters
│   │   └── relations/          # Entity relationships
│   ├── di/                     # Dependency injection modules
│   ├── repository/             # Data repositories
│   ├── ui/                     # Activities and fragments
│   │   ├── add/                # Add game screens
│   │   ├── detail/             # Game detail screens
│   │   ├── list/               # Game list screens
│   │   ├── loan/               # Loan management screens
│   │   ├── scan/               # Barcode scanning screens
│   │   ├── search/             # Search functionality
│   │   ├── settings/           # App settings
│   │   ├── stats/              # Collection statistics
│   │   ├── import/             # Import functionality
│   │   └── export/             # Export functionality
│   ├── utils/                  # Utility classes
│   │   ├── extensions/         # Kotlin extensions
│   │   ├── formatters/         # Data formatters
│   │   └── validators/         # Input validators
│   ├── viewmodel/              # ViewModels
│   ├── workers/                # Background workers
│   └── MainActivity.kt         # Main entry point
├── src/main/res/
│   ├── layout/                 # XML layout files
│   ├── navigation/             # Navigation graphs
│   ├── values/                 # Colors, strings, styles, themes
│   └── drawable/               # Icons and drawables
├── src/test/                   # Unit tests
├── src/androidTest/            # Instrumented tests
└── build.gradle               # App-level Gradle configuration
```

## Database Schema

The app uses Room database with the following main entities:

**Game**
- id (Primary Key)
- name
- barcode
- imageUrl (nullable)
- description (nullable)
- publisher (nullable)
- yearPublished (nullable)
- minPlayers
- maxPlayers
- playTime
- dateAdded
- lastPlayed (nullable)
- rating (nullable)

**Location**
- id (Primary Key)
- gameId (Foreign Key)
- bookcase
- shelf
- notes (nullable)

**LoanInfo**
- id (Primary Key)
- gameId (Foreign Key)
- loanedTo
- contactInfo (nullable)
- dateLoaned
- expectedReturnDate (nullable)
- isReturned
- dateReturned (nullable)

**PlaySession**
- id (Primary Key)
- gameId (Foreign Key)
- date
- players
- notes (nullable)
- winner (nullable)

## API Integration

The app integrates with RapidAPI's barcode lookup service to automatically fetch game information including:
- Game title
- Description
- Cover image
- Publisher information
- Player count
- Play time
- Year published
- Additional product details

The app includes intelligent fallback mechanisms when API data is unavailable or incomplete.

## Building the App

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- JDK 11 or JDK 17
- Android SDK 31 or newer

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Running Tests
```bash
./gradlew test           # Unit tests
./gradlew connectedCheck # Instrumented tests
```

## Permissions

The app requires the following permissions:
- `CAMERA` - For barcode scanning
- `INTERNET` - For API calls
- `READ_EXTERNAL_STORAGE` - For importing files
- `WRITE_EXTERNAL_STORAGE` - For exporting files

The app follows Android's runtime permission model, requesting permissions only when needed and explaining their purpose to the user.

## Error Handling

The app implements comprehensive error handling:
- Network connectivity issues
- Barcode scanning failures
- Import/export errors
- Database migration errors
- API response validation

## Accessibility

The app follows Android accessibility guidelines:
- Content descriptions for all images
- Proper text contrast ratios
- Support for screen readers
- Keyboard navigation support
- Dynamic text sizing

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests to ensure functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the original Python repository for details.

## Original Source

This Android app is based on the Python board game inventory tracker by Matthew Stebbins:
https://github.com/MatthewStebbins/boardgame_inventory
