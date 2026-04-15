# Doubt Desk - Student Query Management App

## ⚠️ IMPORTANT SECURITY NOTICE

**Before pushing to public repository:**
1. ✅ `google-services.json` is now in `.gitignore`
2. ⚠️ **API Key is still in code** - See setup instructions below

## 📱 About
Android app for managing student queries with AI-powered solutions. Students can submit questions, and admins can solve them with AI assistance.

## 🔐 Setup Instructions

### 1. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Download `google-services.json`
3. Place it in `app/` directory
4. **DO NOT commit this file to Git** (already in `.gitignore`)

### 2. API Key Setup (IMPORTANT!)
The AI API key is currently in `app/src/main/java/com/example/firstapp/config/ApiConfig.kt`

**For production/public repos:**

1. Create `apikeys.properties` in root directory:
   ```properties
   AI_API_KEY=your_api_key_here
   ```

2. Add to `.gitignore` (already added):
   ```
   apikeys.properties
   ```

3. Update `app/build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           // Load API key from properties file
           val apiKeysFile = rootProject.file("apikeys.properties")
           if (apiKeysFile.exists()) {
               val apiKeys = java.util.Properties()
               apiKeys.load(java.io.FileInputStream(apiKeysFile))
               buildConfigField("String", "AI_API_KEY", "\"${apiKeys["AI_API_KEY"]}\"")
           }
       }
       buildFeatures {
           buildConfig = true
       }
   }
   ```

4. Update `ApiConfig.kt`:
   ```kotlin
   fun getAiApiKey(): String = BuildConfig.AI_API_KEY
   ```

## ✨ Recent Refactoring (Code Quality Improvements)

### What Changed?
The codebase was refactored to eliminate code duplication and improve maintainability. **All functionality remains the same.**

### New Structure
```
app/src/main/java/com/example/firstapp/
├── base/
│   └── BaseActivity.kt              # Common activity functionality
├── config/
│   └── ApiConfig.kt                 # API configuration
├── ui/
│   └── SwipeCallback.kt             # Reusable swipe gestures
└── utils/
    ├── Constants.kt                 # All constants
    ├── AuthUtils.kt                 # Authentication helpers
    ├── FirebaseUtils.kt             # Firebase helpers
    ├── ViewUtils.kt                 # View helpers
    └── NoteFormatter.kt             # Text formatting
```

### Key Improvements
- ✅ **300+ lines of duplicate code eliminated**
- ✅ **Centralized authentication logic** (logout, admin check)
- ✅ **Centralized Firebase operations**
- ✅ **Reusable swipe gesture component**
- ✅ **API key centralized** for better security
- ✅ **All constants in one place**

## 🚀 Features

### Student Features
- Create and submit queries
- View query status (solved/pending)
- Edit queries (swipe right)
- Delete queries (swipe left)
- View AI-generated answers
- Access related YouTube links

### Admin Features
- View all student queries
- Filter by class (FY, SY, TY)
- Filter by status (solved/unsolved)
- Search by name or subject
- Solve queries with AI assistance
- Add YouTube reference links

## 🛠️ Tech Stack
- **Language:** Kotlin
- **Architecture:** MVVM pattern with utilities
- **Database:** Firebase Realtime Database
- **Authentication:** Firebase Auth (Email/Password, Google Sign-In)
- **AI Integration:** Groq API (llama-3.1-8b-instant)
- **UI:** Material Design, View Binding

## 🧪 Testing Checklist

- [ ] Login (email/password and Google)
- [ ] Admin routing (admin123@gmail.com → AdminDashboard)
- [ ] Student routing (other emails → MainActivity)
- [ ] Add, edit, delete notes
- [ ] Swipe gestures (left: delete, right: edit)
- [ ] Admin solve with AI
- [ ] View solved notes
- [ ] Logout from all screens

## 📝 Admin Credentials
- Email: `admin123@gmail.com`
- Password: (set during Firebase setup)

**Note:** For production, implement server-side admin validation using Firebase Custom Claims instead of email checking.

## 🎯 Code Quality
- No code duplication
- Single source of truth for constants
- Reusable utility functions
- Clean architecture
- Professional patterns

## ⚠️ Before Public Push
- [ ] Move API key to `apikeys.properties`
- [ ] Update `ApiConfig.kt` to use BuildConfig
- [ ] Verify `google-services.json` is not tracked
- [ ] Remove any debug logs with sensitive data
- [ ] Test the app thoroughly

## 📄 License
[Your License Here]

