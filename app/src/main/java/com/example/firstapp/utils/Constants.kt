package com.example.firstapp.utils

object Constants {
    // Firebase paths
    const val FIREBASE_USERS = "users"
    const val FIREBASE_NOTES = "notes"
    
    // SharedPreferences
    const val PREF_NAME = "MyAppPrefs"
    const val PREF_IS_USER_LOGGED_OUT = "isUserLoggedOut"
    const val PREF_USER_ROLE = "userRole"
    
    // User roles
    const val ROLE_ADMIN = "admin"
    const val ROLE_STUDENT = "student"
    
    // Admin email (TODO: Move to server-side validation)
    const val ADMIN_EMAIL = "admin123@gmail.com"
    
    // AI Model
    const val AI_MODEL = "llama-3.1-8b-instant"
    const val AI_MAX_TOKENS = 200
}
