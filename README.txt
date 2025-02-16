# MyMealMateV1
- -------------------- -
A comprehensive meal planning and recipe management Android application that helps users organize their meals, manage recipes, create shopping lists and share to other people. 

## Features
- ---------------------- -
1. User Authentication
   - Secure login and registration system
   - User profile management
   - Guest users

2. Recipe Management
   - Store and organize recipes
   - Recipe details and instructions

3. Meal Planning
   - Create and manage meal plans
   - Schedule meals for different days
   - Meal plan calendar view

4. Shopping List
   - Generate shopping lists from Meal Plans
   - Manage shopping items
   - Mark items as purchased

5. SMS Integration
   - Share Meal plans and shopping lists via SMS
   - Receive notifications and updates

6. Swipe left and Right
   - Swipe left to delete item
   - Swipe right to Share SMS

## Technical Specifications
- ------------------------ -
### Development Environment
- ------------------------- -
- Minimum SDK: 24 (Android 7.0 Nugut)
- Target SDK: 35
- Language: Java
- Build System: Gradle (Kotlin DSL)

### Architecture & Components
- ------------------------ -
- Layer-based architecture with UI and Data layers
- Room Database for local storage
- AndroidX components
- Material Design UI
- Navigation Components
- RecyclerView for list displays
- Google Play Services Maps integration

### Database Structure
- ------------------- -
- Users
- Recipes
- Shopping Items
- Meal Plans With Recipe
- Meal Plans

## Project Structure
- --------------- - 
MyMealMateV1/
    app/
        src/
            main/
                java/
                    com.example.mymealmatev1/
                        data/
                            adapters/
                            dao/
                            entity/
                        ui/
                           auth/
                           fragments/
                           home/
                        utils/
                res/
             AndroidManifest.xml
           test/
           androidTest/
        build.gradle.kts
     build.gradle.kts

## Dependencies
- ------------ -
- AndroidX AppCompat
- Material Design Components
- Navigation Components
- Room Database
- RecyclerView
- ConstraintLayout
- JUnit and Espresso for testing

## Getting Started
- -------------------- -
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the application on an emulator or physical device

## Requirements
- ----------- - 
- Android Studio
- JDK 11 or higher
- Android SDK with minimum API level 24

## Building and Running
- -------------------- -
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Select a device/emulator
4. Click Run (or press Shift + F10)

## Testing
- ---------- -
- Unit tests are located in the `test` directory
- Instrumentation tests are in the `androidTest` directory
- Run tests using Android Studio's test runner

## Version
- -------------- -
Current Version: 1.0
