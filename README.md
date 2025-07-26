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

## Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **UI**: Material Design 3 components
- **Networking**: Retrofit + OkHttp
- **Barcode Scanning**: ZXing Android Embedded
- **Image Loading**: Glide
- **Async Operations**: Kotlin Coroutines + Flow
- **File Handling**: OpenCSV + Apache POI

## Requirements

- Android 7.0 (API level 24) or higher
- Camera permission for barcode scanning
- Internet permission for API lookups
- Storage permissions for import/export functionality

## Setup Instructions

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run the application

## Project Structure

```
app/
├── src/main/java/com/boardgameinventory/
│   ├── api/                    # API services and models
│   ├── data/                   # Room database entities and DAOs
│   ├── repository/             # Data repository
│   ├── ui/                     # Activities and fragments
│   ├── utils/                  # Utility classes
│   ├── viewmodel/              # ViewModels
│   └── MainActivity.kt         # Main entry point
├── src/main/res/
│   ├── layout/                 # XML layout files
│   ├── values/                 # Colors, strings, styles
│   └── drawable/               # Icons and drawables
└── build.gradle               # App-level Gradle configuration
```

## Database Schema

The app uses Room database with the following main entity:

**Game**
- id (Primary Key)
- name
- barcode
- bookcase
- shelf
- loanedTo (nullable)
- description (nullable)
- imageUrl (nullable)
- dateAdded
- dateLoaned (nullable)

## API Integration

The app integrates with RapidAPI's barcode lookup service to automatically fetch game information including:
- Game title
- Description
- Cover image
- Additional product details

## Building the App

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
./gradlew test
```

## Permissions

The app requires the following permissions:
- `CAMERA` - For barcode scanning
- `INTERNET` - For API calls
- `READ_EXTERNAL_STORAGE` - For importing files
- `WRITE_EXTERNAL_STORAGE` - For exporting files

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This project is licensed under the MIT License - see the original Python repository for details.

## Original Source

This Android app is based on the Python board game inventory tracker by Matthew Stebbins:
https://github.com/MatthewStebbins/boardgame_inventory
