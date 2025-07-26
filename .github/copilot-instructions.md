<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Board Game Inventory Android App - Copilot Instructions

This is an Android application built in Kotlin for managing board game collections. The app follows modern Android development best practices and architecture patterns.

## Architecture & Patterns
- Use MVVM architecture with Repository pattern
- Implement ViewModels for business logic
- Use Room database for local data persistence
- Apply Kotlin Coroutines and Flow for asynchronous operations
- Follow Material Design 3 guidelines for UI components

## Code Style & Conventions
- Use Kotlin coding conventions
- Prefer data classes for models
- Use sealed classes for states and events
- Implement proper null safety
- Use extension functions where appropriate
- Follow single responsibility principle

## Database & Data Layer
- Use Room for database operations
- Implement proper DAO methods with suspend functions
- Use Flow for reactive data streams
- Handle database migrations properly
- Implement repository pattern for data access

## UI & User Experience
- Use Material Design 3 components
- Implement proper loading states
- Show appropriate error messages
- Use proper navigation patterns
- Handle edge cases and empty states
- Ensure accessibility compliance

## Networking & API
- Use Retrofit for network calls
- Implement proper error handling
- Use OkHttp interceptors for logging
- Handle network failures gracefully
- Implement offline capabilities where possible

## Testing & Quality
- Write unit tests for ViewModels and repositories
- Implement UI tests for critical user flows
- Use proper mocking for dependencies
- Follow TDD practices where applicable

## Permissions & Security
- Request permissions at runtime
- Handle permission denial gracefully
- Implement proper data validation
- Use secure networking practices

## Performance Considerations
- Use appropriate image loading libraries (Glide)
- Implement proper list adapters with ViewBinding
- Use background threads for heavy operations
- Implement proper memory management

## File Operations
- Handle file imports/exports safely
- Use proper content providers for file access
- Implement progress indicators for long operations
- Handle storage permissions correctly

When suggesting code improvements or new features, prioritize:
1. User experience and accessibility
2. Code maintainability and readability
3. Performance and memory efficiency
4. Proper error handling and edge cases
5. Following Android best practices and guidelines
